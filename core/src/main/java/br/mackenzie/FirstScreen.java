package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class FirstScreen implements Screen {

    private SpriteBatch batch;
    private Caramelo caramelo;
    private Texture fundo;
    private float fundoOffsetX = 0f;
    private float velocidadeParalaxe = 0.4f;
    private Obstaculo cone1;
    private Obstaculo cone2;

    @Override
    public void show() {
        batch = new SpriteBatch();
        caramelo = new Caramelo();
        fundo = new Texture("fase1_cidade.png");
        //cone1 = new Obstaculo(500, 100);
        //cone2 = new Obstaculo(800, 100);
    }

    @Override
    public void render(float delta) {
        input();
        logic();
        draw();
    }

    private void input() {
        float velocidade = 100f;
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            caramelo.moverDireita(velocidade * delta);
            fundoOffsetX -= velocidade * delta * velocidadeParalaxe;
        } 
        else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            caramelo.moverEsquerda(velocidade * delta);
            fundoOffsetX += velocidade * delta * velocidadeParalaxe;
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
        //cone1.getSprite().draw(batch);
        //cone2.getSprite().draw(batch);

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