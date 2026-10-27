package br.mackenzie;

import com.fazecast.jSerialComm.SerialPort;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Le o MPU6050 (via ESP32, porta serial) numa thread separada e transforma a
 * inclinacao lateral em uma "direcao" entre -1 (esquerda) e +1 (direita).
 *
 * O programa do ESP32 NAO foi alterado. Ele envia varias linhas por leitura;
 * so a linha que comeca com "Acel" interessa (ex.: "Aceleração X: 1 | Y: 2 | Z: 3").
 */
public class ControleIoT {

    // ================= CONFIGURACOES (ajuste aqui) =================
    private static final String NOME_PORTA = "COM3";
    private static final int VELOCIDADE_SERIAL = 115200;

    /** Eixo que muda ao inclinar para os lados: 'X' ou 'Y'. Descubra olhando o console (ver roteiro de teste). */
    private static final char EIXO_LATERAL = 'X';
    /** Se inclinar para a direita mover o cachorro para a esquerda, troque para true. */
    private static final boolean INVERTER_DIRECAO = false;

    // O MPU6050 sai de fabrica em +-2g: 16384 unidades = 1g (confirmado pelo seu Z, perto de 16000).
    // Valor ~ 16384 * sen(angulo). Zona morta ~8 graus, velocidade maxima com ~25 graus.
    private static final float ZONA_MORTA = 2300f;
    private static final float INCLINACAO_MAXIMA = 6900f;

    private static final float SUAVIZACAO = 0.6f;          // 0..1 (maior = responde mais rapido)
    private static final long TEMPO_CALIBRACAO_MS = 1500;  // tempo parado para medir a posicao neutra
    private static final long DADOS_VENCEM_MS = 1500;      // leitura mais velha que isso e ignorada
    private static final boolean DEPURAR = true;           // imprime leituras no console
    // ===============================================================

    public enum Estado { DESCONECTADO, SEM_DADOS, RECEBENDO }

    private static final Pattern PADRAO_EIXOS =
        Pattern.compile("X:\\s*(-?\\d+)\\s*\\|\\s*Y:\\s*(-?\\d+)\\s*\\|\\s*Z:\\s*(-?\\d+)");

    private final Object trava = new Object();      // protege os valores do sensor
    private final Object travaPorta = new Object(); // protege a porta serial
    private SerialPort porta;
    private Thread thread;
    private volatile boolean rodando = false;
    private volatile Estado estado = Estado.DESCONECTADO;

    // Valores do sensor (acessar somente dentro de synchronized(trava))
    private int acX, acY, acZ;
    private float lateralFiltrado;
    private boolean temFiltro = false;
    private long ultimaLeituraMs = 0;
    private long amostrasRecebidas = 0;
    private long ultimoLogMs = 0;

    // Calibracao
    private boolean calibrando = false;
    private boolean calibrado = false;
    private long fimCalibracaoMs = 0; // 0 = ainda nao chegou a primeira amostra da calibracao
    private float somaCalibracao = 0;
    private int qtdCalibracao = 0;
    private float neutro = 0;

    /** Inicia a thread de leitura. O jogo nao espera por ela. */
    public void iniciar() {
        if (thread != null) return;
        rodando = true;
        thread = new Thread(this::executar, "ControleIoT");
        thread.setDaemon(true);
        thread.start();
    }

