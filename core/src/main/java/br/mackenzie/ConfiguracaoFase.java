package br.mackenzie;

/** Define o cenário, a dificuldade e os objetos específicos de cada fase. */
public interface ConfiguracaoFase {
    String getNomeCenario();
    float getVelocidade();
    float getDistanciaNecessaria();
    Obstaculo[] criarCones();
    Coletavel[] criarOssinhos();
    void reiniciarObjetos(Obstaculo[] cones, Coletavel[] ossinhos);
}
