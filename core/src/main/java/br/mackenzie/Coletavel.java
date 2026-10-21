package br.mackenzie;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Coletavel {
    private final Texture textura;
    private final Sprite sprite;

    public Coletavel(float posicaoX, float posicaoY) {
        textura = new Texture("ossinho.png");
        sprite = new Sprite(textura);
        sprite.setSize(28, 28);
        sprite.setPosition(posicaoX, posicaoY);
    }

    public Sprite getSprite() { return sprite; }
    public void dispose() { textura.dispose(); }
}
