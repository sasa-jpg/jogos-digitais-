package br.mackenzie;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Fundo vivo em camadas:
 *  1) chao: tile vertical que se repete sem emenda (rua / trilha na floresta / neve nos Alpes);
 *  2) decoracoes: sprites nas laterais que descem com a estrada e balancam com o vento;
 *  3) camada da frente: sombras de nuvens (parallax), passaros, e efeitos de cada fase
 *     (cidade: folhas | floresta: folhas, raios de luz e vaga-lumes | Alpes: neve caindo).
 *
 * O chao e as decoracoes usam o mesmo "progresso" dos obstaculos (ficam sincronizados).
 * Os efeitos vivos usam o proprio relogio (atualizar), entao tambem se mexem no menu.
 */
public class FundoRolante {
    /** Limites da estrada no tile (em pixels da tela de 960 x 720). */
    public static final float ESTRADA_ESQUERDA = 170f;
    public static final float ESTRADA_DIREITA = 790f;

    /** Tipo de decoracao: arquivo, peso (chance de aparecer) e se balanca com o vento. */
    private static class Tipo {
        final String arquivo; final int peso; final boolean balanca;
        Tipo(String arquivo, int peso, boolean balanca) { this.arquivo = arquivo; this.peso = peso; this.balanca = balanca; }
    }
    private static class Item {
        Texture textura; float x, yMundo, largura, altura; boolean balanca;
    }
    private static class Nuvem {
        Texture textura; float x0, y0, largura, altura, vx, parallax;
    }
    private static class Passaro {
        float x, y, vx, fase;
    }
    private static class Particula {
        float x, y, queda, amplitude, frequencia, fase, rotacao, giro, escala;
        Color cor;
    }
    private static class Raio {
        float x0, largura, fase;
    }
    private static class VagaLume {
        float x0, y0, raioX, raioY, frequencia, fase, escala;
    }

    private final float larguraTela, alturaTela;
    private final int fase;
    private final Texture chao;
    private final Map<String, Texture> texturas = new HashMap<String, Texture>();
    private final List<Item> itens = new ArrayList<Item>();
    private final List<Nuvem> nuvens = new ArrayList<Nuvem>();
    private final List<Passaro> bando = new ArrayList<Passaro>();
    private final List<Particula> particulas = new ArrayList<Particula>();
    private final List<Raio> raios = new ArrayList<Raio>();
    private final List<VagaLume> vagaLumes = new ArrayList<VagaLume>();
    private final Random aleatorio = new Random();
    private final Texture passaro1, passaro2, texParticula, texRaio, texVagaLume;
    private final Color corPassaro;
    private float tempo = 0, esperaPassaro = 2f;

    /**
     * @param fase        1 = cidade, 2 = floresta, 3 = Alpes
     * @param comprimento distancia total da fase (para gerar decoracoes ate o fim)
     */
    public FundoRolante(int fase, float larguraTela, float alturaTela, float comprimento) {
        this.fase = fase;
        this.larguraTela = larguraTela;
        this.alturaTela = alturaTela;
        chao = new Texture(arquivoChao(fase));
        chao.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        gerarDecoracoes(tiposDaFase(fase), comprimento + alturaTela + 150f);

        passaro1 = carregar("vivo/passaro_1.png");
        passaro2 = carregar("vivo/passaro_2.png");
        texParticula = carregar(fase == 3 ? "vivo/neve.png" : "vivo/folha.png");
        texRaio = fase == 2 ? carregar("vivo/raio_luz.png") : null;
        texVagaLume = fase == 2 ? carregar("vivo/vaga_lume.png") : null;
        // Cor dos passaros de cada fase (o PNG e claro para poder ser tingido).
        if (fase == 1) corPassaro = new Color(0.50f, 0.54f, 0.66f, 1f);
        else if (fase == 2) corPassaro = new Color(0.85f, 0.35f, 0.28f, 1f);
        else corPassaro = new Color(0.28f, 0.32f, 0.42f, 1f);

        gerarNuvens();
        gerarParticulas(fase == 3 ? 46 : 18);
        if (fase == 2) { gerarRaios(); gerarVagaLumes(16); }
    }

