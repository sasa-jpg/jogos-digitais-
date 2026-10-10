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

public class FirstScreen implements Screen {

    private SpriteBatch batch;
    private Caramelo caramelo;
    private Texture fundo;
    private float fundoOffsetX = 0f;
    private float velocidadeParalaxe = 0.4f;
    private Obstaculo cone1;
    private Obstaculo cone2;
    private Obstaculo cone3;
    private Obstaculo cone4;
    private Coletavel ossinho1;
    private Coletavel ossinho2;
    private Coletavel ossinho3;
    private Coletavel ossinho4;
    private Coletavel ossinho5;
    private Coletavel ossinho6;
    private int pontuacao = 0;
    private BitmapFont fonte;
    private boolean bateuCone1 = false;
    private boolean bateuCone2 = false;
    private boolean bateuCone3 = false;
    private boolean bateuCone4 = false;
    private boolean movimentoBloqueado = false;
    private boolean pegouOssinho1 = false;
    private boolean pegouOssinho2 = false;
    private boolean pegouOssinho3 = false;
    private boolean pegouOssinho4 = false;
    private boolean pegouOssinho5 = false;
    private boolean pegouOssinho6 = false;
    private boolean pulando = false;
    private float tempoAnimacao = 0f;
    private boolean segundaImagem = false;
    private boolean andando = false;
    private float velocidadePulo = 0f;
    private float alturaChao = 70f;
    private float tempoPausa = 0f;
    private ShapeRenderer shapeRenderer;

    @Override
    public void show() {
        batch = new SpriteBatch();
        fonte = new BitmapFont();
        shapeRenderer = new ShapeRenderer();
        caramelo = new Caramelo();
        fundo = new Texture("fase1_cidade.png");
        cone1 = new Obstaculo(800, 80);
        cone2 = new Obstaculo(1300, 80);
        cone3 = new Obstaculo(1900, 80);
        cone4 = new Obstaculo(2500, 80);
        ossinho1 = new Coletavel(500, 120);
        ossinho2 = new Coletavel(1000, 180);
        ossinho3 = new Coletavel(1450, 120);
        ossinho4 = new Coletavel(1700, 180);
        ossinho5 = new Coletavel(2150, 120);
        ossinho6 = new Coletavel(2400, 180);
    }

    @Override
    public void render(float delta) {
        input();
        logic();
        draw();
    }

    
    private void input() {
        float velocidade = 250f;
        float delta = Gdx.graphics.getDeltaTime();

        andando = Gdx.input.isKeyPressed(Input.Keys.RIGHT) && !pulando;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && !movimentoBloqueado) {
            fundoOffsetX -= velocidade * delta * velocidadeParalaxe;

            cone1.getSprite().translateX(-velocidade * delta);
            cone2.getSprite().translateX(-velocidade * delta);
            cone3.getSprite().translateX(-velocidade * delta);
            cone4.getSprite().translateX(-velocidade * delta);

            ossinho1.getSprite().translateX(-velocidade * delta);
            ossinho2.getSprite().translateX(-velocidade * delta);
            ossinho3.getSprite().translateX(-velocidade * delta);
            ossinho4.getSprite().translateX(-velocidade * delta);
            ossinho5.getSprite().translateX(-velocidade * delta);
            ossinho6.getSprite().translateX(-velocidade * delta);
        } 
        

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && !pulando) {
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
        } else {
            tempoAnimacao = 0f;

            if (pulando) {
                
            } else {
                segundaImagem = false;
                caramelo.alternarImagemAndando(false);
            }
        }
    }

    private void logic() {
        float larguraTela = Gdx.graphics.getWidth();

        if (caramelo.getSprite().getX() < 0) {
            caramelo.getSprite().setX(0);
        }

        if (caramelo.getSprite().getX() + caramelo.getSprite().getWidth() > larguraTela) {
            caramelo.getSprite().setX(
                larguraTela - caramelo.getSprite().getWidth()
            );
        }

        float delta = Gdx.graphics.getDeltaTime();

        if (tempoPausa > 0f) {
            tempoPausa -= delta;
        }

       
        if (pulando) {
            float yAnterior = caramelo.getSprite().getY();

            caramelo.pular(velocidadePulo * delta);
            velocidadePulo -= 1200f * delta;

            float yAtual = caramelo.getSprite().getY();

            
            if (yAtual <= alturaChao) {
                caramelo.getSprite().setY(alturaChao);
                velocidadePulo = 0;
                pulando = false;
                caramelo.setImagemPulando(false);
            }

           
            if (pulando && velocidadePulo < 0) {
                Rectangle cachorro = caramelo.getSprite().getBoundingRectangle();

                Obstaculo[] cones = {cone1, cone2, cone3, cone4};

                for (Obstaculo cone : cones) {
                    Rectangle obstaculo = cone.getSprite().getBoundingRectangle();

                    boolean sobreCone =
                        cachorro.x + cachorro.width > obstaculo.x &&
                        cachorro.x < obstaculo.x + obstaculo.width;

                    float topoCone = obstaculo.y + obstaculo.height;
                    boolean cruzouTopo =
                        yAnterior >= topoCone &&
                        yAtual <= topoCone;

                    if (sobreCone && cruzouTopo) {
                        caramelo.getSprite().setY(topoCone);
                        velocidadePulo = 0;
                        pulando = false;
                        caramelo.setImagemPulando(false);
                        break;
                    }
                }
            }
        }

        
        if (!pulando && caramelo.getSprite().getY() > alturaChao) {
            boolean apoiadoEmCone = false;

            Rectangle cachorro = caramelo.getSprite().getBoundingRectangle();

            Obstaculo[] cones = {cone1, cone2, cone3, cone4};

            for (Obstaculo cone : cones) {
                Rectangle obstaculo = cone.getSprite().getBoundingRectangle();

                boolean sobreCone =
                    cachorro.x + cachorro.width > obstaculo.x &&
                    cachorro.x < obstaculo.x + obstaculo.width;

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
                    Math.max(alturaChao, caramelo.getSprite().getY() - 500f * delta)
                );
            }
        }
                
        boolean colidiuCone1 = caramelo.getSprite().getBoundingRectangle()
        .overlaps(cone1.getSprite().getBoundingRectangle());

        boolean colidiuCone2 = caramelo.getSprite().getBoundingRectangle()
                .overlaps(cone2.getSprite().getBoundingRectangle());

        boolean colidiuCone3 = caramelo.getSprite().getBoundingRectangle()
                .overlaps(cone3.getSprite().getBoundingRectangle());

        boolean colidiuCone4 = caramelo.getSprite().getBoundingRectangle()
                .overlaps(cone4.getSprite().getBoundingRectangle());

        
        movimentoBloqueado = !pulando
                && (colidiuCone1 || colidiuCone2
                || colidiuCone3 || colidiuCone4);

        // Cone 1
        if (colidiuCone1 && !bateuCone1) {
            pontuacao -= 5;
            bateuCone1 = true;
        }

        // Cone 2
        if (colidiuCone2 && !bateuCone2) {
            pontuacao -= 5;
            bateuCone2 = true;
        }

        // Cone 3
        if (colidiuCone3 && !bateuCone3) {
            pontuacao -= 5;
            bateuCone3 = true;
        }

        // Cone 4
        if (colidiuCone4 && !bateuCone4) {
            pontuacao -= 5;
            bateuCone4 = true;
        }
        if (caramelo.getSprite().getBoundingRectangle()
                .overlaps(ossinho1.getSprite().getBoundingRectangle()) && !pegouOssinho1) {
            pontuacao += 10;
            pegouOssinho1 = true;
        }

        if (caramelo.getSprite().getBoundingRectangle()
                .overlaps(ossinho2.getSprite().getBoundingRectangle()) && !pegouOssinho2) {
            pontuacao += 10;
            pegouOssinho2 = true;
        }

        if (caramelo.getSprite().getBoundingRectangle()
                .overlaps(ossinho3.getSprite().getBoundingRectangle()) && !pegouOssinho3) {
            pontuacao += 10;
            pegouOssinho3 = true;
        }

        if (caramelo.getSprite().getBoundingRectangle()
                .overlaps(ossinho4.getSprite().getBoundingRectangle()) && !pegouOssinho4) {
            pontuacao += 10;
            pegouOssinho4 = true;
        }

        if (caramelo.getSprite().getBoundingRectangle()
                .overlaps(ossinho5.getSprite().getBoundingRectangle()) && !pegouOssinho5) {
            pontuacao += 10;
            pegouOssinho5 = true;
        }

        if (caramelo.getSprite().getBoundingRectangle()
                .overlaps(ossinho6.getSprite().getBoundingRectangle()) && !pegouOssinho6) {
            pontuacao += 10;
            pegouOssinho6 = true;
        }
    }

    
