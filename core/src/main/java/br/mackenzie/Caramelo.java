package br.mackenzie;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Caramelo {
    private final Texture textura;
    private final Sprite sprite;

    public Caramelo() {
        textura = new Texture("caramelo.png");
        sprite = new Sprite(textura);
        sprite.setSize(100, 75);
        sprite.setPosition(430, 85);
    }

    public Sprite getSprite() { return sprite; }

    public void moverPara(float posicaoX) {
        sprite.setX(Math.max(80, Math.min(780, posicaoX)));
    }

    public void dispose() { textura.dispose(); }
}