    /** Encerra a thread e libera a porta COM (seguro chamar mais de uma vez). */
    public void fechar() {
        rodando = false;
        if (thread != null) {
            thread.interrupt();
            try { thread.join(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            thread = null;
        }
        fecharPorta();
        estado = Estado.DESCONECTADO;
    }

    /** Pede nova calibracao: mantenha o controle parado na posicao neutra. */
    public void iniciarCalibracao() {
        synchronized (trava) {
            calibrando = true;
            fimCalibracaoMs = 0;
            somaCalibracao = 0;
            qtdCalibracao = 0;
        }
    }

    // ================= usado pelo jogo =================

    /** -1 = esquerda, +1 = direita, 0 = parado. Valor proporcional a inclinacao. */
    public float getDirecao() {
        synchronized (trava) {
            if (estado != Estado.RECEBENDO || calibrando || !calibrado) return 0;
            if (System.currentTimeMillis() - ultimaLeituraMs > DADOS_VENCEM_MS) return 0; // dado velho
            float desvio = lateralFiltrado - neutro;
            if (INVERTER_DIRECAO) desvio = -desvio;
            float modulo = Math.abs(desvio);
            if (modulo < ZONA_MORTA) return 0;
            float intensidade = Math.min(1f, (modulo - ZONA_MORTA) / (INCLINACAO_MAXIMA - ZONA_MORTA));
            return desvio < 0 ? -intensidade : intensidade;
        }
    }

    public int getAcX() { synchronized (trava) { return acX; } }
    public int getAcY() { synchronized (trava) { return acY; } }
    public int getAcZ() { synchronized (trava) { return acZ; } }
    public Estado getEstado() { return estado; }
    public long getAmostrasRecebidas() { synchronized (trava) { return amostrasRecebidas; } }

    /** Texto curto para mostrar na tela. */
    public String getTextoStatus() {
        if (estado == Estado.DESCONECTADO) return "Sensor desconectado (usando teclado)";
        if (estado == Estado.SEM_DADOS) return "Sensor sem dados";
        synchronized (trava) {
            if (calibrando) return "Calibrando... mantenha parado";
        }
        return "Sensor ativo";
    }

    // ================= thread de leitura =================

    private void executar() {
        while (rodando) {
            if (!abrirPorta()) {
                estado = Estado.DESCONECTADO;
                dormir(2000); // tenta de novo (ex.: ESP32 plugado depois)
                continue;
            }
            estado = Estado.SEM_DADOS; // porta aberta ainda NAO significa que ha dados
            lerAteFalhar();
            fecharPorta();
            estado = Estado.DESCONECTADO;
        }
    }

    private boolean abrirPorta() {
        SerialPort p = SerialPort.getCommPort(NOME_PORTA);
        p.setComPortParameters(VELOCIDADE_SERIAL, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        // Nao bloqueante: usamos bytesAvailable() e nunca ficamos presos (nem recebemos excecao de timeout).
        p.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        if (!p.openPort()) {
            System.out.println("IoT: nao foi possivel abrir " + NOME_PORTA
                + " (Monitor Serial aberto? ESP32 desconectado?). Portas encontradas:");
            for (SerialPort s : SerialPort.getCommPorts())
                System.out.println("   - " + s.getSystemPortName() + " : " + s.getDescriptivePortName());
            return false;
        }
        synchronized (travaPorta) { porta = p; }
        System.out.println("IoT: porta " + NOME_PORTA + " ABERTA (aguardando dados...)");
        return true;
    }

    /** Le bytes, monta linhas e interpreta. Retorna quando a porta falha ou o jogo fecha. */
    private void lerAteFalhar() {
        final SerialPort p = porta; // copia local: fechar() pode zerar o campo
        byte[] buffer = new byte[256];
        StringBuilder linha = new StringBuilder();
        while (rodando) {
            int disponiveis = p.bytesAvailable();
            if (disponiveis < 0) { System.out.println("IoT: porta perdida."); return; }
            if (disponiveis == 0) {
                // Sem dados novos: se ficar muito tempo assim, marca SEM_DADOS.
                synchronized (trava) {
                    long agora = System.currentTimeMillis();
                    if (estado == Estado.RECEBENDO && agora - ultimaLeituraMs > DADOS_VENCEM_MS) {
                        estado = Estado.SEM_DADOS;
                        System.out.println("IoT: parou de receber dados.");
                    }
                }
                dormir(5);
                continue;
            }
            int lidos = p.readBytes(buffer, Math.min(disponiveis, buffer.length));
            if (lidos < 0) { System.out.println("IoT: erro de leitura, porta perdida."); return; }
            for (int i = 0; i < lidos; i++) {
                char c = (char) (buffer[i] & 0xFF);
                if (c == '\n') {
                    tratarLinha(linha.toString().trim());
                    linha.setLength(0);
                } else if (c != '\r' && linha.length() < 200) {
                    linha.append(c);
                }
            }
        }
    }

    private void tratarLinha(String texto) {
        int[] v = interpretarLinha(texto);
        if (v != null) registrarAmostra(v[0], v[1], v[2]);
    }

    /**
     * Retorna {x, y, z} se a linha for de aceleracao; null para qualquer outra
     * (giroscopio, temperatura, separador, lixo do boot do ESP32).
     * Usa so o inicio "Acel" para nao depender de acentos/codificacao.
     */
    static int[] interpretarLinha(String texto) {
        if (!texto.startsWith("Acel")) return null;
        Matcher m = PADRAO_EIXOS.matcher(texto);
        if (!m.find()) return null;
        try {
            int x = Integer.parseInt(m.group(1)), y = Integer.parseInt(m.group(2)), z = Integer.parseInt(m.group(3));
            // int16 do MPU6050: fora disso e leitura corrompida
            if (Math.abs(x) > 32768 || Math.abs(y) > 32768 || Math.abs(z) > 32768) return null;
            return new int[] { x, y, z };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void registrarAmostra(int x, int y, int z) {
        synchronized (trava) {
            long agora = System.currentTimeMillis();
            acX = x; acY = y; acZ = z;
            amostrasRecebidas++;
            float lateral = (EIXO_LATERAL == 'Y') ? y : x;

            if (estado != Estado.RECEBENDO) {
                estado = Estado.RECEBENDO;
                System.out.println("IoT: RECEBENDO dados do sensor.");
            }
            if (!temFiltro) { lateralFiltrado = lateral; temFiltro = true; }
            else lateralFiltrado += SUAVIZACAO * (lateral - lateralFiltrado);
            ultimaLeituraMs = agora;

            if (calibrando) {
                if (fimCalibracaoMs == 0) fimCalibracaoMs = agora + TEMPO_CALIBRACAO_MS; // comeca na 1a amostra
                somaCalibracao += lateral;
                qtdCalibracao++;
                if (agora >= fimCalibracaoMs) {
                    neutro = somaCalibracao / qtdCalibracao;
                    calibrando = false;
                    calibrado = true;
                    System.out.println("IoT: calibrado. Neutro (eixo " + EIXO_LATERAL + ") = " + neutro
                        + " [" + qtdCalibracao + " amostras]");
                }
            }

            if (DEPURAR && agora - ultimoLogMs >= 250) {
                ultimoLogMs = agora;
                System.out.println("IoT #" + amostrasRecebidas + " | X=" + x + " Y=" + y + " Z=" + z
                    + " | eixo " + EIXO_LATERAL + " filtrado=" + Math.round(lateralFiltrado)
                    + " neutro=" + Math.round(neutro) + (calibrando ? " (calibrando)" : ""));
            }
        }
    }

    private void fecharPorta() {
        synchronized (travaPorta) {
            if (porta != null) {
                if (porta.isOpen()) porta.closePort();
                porta = null;
                System.out.println("IoT: porta " + NOME_PORTA + " fechada.");
            }
        }
    }

    private void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { /* fechar() chamou interrupt */ }
    }
}