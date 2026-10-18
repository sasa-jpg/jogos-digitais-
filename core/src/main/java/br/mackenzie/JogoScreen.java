
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

public class JogoScreen implements Screen {

    private SpriteBatch batch;
    private Caramelo caramelo;
    private Texture fundo;
    private BitmapFont fonte;
    private ShapeRenderer shapeRenderer;

    private float fundoOffsetX = 0f;
    private final float velocidadeParalaxe = 0.4f;

    private Obstaculo[] cones = new Obstaculo[4];
    private Coletavel[] ossinhos = new Coletavel[6];

    private boolean[] bateuCone = new boolean[4];
    private boolean[] pegouOssinho = new boolean[6];

    private int pontuacao = 0;
    private int vidas = 3;
    private int faseAtual = 1;
    private ConfiguracaoFase configuracaoFase;

    private boolean movimentoBloqueado = false;
    private boolean pulando = false;
    private boolean andando = false;
    private boolean segundaImagem = false;
    private boolean faseConcluida = false;
    private boolean jogoConcluido = false;
    private boolean jogoPerdido = false;

    private float tempoAnimacao = 0f;
    private float velocidadePulo = 0f;
    private final float alturaChao = 70f;

    private float velocidadeAtual = 250f;
    private float distanciaFase = 0f;
    private final float distanciaNecessaria = 3000f;

    @Override
    public void show() {
        batch = new SpriteBatch();
        fonte = new BitmapFont();
        shapeRenderer = new ShapeRenderer();

        caramelo = new Caramelo();

        carregarCenario();
        criarObjetos();
    }

    private void carregarCenario() {
        if (fundo != null) {
            fundo.dispose();
        }

        configuracaoFase = obterConfiguracaoFase();
        fundo = new Texture(configuracaoFase.getNomeCenario());
        velocidadeAtual = configuracaoFase.getVelocidade();
        fundoOffsetX = 0f;
    }

    private ConfiguracaoFase obterConfiguracaoFase() {
        if (faseAtual == 1) {
            return new Fase1();
        } else if (faseAtual == 2) {
            return new Fase2();
        }
        return new Fase3();
    }

    private void criarObjetos() {
        liberarObjetos();
        cones = configuracaoFase.criarCones();
        ossinhos = configuracaoFase.criarOssinhos();
        bateuCone = new boolean[cones.length];
        pegouOssinho = new boolean[ossinhos.length];
    }

    @Override
    public void render(float delta) {
        input();
        logic();
        draw();
    }