    private static String arquivoChao(int fase) {
        if (fase == 1) return "fundo/rua_cidade.png";
        if (fase == 2) return "fundo/trilha_floresta.png";
        return "fundo/trilha_alpes.png";
    }

    /** Edite aqui para trocar/adicionar sprites de cada fase. O 3o valor = balanca com o vento. */
    private static Tipo[] tiposDaFase(int fase) {
        if (fase == 1) return new Tipo[] {
            new Tipo("decor/cidade_arvore.png", 4, true), new Tipo("decor/cidade_poste.png", 3, false),
            new Tipo("decor/cidade_hidrante.png", 2, false), new Tipo("decor/cidade_banco.png", 2, false),
            new Tipo("decor/cidade_vaso.png", 2, true)
        };
        if (fase == 2) return new Tipo[] {
            new Tipo("decor/floresta_arvore_grande.png", 4, true), new Tipo("decor/floresta_arvore.png", 3, true),
            new Tipo("decor/floresta_pinheiro.png", 2, true), new Tipo("decor/floresta_samambaia.png", 3, true),
            new Tipo("decor/floresta_cogumelos.png", 2, false), new Tipo("decor/floresta_tronco_caido.png", 2, false),
            new Tipo("decor/floresta_pedra_musgo.png", 2, false), new Tipo("decor/floresta_flores.png", 2, false),
            new Tipo("decor/floresta_arbusto.png", 2, true), new Tipo("decor/floresta_toco.png", 1, false)
        };
        return new Tipo[] {
            new Tipo("decor/alpes_pinheiro_a.png", 4, true), new Tipo("decor/alpes_pinheiro_b.png", 3, true),
            new Tipo("decor/alpes_rocha.png", 3, false), new Tipo("decor/alpes_rocha_pequena.png", 2, false),
            new Tipo("decor/alpes_monte_neve.png", 3, false), new Tipo("decor/alpes_chale.png", 1, false),
            new Tipo("decor/alpes_boneco_neve.png", 1, false)
        };
    }

    private Texture carregar(String arquivo) {
        Texture t = texturas.get(arquivo);
        if (t == null) {
            t = new Texture(arquivo);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            texturas.put(arquivo, t);
        }
        return t;
    }

    private Tipo sortear(Tipo[] tipos) {
        int total = 0;
        for (Tipo t : tipos) total += t.peso;
        int r = aleatorio.nextInt(total);
        for (Tipo t : tipos) { r -= t.peso; if (r < 0) return t; }
        return tipos[0];
    }

    private void gerarDecoracoes(Tipo[] tipos, float ateY) {
        // Uma "linha" a cada 70-150 px; em cada linha tenta um item na esquerda e outro na direita.
        for (float y = 0; y < ateY; y += 70f + aleatorio.nextFloat() * 80f) {
            for (int lado = 0; lado < 2; lado++) {
                if (aleatorio.nextFloat() > 0.8f) continue;
                Tipo tipo = sortear(tipos);
                Texture t = carregar(tipo.arquivo);
                float escala = 0.85f + aleatorio.nextFloat() * 0.3f;
                Item item = new Item();
                item.textura = t;
                item.balanca = tipo.balanca;
                item.largura = t.getWidth() * escala;
                item.altura = t.getHeight() * escala;
                item.yMundo = y + aleatorio.nextFloat() * 40f;
                // A copa pode invadir um pouco a estrada (15 px) para parecer natural.
                if (lado == 0) {
                    float minX = -item.largura * 0.15f;
                    float maxX = ESTRADA_ESQUERDA + 15f - item.largura;
                    item.x = minX + aleatorio.nextFloat() * Math.max(1f, maxX - minX);
                } else {
                    float minX = ESTRADA_DIREITA - 15f;
                    float maxX = larguraTela - item.largura * 0.85f;
                    item.x = minX + aleatorio.nextFloat() * Math.max(1f, maxX - minX);
                }
                itens.add(item);
            }
        }
    }

