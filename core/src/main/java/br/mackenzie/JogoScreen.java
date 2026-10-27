package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.OrthographicCamera;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JogoScreen implements Screen {
    private final float largura = 960, altura = 720;
    private SpriteBatch lote;
    private BitmapFont fonte;
    private ShapeRenderer formas;
    private OrthographicCamera camera;
    private FitViewport tela;
    private FundoRolante fundo;
    private Caramelo caramelo;
    private ConfiguracaoFase configuracao;
    private final List<Obstaculo> obstaculos = new ArrayList<Obstaculo>();
    private final List<Coletavel> ossinhos = new ArrayList<Coletavel>();
    private final List<Float> posicoesObstaculos = new ArrayList<Float>();
    private final Random aleatorio = new Random();
    private int fase = 1, pontos = 0, vidas = 3, obstaculosSuperados = 0, obstaculosEncontrados = 0;
    private float progresso = 0, distanciaFase = 2600, invulneravel = 0, tempoOssinho = 0;
    private String estado = "menu";
    // Menu inicial com imagens (assets/menu). As areas sao calculadas em montarLayoutMenu().
    private Texture texPainel, texJogar, texInstrucoes, texSair, texVoltar;
    private final Rectangle painelMenu = new Rectangle();
    private final Rectangle botaoJogar = new Rectangle();
    private final Rectangle botaoInstrucoes = new Rectangle();
    private final Rectangle botaoSair = new Rectangle();
    private final Rectangle botaoVoltar = new Rectangle();
    private final Vector2 mouse = new Vector2();
    private int botaoPressionado = -1; // 0 = jogar, 1 = instrucoes, 2 = sair (no menu); 0 = continuar, 1 = tentar novamente, 2 = voltar ao menu (na pausa)
    // Menu de pausa com imagens (assets/menu)
    private Texture texPausaPainel, texContinuar, texTentar, texVoltarMenu;
    private final Rectangle painelPausa = new Rectangle();
    private final Rectangle botaoContinuar = new Rectangle();
    private final Rectangle botaoTentar = new Rectangle();
    private final Rectangle botaoVoltarMenu = new Rectangle();
    // Tela de fim de jogo com imagens (assets/menu) e fonte bonita (assets/fonte)
    private Texture texFimPainel;
    private Texture texHud, texCoracaoCheio, texCoracaoVazio, texOsso; // HUD durante o jogo (assets/hud)
    private BitmapFont fonteBonita;
    private final GlyphLayout medidaTexto = new GlyphLayout();
    private final Rectangle painelFim = new Rectangle();
    private final Rectangle botaoTentarFim = new Rectangle();
    private final Rectangle botaoVoltarMenuFim = new Rectangle();
    private float topoPergaminhoFim;
    private String telaAnterior = "menu";
    private ControleIoT controle; // sensor ESP32 + MPU6050 (alternativa ao teclado)

    public void show() {
        camera = new OrthographicCamera(); tela = new FitViewport(largura, altura, camera);
        lote = new SpriteBatch(); fonte = new BitmapFont(); formas = new ShapeRenderer();
        texPainel = carregarImagemMenu("menu/menu_painel.png");
        texJogar = carregarImagemMenu("menu/botao_jogar.png");
        texInstrucoes = carregarImagemMenu("menu/botao_instrucoes.png");
        texSair = carregarImagemMenu("menu/botao_sair.png");
        texVoltar = carregarImagemMenu("menu/botao_voltar.png");
        texPausaPainel = carregarImagemMenu("menu/pausa_painel.png");
        texContinuar = carregarImagemMenu("menu/botao_continuar.png");
        texTentar = carregarImagemMenu("menu/botao_tentar_novamente.png");
        texVoltarMenu = carregarImagemMenu("menu/botao_voltar_menu.png");
        montarLayoutMenu();
        montarLayoutPausa();
        texFimPainel = carregarImagemMenu("menu/fim_painel.png");
        fonteBonita = new BitmapFont(Gdx.files.internal("fonte/fonte_caramelo.fnt"));
        fonteBonita.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        montarLayoutFim();
        texHud = carregarImagemMenu("hud/hud_tabua.png");
        texCoracaoCheio = carregarImagemMenu("hud/hud_coracao_cheio.png");
        texCoracaoVazio = carregarImagemMenu("hud/hud_coracao_vazio.png");
        texOsso = carregarImagemMenu("ossinho.png");
        caramelo = new Caramelo(); carregarFase();
        controle = new ControleIoT(); controle.iniciar();
    }
    private Texture carregarImagemMenu(String arquivo) {
        Texture t = new Texture(Gdx.files.internal(arquivo));
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return t;
    }
    private void carregarFase() {
        if (fundo != null) fundo.dispose();
        if (fase == 1) configuracao = new FaseCidade(); else if (fase == 2) configuracao = new FaseFloresta(); else configuracao = new FaseMontanhas();
        limparObjetos(); progresso = 0; tempoOssinho = 0; invulneravel = 0;
        distanciaFase = configuracao.getDistancia();
        fundo = new FundoRolante(fase, largura, altura, distanciaFase);
        caramelo.moverPara(430);
        criarMapa();
    }
    private void criarMapa() {
        String[] tipos = configuracao.getTiposObstaculos();
        // Quanto menor o espacamento da fase, mais obstaculos (a fase fica mais dificil).
        float espaco = configuracao.getEspacamentoObstaculos();
        int quantidade = (int) ((distanciaFase + altura - 420) / espaco);
        // Cada grupo fica em uma faixa fixa do mapa; a tela avanca sobre o mapa.
        for (int i=0; i<quantidade; i++) {
            float y = 420 + i * espaco;
            String arquivo = tipos[aleatorio.nextInt(tipos.length)];
            float w=54,h=54;
            if (arquivo.equals("tronco.png")) { w=105; h=48; }
            if (arquivo.equals("caixa.png")) { w=58; h=55; }
            if (arquivo.equals("bueiro_aberto.png")) { w=78; h=50; }
            // A estrada e mais estreita no topo e mais larga perto do jogador.
            float limiteEsquerdo = limiteEsquerdoEstrada(y) + 18;
            float limiteDireito = limiteDireitoEstrada(y) - w - 18;
            float x = limiteEsquerdo + aleatorio.nextFloat() * Math.max(1, limiteDireito - limiteEsquerdo);
            obstaculos.add(new Obstaculo(arquivo, x, y, w, h));
            posicoesObstaculos.add(y);
            obstaculosEncontrados++;
        }
    }
    public void render(float delta) {
        delta=Math.min(delta,0.05f); tela.apply(); camera.update(); atualizarMouse(); lote.setProjectionMatrix(camera.combined); formas.setProjectionMatrix(camera.combined);
        if (estado.equals("jogando")) atualizar(delta);
        if (fundo != null && !estado.equals("pausa")) fundo.atualizar(delta);
        desenhar(); tratarInterface();
    }
    private void atualizar(float delta) {
        caramelo.atualizar(delta, configuracao.getVelocidade() / 190f);
        // Direcao: -1 esquerda, +1 direita. Teclado tem prioridade; sem tecla, vale o sensor.
        float direcao = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) direcao -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) direcao += 1;
        boolean teclado = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        if (!teclado && controle != null) direcao = controle.getDirecao();
        if (direcao != 0) caramelo.moverPara(caramelo.getSprite().getX()+direcao*configuracao.getVelocidadeCaramelo()*delta);
        invulneravel=Math.max(0,invulneravel-delta);
        progresso += configuracao.getVelocidade()*delta*0.55f;
        tempoOssinho += delta;
        if (tempoOssinho > 4f) {
            criarOssinhoSemObstaculo();
            tempoOssinho=0;
        }
        for (int i=obstaculos.size()-1;i>=0;i--) {
            Obstaculo o=obstaculos.get(i); float y=o.getPosicaoOriginalY()-progresso;
            o.getSprite().setY(y);
            if (y < -90) { o.dispose(); obstaculos.remove(i); obstaculosSuperados++; pontos+=5; }
            else if (invulneravel<=0 && caramelo.getSprite().getBoundingRectangle().overlaps(o.getSprite().getBoundingRectangle())) { o.dispose(); obstaculos.remove(i); vidas--; invulneravel=1.5f; if (vidas<=0) estado="fim"; }
        }
        for (int i=ossinhos.size()-1;i>=0;i--) {
            Coletavel o=ossinhos.get(i); o.getSprite().setY(o.getSprite().getY()-configuracao.getVelocidade()*delta*0.55f);
            if (caramelo.getSprite().getBoundingRectangle().overlaps(o.getSprite().getBoundingRectangle())) { pontos+=10; o.dispose(); ossinhos.remove(i); }
            else if (o.getSprite().getY() < -40) { o.dispose(); ossinhos.remove(i); }
        }
        if (progresso >= distanciaFase) {
            if (fase<3) { fase++; carregarFase(); } else estado="conclusao";
        }
    }
        // Agora a estrada e reta (o fundo rola), entao os limites sao fixos.
    private float limiteEsquerdoEstrada(float y) { return FundoRolante.ESTRADA_ESQUERDA; }
    private float limiteDireitoEstrada(float y) { return FundoRolante.ESTRADA_DIREITA; }
    private void criarOssinhoSemObstaculo() {
        float y = altura + progresso + 30;
        float larguraOssinho = 28;
        float minimoX = limiteEsquerdoEstrada(altura - 10) + 20;
        float maximoX = limiteDireitoEstrada(altura - 10) - larguraOssinho - 20;
        for (int tentativa = 0; tentativa < 20; tentativa++) {
            float x = minimoX + aleatorio.nextFloat() * Math.max(1, maximoX - minimoX);
            boolean livre = true;
            // Compara as areas no mapa, antes de criar o ossinho.
            Rectangle areaOssinho = new Rectangle(x, y, larguraOssinho, larguraOssinho);
            for (Obstaculo obstaculo : obstaculos) {
                Rectangle areaObstaculo = new Rectangle(
                    obstaculo.getSprite().getX(),
                    obstaculo.getPosicaoOriginalY(),
                    obstaculo.getSprite().getWidth(),
                    obstaculo.getSprite().getHeight()
                );
                // Deixa uma margem extra para o ossinho nao ficar escondido atras do obstaculo.
                areaObstaculo.set(areaObstaculo.x - 24, areaObstaculo.y - 42,
                    areaObstaculo.width + 48, areaObstaculo.height + 84);
                if (areaOssinho.overlaps(areaObstaculo)) {
                    livre = false;
                    break;
                }
            }
            for (Coletavel existente : ossinhos) {
                Rectangle areaExistente = existente.getSprite().getBoundingRectangle();
                areaExistente.set(areaExistente.x - 18, areaExistente.y - 35,
                    areaExistente.width + 36, areaExistente.height + 70);
                if (areaOssinho.overlaps(areaExistente)) {
                    livre = false;
                    break;
                }
            }
            if (livre) {
                ossinhos.add(new Coletavel(x, y));
                return;
            }
        }
        // Se nao houver espaco seguro, pula esta geracao em vez de esconder o ossinho.
    }

    private void desenhar() {
        Gdx.gl.glClearColor(0.95f,0.89f,0.78f,1); Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        lote.begin();
        if (fundo!=null) {
            // O fundo rola junto com os obstaculos (mesmo "progresso").
            fundo.desenhar(lote, progresso);
        }
        for (Obstaculo o:obstaculos) if (o.getSprite().getY()>-100 && o.getSprite().getY()<altura+80) o.getSprite().draw(lote);
        for (Coletavel o:ossinhos) o.getSprite().draw(lote);
        if (caramelo!=null) caramelo.getSprite().draw(lote);
        if (fundo!=null) fundo.desenharFrente(lote, progresso);
        lote.end();
        if (estado.equals("jogando")) {
            desenharHud();
        } else if (estado.equals("menu")) {
            escurecerTela(0.35f);
            lote.begin();
            lote.draw(texPainel, painelMenu.x, painelMenu.y, painelMenu.width, painelMenu.height);
            desenharBotaoImagem(texJogar, botaoJogar, 0);
            desenharBotaoImagem(texInstrucoes, botaoInstrucoes, 1);
            desenharBotaoImagem(texSair, botaoSair, 2);
            lote.end();
        } else if (estado.equals("instrucoes")) {
            escurecerTela(0.35f);
            lote.begin();
            lote.draw(texPainel, painelMenu.x, painelMenu.y, painelMenu.width, painelMenu.height);
            float cx = painelMenu.x + painelMenu.width / 2f, topo = topoPergaminho();
            Color creme = new Color(1f, 0.96f, 0.82f, 1f);
            textoBonito("COMO JOGAR", cx, topo - 6, 0.9f, new Color(1f, 0.78f, 0.25f, 1f));
            textoBonito("Incline o controle para mover o Caramelo.", cx, topo - 78, 0.38f, creme);
            textoBonito("Desvie dos cones, caixas, troncos e bueiros.", cx, topo - 108, 0.38f, creme);
            textoBonito("Pegue os ossinhos para ganhar pontos.", cx, topo - 138, 0.38f, creme);
            textoBonito("A cada fase o jogo fica mais rápido!", cx, topo - 168, 0.38f, creme);
            desenharBotaoImagem(texVoltar, botaoVoltar, 0);
            lote.end();
        } else if (estado.equals("pausa")) {
            escurecerTela(0.35f);
            lote.begin();
            lote.draw(texPausaPainel, painelPausa.x, painelPausa.y, painelPausa.width, painelPausa.height);
            desenharBotaoImagem(texContinuar, botaoContinuar, 0);
            desenharBotaoImagem(texTentar, botaoTentar, 1);
            desenharBotaoImagem(texVoltarMenu, botaoVoltarMenu, 2);
            lote.end();
        } else if (estado.equals("fim")) {
            escurecerTela(0.35f);
            lote.begin();
            lote.draw(texFimPainel, painelFim.x, painelFim.y, painelFim.width, painelFim.height);
            desenharPontuacao(painelFim.x + painelFim.width / 2f, topoPergaminhoFim - 12, topoPergaminhoFim - 58);
            desenharBotaoImagem(texTentar, botaoTentarFim, 0);
            desenharBotaoImagem(texVoltarMenu, botaoVoltarMenuFim, 1);
            lote.end();
        } else if (estado.equals("conclusao")) {
            painel(220,150,520,430); botaoVisual(new Rectangle(350,290,260,50)); botaoVisual(new Rectangle(350,220,260,50)); lote.begin();
            textoCentral("FASE CONCLUIDA!",480,510,1.8f);
            desenharPontuacao(480, 480, 430);
            desenharBotao(new Rectangle(350,290,260,50),"TENTAR NOVAMENTE"); desenharBotao(new Rectangle(350,220,260,50),"VOLTAR AO MENU");
            lote.end();
        }
    }
    private void desenharBotao(Rectangle r,String texto) {
        fonte.setColor(Color.WHITE); fonte.getData().setScale(1.05f); fonte.draw(lote,texto,r.x+r.width/2-texto.length()*5.2f,r.y+r.height/2+5); fonte.getData().setScale(1f);
    }
    private void botaoVisual(Rectangle r) {
        formas.begin(ShapeRenderer.ShapeType.Filled); formas.setColor(new Color(0.48f,0.30f,0.18f,1)); formas.rect(r.x,r.y,r.width,r.height); formas.end();
    }
    private void textoCentral(String s,float x,float y,float escala) { textoCentral(s,x,y,escala,Color.WHITE); }
    private void textoCentral(String s,float x,float y,float escala,Color cor) { fonte.setColor(cor); fonte.getData().setScale(escala); fonte.draw(lote,s,x-s.length()*4.5f*escala,y); fonte.getData().setScale(1); fonte.setColor(Color.WHITE); }
    private static final Color COR_TEXTO = new Color(0.28f,0.17f,0.10f,1);

    // ---------- Menu com imagens ----------
    private static final float ESCALA_PAINEL = 1.55f;   // painel 417x450 -> ~646x698
    private static final float ESCALA_BOTAO = 0.70f;    // botoes ficam ~170-180 px de largura
    private static final int PERGAMINHO_TOPO_PX = 206;  // limites do pergaminho dentro da imagem (a partir do topo)
    private static final int PERGAMINHO_BASE_PX = 378;
    private float topoPergaminho() { return painelMenu.y + (texPainel.getHeight() - PERGAMINHO_TOPO_PX) * ESCALA_PAINEL; }
    private float basePergaminho() { return painelMenu.y + (texPainel.getHeight() - PERGAMINHO_BASE_PX) * ESCALA_PAINEL; }
    private void montarLayoutMenu() {
        float pw = texPainel.getWidth() * ESCALA_PAINEL, ph = texPainel.getHeight() * ESCALA_PAINEL;
        painelMenu.set((largura - pw) / 2f, (altura - ph) / 2f, pw, ph);
        float cx = painelMenu.x + pw / 2f;
        float hJ = texJogar.getHeight() * ESCALA_BOTAO, hI = texInstrucoes.getHeight() * ESCALA_BOTAO, hS = texSair.getHeight() * ESCALA_BOTAO;
        float folga = ((topoPergaminho() - basePergaminho()) - (hJ + hI + hS)) / 4f;
        float y = topoPergaminho() - folga - hJ;
        botaoJogar.set(cx - texJogar.getWidth() * ESCALA_BOTAO / 2f, y, texJogar.getWidth() * ESCALA_BOTAO, hJ);
        y -= folga + hI;
        botaoInstrucoes.set(cx - texInstrucoes.getWidth() * ESCALA_BOTAO / 2f, y, texInstrucoes.getWidth() * ESCALA_BOTAO, hI);
        y -= folga + hS;
        botaoSair.set(cx - texSair.getWidth() * ESCALA_BOTAO / 2f, y, texSair.getWidth() * ESCALA_BOTAO, hS);
        float wVoltar = 160, hVoltar = wVoltar * texVoltar.getHeight() / texVoltar.getWidth();
        botaoVoltar.set(cx - wVoltar / 2f, basePergaminho() + 2, wVoltar, hVoltar);
    }
    private void atualizarMouse() { mouse.set(Gdx.input.getX(), Gdx.input.getY()); tela.unproject(mouse); }
    private void escurecerTela(float alfa) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        formas.begin(ShapeRenderer.ShapeType.Filled); formas.setColor(0, 0, 0, alfa); formas.rect(0, 0, largura, altura); formas.end();
    }
    /** Desenha o botao com efeito: normal levemente escuro, hover maior e brilhante, pressionado menor e escuro. */
    private void desenharBotaoImagem(Texture tex, Rectangle r, int indice) {
        boolean sobre = r.contains(mouse.x, mouse.y);
        boolean pressionado = sobre && botaoPressionado == indice && Gdx.input.isTouched();
        float escala = pressionado ? 0.95f : sobre ? 1.08f : 1f;
        float brilho = pressionado ? 0.75f : sobre ? 1f : 0.92f;
        float w = r.width * escala, h = r.height * escala;
        lote.setColor(brilho, brilho, brilho, 1);
        lote.draw(tex, r.x + (r.width - w) / 2f, r.y + (r.height - h) / 2f, w, h);
        lote.setColor(Color.WHITE);
    }
    // ---------- Menu de pausa com imagens ----------
    private float escalaPainelPausa = 1f; // calculada para a pausa ter a mesma largura do menu inicial
    private static final float ESCALA_BOTAO_PAUSA = 0.55f;  // botoes ficam ~210 px de largura
    private static final int PAUSA_PERGAMINHO_TOPO_PX = 398; // limites do pergaminho dentro da imagem (a partir do topo)
    private static final int PAUSA_PERGAMINHO_BASE_PX = 698;
    private void montarLayoutPausa() {
        escalaPainelPausa = texPainel.getWidth() * ESCALA_PAINEL / texPausaPainel.getWidth();
        float pw = texPausaPainel.getWidth() * escalaPainelPausa, ph = texPausaPainel.getHeight() * escalaPainelPausa;
        painelPausa.set((largura - pw) / 2f, (altura - ph) / 2f, pw, ph);
        float topo = painelPausa.y + (texPausaPainel.getHeight() - PAUSA_PERGAMINHO_TOPO_PX) * escalaPainelPausa;
        float base = painelPausa.y + (texPausaPainel.getHeight() - PAUSA_PERGAMINHO_BASE_PX) * escalaPainelPausa;
        float cx = painelPausa.x + pw / 2f;
        Texture[] tex = { texContinuar, texTentar, texVoltarMenu };
        Rectangle[] areas = { botaoContinuar, botaoTentar, botaoVoltarMenu };
        float total = 0; for (Texture t : tex) total += t.getHeight() * ESCALA_BOTAO_PAUSA;
        float folga = ((topo - base) - total) / 4f;
        float y = topo - folga;
        for (int i = 0; i < tex.length; i++) {
            float w = tex[i].getWidth() * ESCALA_BOTAO_PAUSA, h = tex[i].getHeight() * ESCALA_BOTAO_PAUSA;
            y -= h; areas[i].set(cx - w / 2f, y, w, h); y -= folga;
        }
    }
    private int botaoPausaNaPosicao(float x, float y) {
        if (botaoContinuar.contains(x, y)) return 0;
        if (botaoTentar.contains(x, y)) return 1;
        if (botaoVoltarMenu.contains(x, y)) return 2;
        return -1;
    }
    /** Mesma regra do menu: o clique so vale ao soltar em cima do botao em que foi pressionado. */
    private void tratarPausa() {
        if (Gdx.input.justTouched()) botaoPressionado = botaoPausaNaPosicao(mouse.x, mouse.y);
        else if (!Gdx.input.isTouched() && botaoPressionado >= 0) {
            int pressionado = botaoPressionado; botaoPressionado = -1;
            if (botaoPausaNaPosicao(mouse.x, mouse.y) == pressionado) {
                if (pressionado == 0) estado = "jogando";
                else if (pressionado == 1) reiniciarJogo();
                else estado = "menu";
            }
        }
    }
    // ---------- HUD durante o jogo ----------
    private static final float HUD_ESCALA = 0.5f;      // tabua 592x276 (com folhas) -> ~296x138
    private static final float HUD_MARGEM_PX = 26;     // margem transparente da imagem (onde ficam as folhas)
    private static final float HUD_LARGURA = 270, HUD_ALTURA = 112; // area da tabua em si
    private static final int VIDAS_MAXIMAS = 3;
    private void desenharHud() {
        float m = HUD_MARGEM_PX * HUD_ESCALA, x = 14, topo = altura - 14;
        lote.begin();
        lote.draw(texHud, x - m, topo - HUD_ALTURA - m, texHud.getWidth() * HUD_ESCALA, texHud.getHeight() * HUD_ESCALA);
        // linha 1: ossinho + pontos
        lote.draw(texOsso, x + 18, topo - 41, 30, 28);
        textoEsquerda("Pontos: " + pontos, x + 56, topo - 8, 0.52f, new Color(1f, 0.82f, 0.3f, 1f));
        // linha 2: coracoes (vidas)
        for (int i = 0; i < VIDAS_MAXIMAS; i++)
            lote.draw(i < vidas ? texCoracaoCheio : texCoracaoVazio, x + 18 + i * 31, topo - 74, 28, 28);
        // linha 3: fase
        textoEsquerda("Fase " + fase + " - " + nomeFase(), x + 18, topo - 78, 0.36f, new Color(1f, 0.96f, 0.82f, 1f));
        // dica de teclado no canto direito
        medidaTexto.setText(fonteBonita, "ESC: pausar");
        float larg = medidaTexto.width * 0.4f;
        textoEsquerda("ESC: pausar", largura - 16 - larg, altura - 12, 0.4f, new Color(1f, 0.96f, 0.82f, 1f));
        if (controle != null) { // status do sensor IoT
            String statusIoT = "C: calibrar | " + controle.getTextoStatus();
            medidaTexto.setText(fonteBonita, statusIoT);
            textoEsquerda(statusIoT, largura - 16 - medidaTexto.width * 0.4f, altura - 36, 0.4f, new Color(1f, 0.96f, 0.82f, 1f));
        }
        lote.end();
    }
    private void textoEsquerda(String texto, float x, float topo, float escala, Color cor) {
        fonteBonita.getData().setScale(escala); fonteBonita.setColor(cor);
        fonteBonita.draw(lote, texto, x, topo);
        fonteBonita.setColor(Color.WHITE); fonteBonita.getData().setScale(1f);
    }

    // ---------- Fim de jogo com imagens e fonte bonita ----------
    private static final float ESCALA_BOTAO_FIM = 0.48f;   // botoes ficam ~180 px de largura (igual ao menu inicial)
    private static final int FIM_PERGAMINHO_TOPO_PX = 385;  // limites do pergaminho dentro da imagem (a partir do topo)
    private static final int FIM_PERGAMINHO_BASE_PX = 685;
    private void montarLayoutFim() {
        float escala = texPainel.getWidth() * ESCALA_PAINEL / texFimPainel.getWidth(); // mesma largura do menu inicial
        float pw = texFimPainel.getWidth() * escala, ph = texFimPainel.getHeight() * escala;
        painelFim.set((largura - pw) / 2f, (altura - ph) / 2f, pw, ph);
        topoPergaminhoFim = painelFim.y + (texFimPainel.getHeight() - FIM_PERGAMINHO_TOPO_PX) * escala;
        float base = painelFim.y + (texFimPainel.getHeight() - FIM_PERGAMINHO_BASE_PX) * escala;
        float cx = painelFim.x + pw / 2f;
        float wT = texTentar.getWidth() * ESCALA_BOTAO_FIM, hT = texTentar.getHeight() * ESCALA_BOTAO_FIM;
        float wV = texVoltarMenu.getWidth() * ESCALA_BOTAO_FIM, hV = texVoltarMenu.getHeight() * ESCALA_BOTAO_FIM;
        botaoVoltarMenuFim.set(cx - wV / 2f, base + 4, wV, hV);
        botaoTentarFim.set(cx - wT / 2f, base + 4 + hV + 2, wT, hT);
    }
    /** Pontuacao e obstaculos superados com a fonte do jogo. y1 e y2 sao o topo de cada linha. */
    private void desenharPontuacao(float cx, float y1, float y2) {
        float taxa = obstaculosEncontrados == 0 ? 0 : 100f * obstaculosSuperados / obstaculosEncontrados;
        textoBonito("Pontuação: " + pontos, cx, y1, 0.62f, new Color(1f, 0.78f, 0.25f, 1f));
        textoBonito("Obstáculos superados: " + obstaculosSuperados + " (" + Math.round(taxa) + "%)", cx, y2, 0.45f, new Color(1f, 0.96f, 0.82f, 1f));
    }
    private void textoBonito(String texto, float cx, float topo, float escala, Color cor) {
        fonteBonita.getData().setScale(escala); fonteBonita.setColor(cor);
        medidaTexto.setText(fonteBonita, texto);
        fonteBonita.draw(lote, texto, cx - medidaTexto.width / 2f, topo);
        fonteBonita.setColor(Color.WHITE); fonteBonita.getData().setScale(1f);
    }
    /** Botao VOLTAR das instrucoes: o clique so vale ao soltar em cima dele. */
    private void tratarInstrucoes() {
        if (Gdx.input.justTouched()) botaoPressionado = botaoVoltar.contains(mouse.x, mouse.y) ? 0 : -1;
        else if (!Gdx.input.isTouched() && botaoPressionado >= 0) {
            botaoPressionado = -1;
            if (botaoVoltar.contains(mouse.x, mouse.y)) estado = "menu";
        }
    }
    private int botaoFimNaPosicao(float x, float y) {
        if (botaoTentarFim.contains(x, y)) return 0;
        if (botaoVoltarMenuFim.contains(x, y)) return 1;
        return -1;
    }
    /** Mesma regra do menu: o clique so vale ao soltar em cima do botao em que foi pressionado. */
    private void tratarFim() {
        if (Gdx.input.justTouched()) botaoPressionado = botaoFimNaPosicao(mouse.x, mouse.y);
        else if (!Gdx.input.isTouched() && botaoPressionado >= 0) {
            int pressionado = botaoPressionado; botaoPressionado = -1;
            if (botaoFimNaPosicao(mouse.x, mouse.y) == pressionado) {
                if (pressionado == 0) reiniciarJogo(); else estado = "menu";
            }
        }
    }
    private int botaoNaPosicao(float x, float y) {
        if (botaoJogar.contains(x, y)) return 0;
        if (botaoInstrucoes.contains(x, y)) return 1;
        if (botaoSair.contains(x, y)) return 2;
        return -1;
    }
    /** O clique so vale ao soltar o botao do mouse em cima do mesmo botao em que foi pressionado. */
    private void tratarMenu() {
        if (Gdx.input.justTouched()) botaoPressionado = botaoNaPosicao(mouse.x, mouse.y);
        else if (!Gdx.input.isTouched() && botaoPressionado >= 0) {
            int pressionado = botaoPressionado; botaoPressionado = -1;
            if (botaoNaPosicao(mouse.x, mouse.y) == pressionado) {
                if (pressionado == 0) reiniciarJogo();
                else if (pressionado == 1) estado = "instrucoes";
                else Gdx.app.exit();
            }
        }
    }
    private void painel(float x,float y,float w,float h) { formas.begin(ShapeRenderer.ShapeType.Filled); formas.setColor(new Color(0.28f,0.17f,0.10f,0.94f)); formas.rect(x,y,w,h); formas.setColor(new Color(0.82f,0.57f,0.31f,1)); formas.rect(x,y,8,h); formas.end(); }
    private String nomeFase() { return fase==1?"Cidade":fase==2?"Floresta":"Montanhas"; }
    private void tratarInterface() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { if (estado.equals("jogando")) estado="pausa"; else if (estado.equals("pausa")) estado="jogando"; else if (estado.equals("instrucoes")) estado="menu"; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) { if (estado.equals("menu")) { estado="jogando"; calibrarControle(); } else if (estado.equals("pausa")) estado="jogando"; else if (estado.equals("fim")||estado.equals("conclusao")) reiniciarJogo(); }
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) calibrarControle(); // recalibra o sensor
        if (estado.equals("menu")) tratarMenu();
        else if (estado.equals("pausa")) tratarPausa();
        else if (estado.equals("fim")) tratarFim();
        else if (estado.equals("instrucoes")) tratarInstrucoes();
        if (Gdx.input.justTouched()) {
            float x=mouse.x, y=mouse.y; // coordenadas do mundo (respeita as faixas pretas do FitViewport)
            if (estado.equals("conclusao")) { if (y>=290 && y<=340) reiniciarJogo(); else if (y>=220 && y<=270) estado="menu"; }
        }
    }
    private void reiniciarJogo() { fase=1; pontos=0; vidas=3; obstaculosSuperados=0; obstaculosEncontrados=0; estado="jogando"; carregarFase(); calibrarControle(); }
    private void calibrarControle() { if (controle != null) controle.iniciarCalibracao(); }
    private void limparObjetos() { for(Obstaculo o:obstaculos)o.dispose(); for(Coletavel o:ossinhos)o.dispose(); obstaculos.clear(); ossinhos.clear(); posicoesObstaculos.clear(); }
    public void resize(int w,int h) { tela.update(w,h,true); }
    public void pause() { if(estado.equals("jogando"))estado="pausa"; }
    public void resume() { }
    // Game.dispose() chama hide() (nao chama dispose() da tela), por isso a porta COM e fechada aqui tambem.
    public void hide() { if (controle != null) controle.fechar(); }
    public void dispose() { if(controle!=null)controle.fechar(); limparObjetos(); if(fundo!=null)fundo.dispose(); if(caramelo!=null)caramelo.dispose(); if(lote!=null)lote.dispose(); if(fonte!=null)fonte.dispose(); if(formas!=null)formas.dispose(); if(texPainel!=null)texPainel.dispose(); if(texJogar!=null)texJogar.dispose(); if(texInstrucoes!=null)texInstrucoes.dispose(); if(texSair!=null)texSair.dispose(); if(texVoltar!=null)texVoltar.dispose(); if(texPausaPainel!=null)texPausaPainel.dispose(); if(texContinuar!=null)texContinuar.dispose(); if(texTentar!=null)texTentar.dispose(); if(texVoltarMenu!=null)texVoltarMenu.dispose(); if(texFimPainel!=null)texFimPainel.dispose(); if(fonteBonita!=null)fonteBonita.dispose(); if(texHud!=null)texHud.dispose(); if(texCoracaoCheio!=null)texCoracaoCheio.dispose(); if(texCoracaoVazio!=null)texCoracaoVazio.dispose(); if(texOsso!=null)texOsso.dispose(); }
}