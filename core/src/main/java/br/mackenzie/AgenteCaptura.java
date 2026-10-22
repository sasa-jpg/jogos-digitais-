package br.mackenzie;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AgenteCaptura {
    private final Array<Texture> texturas = new Array<Texture>();
    private final Animation<TextureRegion> corrida;
    private final Sprite sprite = new Sprite();
    private float tempo = 0;
    private float velocidadeX;
    private boolean ativo = false;
    private boolean direita = true;
    private float espera = 3f;

    public AgenteCaptura() {
        Array<TextureRegion> quadros = new Array<TextureRegion>();
        for (int i = 4; i <= 6; i++) {
            Texture textura = new Texture("agente/correndo_direita_" + i + ".png");
            textura.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            texturas.add(textura);
            quadros.add(new TextureRegion(textura));
        }
        corrida = new Animation<TextureRegion>(0.09f, quadros, Animation.PlayMode.LOOP);
        sprite.setSize(150, 145);
        sprite.setPosition(-170, 370);
    }

    public void atualizar(float delta, int fase) {
        tempo += delta;
        if (!ativo) {
            espera -= delta;
            if (espera <= 0) {
                ativo = true;
                direita = Math.random() >= 0.5;
                velocidadeX = (direita ? 1 : -1) * (220 + fase * 35);
                sprite.setX(direita ? 245 : 670);
                sprite.setY(250 + (float)Math.random() * 260);
                espera = Math.max(2.5f, 7f - fase * 1.2f) + (float)Math.random() * 3f;
            }
        } else {
            sprite.setX(sprite.getX() + velocidadeX * delta);
            if (velocidadeX > 0 && sprite.getX() + sprite.getWidth() >= 700) {
                sprite.setX(700 - sprite.getWidth());
                ativo = false;
            } else if (velocidadeX < 0 && sprite.getX() <= 235) {
                sprite.setX(235);
                ativo = false;
            }
        }
    }

    public void desenhar(com.badlogic.gdx.graphics.g2d.SpriteBatch lote) {
        if (!ativo) return;
        TextureRegion quadro = corrida.getKeyFrame(tempo);
        if (direita) lote.draw(quadro, sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
        else lote.draw(quadro, sprite.getX()+sprite.getWidth(), sprite.getY(), -sprite.getWidth(), sprite.getHeight());
    }
    public boolean estaAtivo() { return ativo; }
    public Sprite getSprite() {
        TextureRegion quadro = corrida.getKeyFrame(tempo);
        sprite.setRegion(quadro);
        sprite.setFlip(!direita, false);
        return sprite;
    }
    public void desativar() { ativo = false; espera = 3f; }
    public void dispose() { for (Texture t : texturas) t.dispose(); }
}
