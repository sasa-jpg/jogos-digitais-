package br.mackenzie;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Obstaculo {

    private Sprite sprite;
    private Texture textura;

    public Obstaculo(float x, float y) {
        textura = new Texture("cone.png");
        sprite = new Sprite(textura);

        sprite.setPosition(x, y);
        sprite.setSize(100, 100);
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void dispose() {
        textura.dispose();
    }
}