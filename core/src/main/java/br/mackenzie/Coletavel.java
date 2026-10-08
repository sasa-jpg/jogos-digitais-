package br.mackenzie;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Coletavel {

    private Sprite sprite;
    private Texture textura;

    public Coletavel(float x, float y) {
        textura = new Texture("ossinho.png");
        sprite = new Sprite(textura);

        sprite.setPosition(x, y);
        sprite.setSize(70, 70);
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void dispose() {
        textura.dispose();
    }
}