private void draw() {
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    float larguraTela = Gdx.graphics.getWidth();
    float alturaTela = Gdx.graphics.getHeight();

    batch.begin();

    // Fundo
    float x = fundoOffsetX % larguraTela;

    if (x > 0) {
        x -= larguraTela;
    }

    batch.draw(fundo, x, 0, larguraTela, alturaTela);
    batch.draw(fundo, x + larguraTela, 0, larguraTela, alturaTela);

    // Caramelo
    caramelo.getSprite().draw(batch);

    // Cones
    cone1.getSprite().draw(batch);
    cone2.getSprite().draw(batch);
    cone3.getSprite().draw(batch);
    cone4.getSprite().draw(batch);

    // Ossinhos
    if (!pegouOssinho1) ossinho1.getSprite().draw(batch);
    if (!pegouOssinho2) ossinho2.getSprite().draw(batch);
    if (!pegouOssinho3) ossinho3.getSprite().draw(batch);
    if (!pegouOssinho4) ossinho4.getSprite().draw(batch);
    if (!pegouOssinho5) ossinho5.getSprite().draw(batch);
    if (!pegouOssinho6) ossinho6.getSprite().draw(batch);

    
    batch.end();

    // painel de pontuação
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(
        GL20.GL_SRC_ALPHA,
        GL20.GL_ONE_MINUS_SRC_ALPHA
    );

    if (shapeRenderer == null) {
        shapeRenderer = new ShapeRenderer();
    }

shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

    shapeRenderer.setColor(0.07f, 0.10f, 0.15f, 0.90f);
    shapeRenderer.rect(
        20,
        alturaTela - 95,
        240,
        75
    );

    
    shapeRenderer.setColor(Color.GOLD);
    shapeRenderer.rect(
        20,
        alturaTela - 95,
        6,
        75
    );

    shapeRenderer.end();
    Gdx.gl.glDisable(GL20.GL_BLEND);

    
    batch.begin();

    fonte.setColor(Color.WHITE);
    fonte.getData().setScale(1.0f);
    fonte.draw(
        batch,
        "PONTUACAO",
        40,
        alturaTela - 30
    );

    fonte.setColor(Color.GOLD);
    fonte.getData().setScale(1.5f);
    fonte.draw(
        batch,
        pontuacao + " pontos",
        40,
        alturaTela - 55
    );

    
    fonte.getData().setScale(1.0f);
    fonte.setColor(Color.WHITE);

    batch.end();
}

    @Override
    public void resize(int width, int height) {
        if(width <= 0 || height <= 0) return;
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

    @Override
    public void dispose() {
        batch.dispose();
        caramelo.dispose();
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}