    private void input() {
        float delta = Gdx.graphics.getDeltaTime();

        if (jogoPerdido || jogoConcluido) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                reiniciarJogo();
            }
            return;
        }

        if (faseConcluida) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                if (faseAtual < 3) {
                    avancarFase();
                } else {
                    jogoConcluido = true;
                }
            }
            return;
        }

        andando = Gdx.input.isKeyPressed(Input.Keys.RIGHT)
                && !pulando
                && !movimentoBloqueado;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)
                && !movimentoBloqueado) {

            fundoOffsetX -= velocidadeAtual * delta
                    * velocidadeParalaxe;

            distanciaFase += velocidadeAtual * delta;

            for (Obstaculo cone : cones) {
                cone.getSprite().translateX(-velocidadeAtual * delta);
            }

            for (Coletavel ossinho : ossinhos) {
                ossinho.getSprite().translateX(-velocidadeAtual * delta);
            }

            if (distanciaFase >= configuracaoFase.getDistanciaNecessaria()) {
                faseConcluida = true;
                movimentoBloqueado = true;
                andando = false;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)
                && !pulando
                && !faseConcluida) {

            pulando = true;
            andando = false;
            movimentoBloqueado = false;
            velocidadePulo = 750f;
            caramelo.setImagemPulando(true);
        }

        if (andando) {
            tempoAnimacao += delta;

            if (tempoAnimacao >= 0.15f) {
                segundaImagem = !segundaImagem;
                tempoAnimacao = 0f;
                caramelo.alternarImagemAndando(segundaImagem);
            }
        } else if (!pulando) {
            tempoAnimacao = 0f;
            segundaImagem = false;
            caramelo.alternarImagemAndando(false);
        }
    }

    private void logic() {
        if (faseConcluida || jogoConcluido || jogoPerdido) {
            return;
        }

        float delta = Gdx.graphics.getDeltaTime();
        float larguraTela = Gdx.graphics.getWidth();

        if (caramelo.getSprite().getX() < 0) {
            caramelo.getSprite().setX(0);
        }

        if (caramelo.getSprite().getX()
                + caramelo.getSprite().getWidth() > larguraTela) {
            caramelo.getSprite().setX(
                    larguraTela - caramelo.getSprite().getWidth());
        }

        // Movimento do pulo e gravidade
        if (pulando) {
            float yAnterior = caramelo.getSprite().getY();

            caramelo.pular(velocidadePulo * delta);
            velocidadePulo -= 1200f * delta;

            float yAtual = caramelo.getSprite().getY();

            if (yAtual <= alturaChao) {
                caramelo.getSprite().setY(alturaChao);
                velocidadePulo = 0f;
                pulando = false;
                caramelo.setImagemPulando(false);
            }

            // Permite pousar sobre o topo de um cone
            if (pulando && velocidadePulo < 0) {
                Rectangle cachorro =
                        caramelo.getSprite().getBoundingRectangle();

                for (Obstaculo cone : cones) {
                    Rectangle obstaculo =
                            cone.getSprite().getBoundingRectangle();

                    boolean sobreCone =
                            cachorro.x + cachorro.width > obstaculo.x
                            && cachorro.x < obstaculo.x + obstaculo.width;

                    float topoCone = obstaculo.y + obstaculo.height;

                    boolean cruzouTopo =
                            yAnterior >= topoCone && yAtual <= topoCone;

                    if (sobreCone && cruzouTopo) {
                        caramelo.getSprite().setY(topoCone);
                        velocidadePulo = 0f;
                        pulando = false;
                        caramelo.setImagemPulando(false);
                        break;
                    }
                }
            }
        }

        // Queda quando o cachorro não está apoiado no chão ou cone
        if (!pulando && caramelo.getSprite().getY() > alturaChao) {
            boolean apoiadoEmCone = false;

            Rectangle cachorro =
                    caramelo.getSprite().getBoundingRectangle();

            for (Obstaculo cone : cones) {
                Rectangle obstaculo =
                        cone.getSprite().getBoundingRectangle();

                boolean sobreCone =
                        cachorro.x + cachorro.width > obstaculo.x
                        && cachorro.x < obstaculo.x + obstaculo.width;

                boolean naAlturaDoCone =
                        Math.abs(caramelo.getSprite().getY()
                        - (obstaculo.y + obstaculo.height)) < 2f;

                if (sobreCone && naAlturaDoCone) {
                    apoiadoEmCone = true;
                    break;
                }
            }

            if (!apoiadoEmCone) {
                caramelo.getSprite().setY(
                        Math.max(alturaChao,
                                caramelo.getSprite().getY()
                                - 500f * delta));
            }
        }

        Rectangle cachorro =
                caramelo.getSprite().getBoundingRectangle();

        // Colisões com cones: cada cone desconta uma vida uma única vez
        for (int i = 0; i < cones.length; i++) {
            Rectangle obstaculo =
                    cones[i].getSprite().getBoundingRectangle();

            if (cachorro.overlaps(obstaculo) && !bateuCone[i]) {
                bateuCone[i] = true;
                vidas--;

                movimentoBloqueado = true;

                if (vidas <= 0) {
                    vidas = 0;
                    jogoPerdido = true;
                    andando = false;
                    pulando = false;
                    caramelo.setImagemPulando(false);
                    return;
                }
            }
        }

        // Bloqueia o movimento enquanto o cachorro estiver encostando
        // em um cone. Ele pode pular para continuar.
        boolean encostouEmCone = false;

        for (Obstaculo cone : cones) {
            if (cachorro.overlaps(
                    cone.getSprite().getBoundingRectangle())) {
                encostouEmCone = true;
                break;
            }
        }

        movimentoBloqueado = encostouEmCone && !pulando;

        // Coleta dos ossinhos
        for (int i = 0; i < ossinhos.length; i++) {
            if (!pegouOssinho[i]
                    && cachorro.overlaps(
                    ossinhos[i].getSprite().getBoundingRectangle())) {

                pontuacao += 10;
                pegouOssinho[i] = true;
            }
        }
    }

    private void avancarFase() {
        faseAtual++;

        if (faseAtual > 3) {
            faseAtual = 3;
            jogoConcluido = true;
            return;
        }

        distanciaFase = 0f;
        faseConcluida = false;
        movimentoBloqueado = false;
        pulando = false;
        andando = false;
        velocidadePulo = 0f;

        // Cada classe de fase define sua velocidade e seu cenario.
        carregarCenario();
        criarObjetos();
        reiniciarObjetosDaFase();
    }

    private void reiniciarObjetosDaFase() {
        configuracaoFase.reiniciarObjetos(cones, ossinhos);

        for (int i = 0; i < bateuCone.length; i++) {
            bateuCone[i] = false;
        }

        for (int i = 0; i < pegouOssinho.length; i++) {
            pegouOssinho[i] = false;
        }

        caramelo.getSprite().setPosition(100, alturaChao);
        caramelo.setImagemPulando(false);
        caramelo.alternarImagemAndando(false);
    }

    private void reiniciarJogo() {
        faseAtual = 1;
        pontuacao = 0;
        vidas = 3;

        velocidadeAtual = 250f;
        distanciaFase = 0f;

        faseConcluida = false;
        jogoConcluido = false;
        jogoPerdido = false;

        carregarCenario();
        criarObjetos();
        reiniciarObjetosDaFase();
    }

    private void draw() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float larguraTela = Gdx.graphics.getWidth();
        float alturaTela = Gdx.graphics.getHeight();

        batch.begin();

        // Cenário com rolagem
        float x = fundoOffsetX % larguraTela;

        if (x > 0) {
            x -= larguraTela;
        }

        batch.draw(fundo, x, 0, larguraTela, alturaTela);
        batch.draw(fundo, x + larguraTela, 0,
                larguraTela, alturaTela);

        caramelo.getSprite().draw(batch);

        for (Obstaculo cone : cones) {
            cone.getSprite().draw(batch);
        }

        for (int i = 0; i < ossinhos.length; i++) {
            if (!pegouOssinho[i]) {
                ossinhos[i].getSprite().draw(batch);
            }
        }

        batch.end();

        // Painel da pontuação
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(
                GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.07f, 0.10f, 0.15f, 0.90f);
        shapeRenderer.rect(20, alturaTela - 125, 240, 105);

        shapeRenderer.setColor(Color.GOLD);
        shapeRenderer.rect(20, alturaTela - 125, 6, 105);

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();

        fonte.setColor(Color.WHITE);
        fonte.getData().setScale(1f);
        fonte.draw(batch, "PONTUACAO", 40, alturaTela - 30);

        fonte.setColor(Color.GOLD);
        fonte.getData().setScale(1.5f);
        fonte.draw(batch, pontuacao + " pontos", 40, alturaTela - 55);

        fonte.setColor(Color.WHITE);
        fonte.getData().setScale(1f);
        fonte.draw(batch, "VIDAS: " + vidas, 40, alturaTela - 95);
        fonte.draw(batch, "FASE " + faseAtual,
                larguraTela - 120, alturaTela - 30);

        if (faseConcluida && !jogoConcluido) {
            fonte.setColor(Color.GOLD);

            if (faseAtual < 3) {
                fonte.draw(batch,
                        "Fase concluida! Aperte ESPACO",
                        40, alturaTela / 2f);
            } else {
                fonte.draw(batch,
                        "Ultima fase concluida! Aperte ESPACO",
                        40, alturaTela / 2f);
            }
        }

        if (jogoPerdido) {
            fonte.setColor(Color.RED);
            fonte.getData().setScale(2f);
            fonte.draw(batch, "FIM DE JOGO",
                    larguraTela / 2f - 100, alturaTela / 2f + 30);

            fonte.setColor(Color.WHITE);
            fonte.getData().setScale(1f);
            fonte.draw(batch, "Pontuacao final: " + pontuacao,
                    larguraTela / 2f - 90, alturaTela / 2f - 5);
            fonte.draw(batch, "Aperte ESPACO para jogar novamente",
                    larguraTela / 2f - 145, alturaTela / 2f - 35);
        }

        if (jogoConcluido) {
            fonte.setColor(Color.GOLD);
            fonte.getData().setScale(2f);
            fonte.draw(batch, "PARABENS!",
                    larguraTela / 2f - 90, alturaTela / 2f + 35);

            fonte.setColor(Color.WHITE);
            fonte.getData().setScale(1f);
            fonte.draw(batch, "Voce concluiu as 3 fases!",
                    larguraTela / 2f - 105, alturaTela / 2f + 5);
            fonte.draw(batch, "Pontuacao final: " + pontuacao,
                    larguraTela / 2f - 90, alturaTela / 2f - 20);
            fonte.draw(batch, "Aperte ESPACO para jogar novamente",
                    larguraTela / 2f - 145, alturaTela / 2f - 50);
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Nao e necessario alterar a tela neste momento.
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    private void liberarObjetos() {
        if (cones != null) {
            for (Obstaculo cone : cones) {
                if (cone != null) cone.dispose();
            }
        }
        if (ossinhos != null) {
            for (Coletavel ossinho : ossinhos) {
                if (ossinho != null) ossinho.dispose();
            }
        }
    }

    @Override
    public void dispose() {
        liberarObjetos();
        if (batch != null) batch.dispose();
        if (fonte != null) fonte.dispose();
        if (fundo != null) fundo.dispose();
        if (caramelo != null) caramelo.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
