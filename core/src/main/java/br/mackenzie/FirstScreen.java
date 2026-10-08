package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

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
    private boolean pegouOssinho1 = false;
    private boolean pegouOssinho2 = false;
    private boolean pegouOssinho3 = false;
    private boolean pegouOssinho4 = false;
    private boolean pegouOssinho5 = false;
    private boolean pegouOssinho6 = false;
    private boolean pulando = false;
    private float velocidadePulo = 0f;
    private float alturaChao = 80f;

    @Override
    public void show() {
        batch = new SpriteBatch();
        fonte = new BitmapFont();
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

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
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

        else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            caramelo.moverEsquerda(velocidade * delta);
            fundoOffsetX += velocidade * delta * velocidadeParalaxe;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && !pulando) {
            pulando = true;
            velocidadePulo = 750f;
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

        if (pulando) {
            caramelo.pular(velocidadePulo * delta);
            
            velocidadePulo -= 1200f * delta;

            if (caramelo.getSprite().getY() <= alturaChao) {
                caramelo.getSprite().setY(alturaChao);
                velocidadePulo = 0;
                pulando = false;
            }
        }

        if (caramelo.getSprite().getBoundingRectangle()
                .overlaps(cone1.getSprite().getBoundingRectangle()) && !bateuCone1) {
            pontuacao -= 5;
            bateuCone1 = true;
        }

        if (caramelo.getSprite().getBoundingRectangle()
                .overlaps(cone2.getSprite().getBoundingRectangle()) && !bateuCone2) {
            pontuacao -= 5;
            bateuCone2 = true;
        }

        if (caramelo.getSprite().getBoundingRectangle()
                .overlaps(cone3.getSprite().getBoundingRectangle()) && !bateuCone3) {
            pontuacao -= 5;
            bateuCone3 = true;
        }

        if (caramelo.getSprite().getBoundingRectangle()
                .overlaps(cone4.getSprite().getBoundingRectangle()) && !bateuCone4) {
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

        batch.begin();

        // Fundo 
        float larguraTela = Gdx.graphics.getWidth();
        float alturaTela = Gdx.graphics.getHeight();

        float x = fundoOffsetX % larguraTela;

        if (x > 0) {
            x -= larguraTela;
        }

        batch.draw(fundo, x, 0, larguraTela, alturaTela);
        batch.draw(fundo, x + larguraTela, 0, larguraTela, alturaTela);

        // Caramelo
        caramelo.getSprite().draw(batch);

        // Obstáculos
        cone1.getSprite().draw(batch);
        cone2.getSprite().draw(batch);
        cone3.getSprite().draw(batch);
        cone4.getSprite().draw(batch);

        //osso
        if (!pegouOssinho1) {
            ossinho1.getSprite().draw(batch);
        }

        if (!pegouOssinho2) {
            ossinho2.getSprite().draw(batch);
        }

        if (!pegouOssinho3) {
            ossinho3.getSprite().draw(batch);
        }

        if (!pegouOssinho4) {
            ossinho4.getSprite().draw(batch);
        }

        if (!pegouOssinho5) {
            ossinho5.getSprite().draw(batch);
        }

        if (!pegouOssinho6) {
            ossinho6.getSprite().draw(batch);
        }

        fonte.draw(batch, "Pontuação: " + pontuacao, 20, Gdx.graphics.getHeight() - 20);

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
    }
}