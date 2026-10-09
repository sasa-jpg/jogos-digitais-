
package br.mackenzie;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Caramelo {

    private Sprite sprite;
    private Texture textura;
    private Texture texturaPulando;
    private Texture texturaAndando2;

    public Caramelo() {
        textura = new Texture("caramelo.png");
        texturaPulando = new Texture("caramelo_pulando.png");
        texturaAndando2 = new Texture("caramelo_andando2.png");

        sprite = new Sprite(textura);
        sprite.setPosition(100, 70);
        sprite.setSize(150, 130);
    }

    public void alternarImagemAndando(boolean segundaImagem) {
        if (segundaImagem) {
            sprite.setTexture(texturaAndando2);
            sprite.setRegion(
                0, 0,
                texturaAndando2.getWidth(),
                texturaAndando2.getHeight()
            );
        } else {
            sprite.setTexture(textura);
            sprite.setRegion(
                0, 0,
                textura.getWidth(),
                textura.getHeight()
            );
        }

        sprite.setSize(150, 130);
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

    
    public void setImagemPulando(boolean pulando) {
        if (pulando) {
            sprite.setTexture(texturaPulando);
            sprite.setRegion(
                0, 0,
                texturaPulando.getWidth(),
                texturaPulando.getHeight()
            );
        } else {
            sprite.setTexture(textura);
            sprite.setRegion(
                0, 0,
                textura.getWidth(),
                textura.getHeight()
            );
        }

        sprite.setSize(150, 130);
    }

    public void dispose() {
        textura.dispose();
        texturaPulando.dispose();
        texturaAndando2.dispose();
    }
}