package br.mackenzie;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Caramelo {

    private Sprite sprite;
    private Texture textura;

    public Caramelo() {
        textura = new Texture("caramelo.png");
        sprite = new Sprite(textura);

        sprite.setPosition(100, 80);
        sprite.setSize(150, 120);
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void moverEsquerda(float distancia) {
        sprite.translateX(-distancia);
    }

    public void moverDireita(float distancia) {
        sprite.translateX(distancia);
    }

    public void pular(float distancia) {
        sprite.translateY(distancia);
    }

    public void dispose() {
        textura.dispose();
    }
}