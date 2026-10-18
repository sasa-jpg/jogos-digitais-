package br.mackenzie;

import com.badlogic.gdx.Game;

/** Classe principal do jogo. */
public class Main extends Game {
    @Override
    public void create() {
        setScreen(new JogoScreen());
    }
}
