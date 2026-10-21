package br.mackenzie;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Obstaculo {
    private final Texture textura;
    private final Sprite sprite;
    private final float posicaoOriginalY;

    public Obstaculo(String arquivo, float posicaoX, float posicaoY, float largura, float altura) {
        textura = new Texture(arquivo);
        sprite = new Sprite(textura);
        sprite.setSize(largura, altura);
        sprite.setPosition(posicaoX, posicaoY);
        posicaoOriginalY = posicaoY;
    }

    public Sprite getSprite() { return sprite; }
    public float getPosicaoOriginalY() { return posicaoOriginalY; }
    public void dispose() { textura.dispose(); }
}
