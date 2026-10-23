package br.mackenzie;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Caramelo {
    private final Texture textura1;
    private final Texture textura2;
    private final Sprite sprite;
    private float tempoAnimacao = 0;
    private boolean segundoQuadro = false;

    public Caramelo() {
        textura1 = new Texture("caramelo_correndo_1.png");
        textura2 = new Texture("caramelo_correndo_2.png");
        sprite = new Sprite(textura1);
        sprite.setSize(85, 106);
        sprite.setPosition(430, 65);
    }

    public Sprite getSprite() { return sprite; }

    /** ritmo = 1 na velocidade inicial; maior = patas se mexem mais rapido. */
    public void atualizar(float delta, float ritmo) {
        tempoAnimacao += delta * ritmo;
        if (tempoAnimacao >= 0.22f) {
            tempoAnimacao = 0;
            segundoQuadro = !segundoQuadro;
            sprite.setTexture(segundoQuadro ? textura2 : textura1);
        }
    }

    public void moverPara(float posicaoX) {
        // Mantem o cachorro dentro dos limites da estrada.
        sprite.setX(Math.max(190, Math.min(675, posicaoX)));
    }

    public void dispose() {
        textura1.dispose();
        textura2.dispose();
    }
}