    private void gerarNuvens() {
        for (int i = 0; i < 4; i++) {
            Nuvem n = new Nuvem();
            n.textura = carregar(i % 2 == 0 ? "vivo/sombra_nuvem_a.png" : "vivo/sombra_nuvem_b.png");
            float escala = 1.0f + aleatorio.nextFloat() * 0.8f;
            n.largura = n.textura.getWidth() * escala;
            n.altura = n.textura.getHeight() * escala;
            n.x0 = aleatorio.nextFloat() * (larguraTela + 2 * n.largura);
            n.y0 = aleatorio.nextFloat() * (alturaTela + 2 * n.altura);
            n.vx = 10f + aleatorio.nextFloat() * 14f;          // deriva lateral (px/s)
            n.parallax = 0.15f + aleatorio.nextFloat() * 0.2f; // desce mais devagar que a estrada
            nuvens.add(n);
        }
    }

    private Color corFolha() {
        if (fase == 1) return new Color(0.95f, 0.62f, 0.18f, 0.95f);                 // outono na cidade
        float r = aleatorio.nextFloat();                                              // floresta: verdes e dourados
        if (r < 0.55f) return new Color(0.45f, 0.78f, 0.28f, 0.95f);
        if (r < 0.80f) return new Color(0.85f, 0.75f, 0.25f, 0.95f);
        return new Color(0.80f, 0.40f, 0.18f, 0.95f);
    }

