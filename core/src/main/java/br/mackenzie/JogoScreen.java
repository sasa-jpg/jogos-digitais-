package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
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
    private Texture fundo;
    private Caramelo caramelo;
    private AgenteCaptura agente;
    private ConfiguracaoFase configuracao;
    private final List<Obstaculo> obstaculos = new ArrayList<Obstaculo>();
    private final List<Coletavel> ossinhos = new ArrayList<Coletavel>();
    private final List<Float> posicoesObstaculos = new ArrayList<Float>();
    private final Random aleatorio = new Random();
    private int fase = 1, pontos = 0, vidas = 3, obstaculosSuperados = 0, obstaculosEncontrados = 0;
    private float progresso = 0, distanciaFase = 2600, invulneravel = 0, tempoOssinho = 0;
    private String estado = "menu";
    private boolean agenteContabilizado = false;
    private Rectangle botaoJogar = new Rectangle(350, 310, 260, 65);
    private Rectangle botaoInstrucoes = new Rectangle(350, 225, 260, 55);
    private Rectangle botaoSair = new Rectangle(350, 150, 260, 55);
    private String telaAnterior = "menu";

    public void show() {
        camera = new OrthographicCamera(); tela = new FitViewport(largura, altura, camera);
        lote = new SpriteBatch(); fonte = new BitmapFont(); formas = new ShapeRenderer();
        caramelo = new Caramelo(); agente = new AgenteCaptura(); carregarFase();
    }
    private void carregarFase() {
        if (fundo != null) fundo.dispose();
        if (fase == 1) configuracao = new FaseCidade(); else if (fase == 2) configuracao = new FaseFloresta(); else configuracao = new FaseMontanhas();
        fundo = new Texture(configuracao.getNomeCenario());
        limparObjetos(); progresso = 0; tempoOssinho = 0; invulneravel = 0;
        distanciaFase = fase == 1 ? 2300 : fase == 2 ? 2800 : 3300;
        caramelo.moverPara(430);
        criarMapa();
    }
    private void criarMapa() {
        String[] tipos = configuracao.getTiposObstaculos();
        int quantidade = 10 + fase * 5;
        // Cada grupo fica em uma faixa fixa do mapa; a tela avanca sobre o mapa.
        for (int i=0; i<quantidade; i++) {
            float y = 420 + i * (fase == 1 ? 145 : fase == 2 ? 125 : 108);
            float[] faixas = {185, 350, 515, 680};
            int faixa = aleatorio.nextInt(faixas.length);
            String arquivo = tipos[aleatorio.nextInt(tipos.length)];
            float w=54,h=54;
            if (arquivo.equals("tronco.png")) { w=105; h=48; }
            if (arquivo.equals("caixa.png")) { w=58; h=55; }
            if (arquivo.equals("bueiro_aberto.png")) { w=78; h=50; }
            // Alternar faixas impede uma parede completa de obstáculos.
            float x = faixas[faixa];
            obstaculos.add(new Obstaculo(arquivo, x, y, w, h));
            posicoesObstaculos.add(y);
            obstaculosEncontrados++;
        }
    }
    public void render(float delta) {
        delta=Math.min(delta,0.05f); tela.apply(); camera.update(); lote.setProjectionMatrix(camera.combined); formas.setProjectionMatrix(camera.combined);
        if (estado.equals("jogando")) atualizar(delta);
        desenhar(); tratarInterface();
    }
    private void atualizar(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) caramelo.moverPara(caramelo.getSprite().getX()-260*delta);
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) caramelo.moverPara(caramelo.getSprite().getX()+260*delta);
        invulneravel=Math.max(0,invulneravel-delta);
        progresso += configuracao.getVelocidade()*delta*0.55f;
        agente.atualizar(delta,fase);
        tempoOssinho += delta;
        if (tempoOssinho > 4f) { ossinhos.add(new Coletavel(100+aleatorio.nextInt(730), altura+progresso+30)); tempoOssinho=0; }
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
        if (agente.estaAtivo() && invulneravel<=0 && caramelo.getSprite().getBoundingRectangle().overlaps(agente.getSprite().getBoundingRectangle())) {
            vidas--; invulneravel=1.5f; agente.desativar(); if (vidas<=0) estado="fim";
        }
        if (progresso >= distanciaFase) {
            if (fase<3) { fase++; carregarFase(); } else estado="conclusao";
        }
    }
    private void desenhar() {
        Gdx.gl.glClearColor(0.95f,0.89f,0.78f,1); Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        lote.begin();
        if (fundo!=null) {
            // O fundo e uma imagem vertical unica para evitar a emenda no meio da tela.
            lote.draw(fundo, 0, -progresso, largura, fundo.getHeight());
        }
        for (Obstaculo o:obstaculos) if (o.getSprite().getY()>-100 && o.getSprite().getY()<altura+80) o.getSprite().draw(lote);
        for (Coletavel o:ossinhos) o.getSprite().draw(lote);
        if (caramelo!=null) caramelo.getSprite().draw(lote);
        if (agente!=null && estado.equals("jogando")) agente.desenhar(lote);
        lote.end();
        if (estado.equals("jogando")) {
            painel(18,605,280,94); lote.begin(); fonte.setColor(Color.WHITE); fonte.getData().setScale(1.1f);
            fonte.draw(lote,"Pontos: "+pontos,36,676); fonte.draw(lote,"Vidas: "+vidas,36,647); fonte.draw(lote,"Fase "+fase+" - "+nomeFase(),36,620);
            fonte.getData().setScale(0.85f); fonte.draw(lote,"ESC: pausar | Setas: teste provisório",620,690); lote.end();
        } else if (estado.equals("menu")) {
            painel(230,125,500,470); botaoVisual(botaoJogar); botaoVisual(botaoInstrucoes); botaoVisual(botaoSair); lote.begin(); fonte.setColor(Color.WHITE);
            textoCentral("AVENTURA DO CARAMELO",480,520,1.8f); textoCentral("Atravesse os cenarios e desvie dos obstaculos",480,470,0.95f);
            desenharBotao(botaoJogar,"JOGAR"); desenharBotao(botaoInstrucoes,"INSTRUCOES"); desenharBotao(botaoSair,"SAIR"); lote.end();
        } else if (estado.equals("instrucoes")) {
            painel(180,155,600,410); botaoVisual(new Rectangle(350,210,260,55)); lote.begin(); textoCentral("COMO JOGAR",480,505,1.8f); textoCentral("Desvie dos cones, caixas, troncos e bueiros.",480,440,0.95f); textoCentral("O agente com rede atravessa o caminho.",480,400,0.95f); textoCentral("Chegue ao final da fase para avancar.",480,360,0.95f); textoCentral("Setas sao apenas para teste; IoT ainda sera integrado.",480,315,0.8f); desenharBotao(new Rectangle(350,210,260,55),"VOLTAR"); lote.end();
        } else if (estado.equals("pausa") || estado.equals("fim") || estado.equals("conclusao")) {
            painel(220,150,520,430); if (estado.equals("pausa")) { botaoVisual(new Rectangle(350,335,260,48)); botaoVisual(new Rectangle(350,275,260,48)); botaoVisual(new Rectangle(350,215,260,48)); } else { botaoVisual(new Rectangle(350,290,260,50)); botaoVisual(new Rectangle(350,220,260,50)); } lote.begin();
            textoCentral(estado.equals("pausa")?"JOGO PAUSADO":estado.equals("fim")?"FIM DE JOGO":"FASE CONCLUIDA!",480,510,1.8f);
            textoCentral("Pontuacao: "+pontos,480,455,1.1f);
            float taxa=obstaculosEncontrados==0?0:100f*obstaculosSuperados/obstaculosEncontrados;
            textoCentral("Obstaculos superados: "+obstaculosSuperados+" ("+Math.round(taxa)+"%)",480,415,0.85f);
            if (estado.equals("pausa")) { desenharBotao(new Rectangle(350,335,260,48),"CONTINUAR"); desenharBotao(new Rectangle(350,275,260,48),"TENTAR NOVAMENTE"); desenharBotao(new Rectangle(350,215,260,48),"VOLTAR AO MENU"); }
            else { desenharBotao(new Rectangle(350,290,260,50),"TENTAR NOVAMENTE"); desenharBotao(new Rectangle(350,220,260,50),"VOLTAR AO MENU"); }
            lote.end();
        }
    }
    private void desenharBotao(Rectangle r,String texto) {
        fonte.setColor(Color.WHITE); fonte.getData().setScale(1.05f); fonte.draw(lote,texto,r.x+r.width/2-texto.length()*5.2f,r.y+r.height/2+5); fonte.getData().setScale(1f);
    }
    private void botaoVisual(Rectangle r) {
        formas.begin(ShapeRenderer.ShapeType.Filled); formas.setColor(new Color(0.48f,0.30f,0.18f,1)); formas.rect(r.x,r.y,r.width,r.height); formas.end();
    }
    private void textoCentral(String s,float x,float y,float escala) { fonte.setColor(Color.WHITE); fonte.getData().setScale(escala); fonte.draw(lote,s,x-s.length()*4.5f*escala,y); fonte.getData().setScale(1); }
    private void painel(float x,float y,float w,float h) { formas.begin(ShapeRenderer.ShapeType.Filled); formas.setColor(new Color(0.28f,0.17f,0.10f,0.94f)); formas.rect(x,y,w,h); formas.setColor(new Color(0.82f,0.57f,0.31f,1)); formas.rect(x,y,8,h); formas.end(); }
    private String nomeFase() { return fase==1?"Cidade":fase==2?"Floresta":"Montanhas"; }
    private void tratarInterface() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { if (estado.equals("jogando")) estado="pausa"; else if (estado.equals("pausa")) estado="jogando"; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) { if (estado.equals("menu")) estado="jogando"; else if (estado.equals("pausa")) estado="jogando"; else if (estado.equals("fim")||estado.equals("conclusao")) reiniciarJogo(); }
        if (Gdx.input.justTouched()) {
            float x=Gdx.input.getX()*largura/Gdx.graphics.getWidth(); float y=altura-Gdx.input.getY()*altura/Gdx.graphics.getHeight();
            if (estado.equals("menu")) { if (botaoJogar.contains(x,y)) { reiniciarJogo(); estado="jogando"; } else if (botaoInstrucoes.contains(x,y)) estado="instrucoes"; else if (botaoSair.contains(x,y)) Gdx.app.exit(); }
            else if (estado.equals("instrucoes")) { if (y>=200 && y<=275) estado="menu"; }
            else if (estado.equals("pausa")) { if (y>=335 && y<=383) estado="jogando"; else if (y>=275 && y<=323) reiniciarJogo(); else if (y>=215 && y<=263) estado="menu"; }
            else if (estado.equals("fim")||estado.equals("conclusao")) { if (y>=290 && y<=340) reiniciarJogo(); else if (y>=220 && y<=270) estado="menu"; }
        }
    }
    private void reiniciarJogo() { fase=1; pontos=0; vidas=3; obstaculosSuperados=0; obstaculosEncontrados=0; estado="jogando"; if(agente!=null) agente.desativar(); carregarFase(); }
    private void limparObjetos() { for(Obstaculo o:obstaculos)o.dispose(); for(Coletavel o:ossinhos)o.dispose(); obstaculos.clear(); ossinhos.clear(); posicoesObstaculos.clear(); }
    public void resize(int w,int h) { tela.update(w,h,true); }
    public void pause() { if(estado.equals("jogando"))estado="pausa"; }
    public void resume() { }
    public void hide() { }
    public void dispose() { limparObjetos(); if(fundo!=null)fundo.dispose(); if(caramelo!=null)caramelo.dispose(); if(agente!=null)agente.dispose(); if(lote!=null)lote.dispose(); if(fonte!=null)fonte.dispose(); if(formas!=null)formas.dispose(); }
}