    private void gerarParticulas(int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            Particula p = new Particula();
            p.x = aleatorio.nextFloat() * larguraTela;
            p.y = aleatorio.nextFloat() * alturaTela;
            if (fase == 3) {          // neve: bem leve, cai devagar e quase sem girar
                p.queda = 20f + aleatorio.nextFloat() * 30f;
                p.amplitude = 8f + aleatorio.nextFloat() * 16f;
                p.escala = 0.5f + aleatorio.nextFloat() * 0.8f;
                p.giro = 0f;
                p.cor = new Color(1f, 1f, 1f, 0.95f);
            } else {                  // folhas
                p.queda = 28f + aleatorio.nextFloat() * 40f;
                p.amplitude = 14f + aleatorio.nextFloat() * 26f;
                p.escala = 0.7f + aleatorio.nextFloat() * 0.7f;
                p.giro = (aleatorio.nextFloat() - 0.5f) * 180f;
                p.cor = corFolha();
            }
            p.frequencia = 0.8f + aleatorio.nextFloat() * 1.4f;
            p.fase = aleatorio.nextFloat() * 6.283f;
            p.rotacao = aleatorio.nextFloat() * 360f;
            particulas.add(p);
        }
    }

    private void gerarRaios() {
        float[] posicoes = {120f, 440f, 740f};
        for (float x : posicoes) {
            Raio r = new Raio();
            r.x0 = x + (aleatorio.nextFloat() - 0.5f) * 80f;
            r.largura = 240f + aleatorio.nextFloat() * 120f;
            r.fase = aleatorio.nextFloat() * 6.283f;
            raios.add(r);
        }
    }

    private void gerarVagaLumes(int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            VagaLume v = new VagaLume();
            v.x0 = aleatorio.nextFloat() * larguraTela;
            v.y0 = aleatorio.nextFloat() * alturaTela;
            v.raioX = 20f + aleatorio.nextFloat() * 40f;
            v.raioY = 12f + aleatorio.nextFloat() * 26f;
            v.frequencia = 0.5f + aleatorio.nextFloat() * 0.9f;
            v.fase = aleatorio.nextFloat() * 6.283f;
            v.escala = 0.7f + aleatorio.nextFloat() * 0.7f;
            vagaLumes.add(v);
        }
    }

    private void criarBando() {
        int quantidade = 1 + aleatorio.nextInt(3);
        int direcao = aleatorio.nextBoolean() ? 1 : -1;
        float baseY = 250f + aleatorio.nextFloat() * 400f;
        float vx = direcao * (150f + aleatorio.nextFloat() * 80f);
        for (int i = 0; i < quantidade; i++) {
            Passaro p = new Passaro();
            p.vx = vx * (0.95f + aleatorio.nextFloat() * 0.1f);
            p.x = (direcao > 0 ? -60f : larguraTela + 60f) - direcao * i * 48f;
            p.y = baseY + (i % 2 == 0 ? 1 : -1) * i * 24f;
            p.fase = aleatorio.nextFloat() * 6.283f;
            bando.add(p);
        }
    }

    /** Chame a cada quadro (ate no menu), exceto quando o jogo estiver pausado. */
    public void atualizar(float delta) {
        tempo += delta;

        esperaPassaro -= delta;
        if (esperaPassaro <= 0) {
            criarBando();
            esperaPassaro = 5f + aleatorio.nextFloat() * 6f;
        }
        for (int i = bando.size() - 1; i >= 0; i--) {
            Passaro p = bando.get(i);
            p.x += p.vx * delta;
            if (p.x < -120f || p.x > larguraTela + 120f) bando.remove(i);
        }

        for (Particula p : particulas) {
            p.y -= p.queda * delta;
            p.rotacao += p.giro * delta;
            if (p.y < -20f) { p.y = alturaTela + 20f; p.x = aleatorio.nextFloat() * larguraTela; }
        }
    }

    private static float modulo(float a, float b) { return ((a % b) + b) % b; }

    /** Chame dentro de lote.begin()/end(), antes de obstaculos e jogador. */
    public void desenhar(SpriteBatch lote, float progresso) {
        float h = chao.getHeight();
        float y = -(progresso % h);
        // Duas copias do tile, uma sobre a outra com 1 px de sobreposicao (evita linha entre elas).
        lote.draw(chao, 0, y, larguraTela, h);
        lote.draw(chao, 0, y + h - 1f, larguraTela, h);

        // De tras para frente: itens mais abaixo na tela ficam por cima dos mais altos.
        for (int i = itens.size() - 1; i >= 0; i--) {
            Item it = itens.get(i);
            float yTela = it.yMundo - progresso;
            if (yTela > alturaTela || yTela + it.altura < 0) continue;
            if (it.balanca) {
                // Vento: onda que viaja pelo mapa (depende da posicao no mundo), ate +-3 graus.
                float angulo = 3f * (float) Math.sin(tempo * 1.6f + it.yMundo * 0.006f + it.x * 0.01f);
                lote.draw(it.textura, it.x, yTela, it.largura / 2f, it.altura * 0.12f, it.largura, it.altura,
                    1f, 1f, angulo, 0, 0, it.textura.getWidth(), it.textura.getHeight(), false, false);
            } else {
                lote.draw(it.textura, it.x, yTela, it.largura, it.altura);
            }
        }
    }

    /** Chame depois de obstaculos e jogador (antes do HUD): nuvens, raios, passaros e particulas. */
    public void desenharFrente(SpriteBatch lote, float progresso) {
        // Sombras de nuvens: derivam para o lado e descem mais devagar que a estrada (parallax).
        for (Nuvem n : nuvens) {
            float x = modulo(n.x0 + n.vx * tempo, larguraTela + 2 * n.largura) - n.largura;
            float y = modulo(n.y0 - progresso * n.parallax, alturaTela + 2 * n.altura) - n.altura;
            lote.draw(n.textura, x, y, n.largura, n.altura);
        }

        // Floresta: raios de luz que pulsam suavemente entre as copas.
        if (texRaio != null) {
            for (Raio r : raios) {
                float alfa = 0.20f + 0.11f * (float) Math.sin(tempo * 0.55f + r.fase);
                float x = r.x0 + 28f * (float) Math.sin(tempo * 0.22f + r.fase * 2f);
                lote.setColor(1f, 1f, 1f, alfa);
                lote.draw(texRaio, x, 0f, r.largura, alturaTela);
            }
            lote.setColor(Color.WHITE);
        }

        // Passaros: sombra no chao (deslocada) e o passaro no ar, batendo asas.
        boolean asasAbertas = ((int) (tempo * 9f)) % 2 == 0;
        Texture quadro = asasAbertas ? passaro1 : passaro2;
        for (Passaro p : bando) {
            float oscilacao = 6f * (float) Math.sin(tempo * 3f + p.fase);
            boolean paraEsquerda = p.vx < 0;
            lote.setColor(0f, 0f, 0f, 0.22f);
            lote.draw(quadro, p.x + 30f, p.y - 55f + oscilacao, 0, 0, quadro.getWidth(), quadro.getHeight(),
                0.9f, 0.9f, 0f, 0, 0, quadro.getWidth(), quadro.getHeight(), paraEsquerda, false);
            lote.setColor(corPassaro);
            lote.draw(quadro, p.x, p.y + oscilacao, 0, 0, quadro.getWidth(), quadro.getHeight(),
                1.2f, 1.2f, 0f, 0, 0, quadro.getWidth(), quadro.getHeight(), paraEsquerda, false);
        }

        // Folhas (cidade/floresta) ou neve (Alpes), balancando de um lado para o outro.
        float w = texParticula.getWidth(), hh = texParticula.getHeight();
        for (Particula p : particulas) {
            float x = p.x + p.amplitude * (float) Math.sin(tempo * p.frequencia + p.fase);
            if (fase == 3) {
                // Halo azul bem fraco: o floco continua visivel sobre a neve branca.
                lote.setColor(0.55f, 0.68f, 0.88f, 0.40f);
                lote.draw(texParticula, x, p.y, w / 2f, hh / 2f, w, hh, p.escala * 1.7f, p.escala * 1.7f, 0f,
                    0, 0, texParticula.getWidth(), texParticula.getHeight(), false, false);
            }
            lote.setColor(p.cor);
            lote.draw(texParticula, x, p.y, w / 2f, hh / 2f, w, hh, p.escala, p.escala, p.rotacao,
                0, 0, texParticula.getWidth(), texParticula.getHeight(), false, false);
        }

        // Floresta: vaga-lumes que vagam e piscam.
        if (texVagaLume != null) {
            float vw = texVagaLume.getWidth(), vh = texVagaLume.getHeight();
            for (VagaLume v : vagaLumes) {
                float x = v.x0 + v.raioX * (float) Math.sin(tempo * v.frequencia + v.fase);
                float y = modulo(v.y0 - progresso * 0.5f, alturaTela + 60f) - 30f
                    + v.raioY * (float) Math.cos(tempo * v.frequencia * 0.7f + v.fase);
                float brilho = 0.5f + 0.5f * (float) Math.sin(tempo * 2.2f + v.fase * 3f);
                lote.setColor(1f, 0.95f, 0.55f, 0.9f * brilho);
                lote.draw(texVagaLume, x, y, vw / 2f, vh / 2f, vw, vh, v.escala, v.escala, 0f,
                    0, 0, texVagaLume.getWidth(), texVagaLume.getHeight(), false, false);
            }
        }
        lote.setColor(Color.WHITE);
    }

    public void dispose() {
        chao.dispose();
        for (Texture t : texturas.values()) t.dispose();
        texturas.clear();
        itens.clear(); nuvens.clear(); bando.clear(); particulas.clear(); raios.clear(); vagaLumes.clear();
    }
}
