package br.mackenzie;

public class FaseFloresta implements ConfiguracaoFase {
    public String getNomeCenario() { return "fundo_floresta.png"; }
    public float getVelocidade() { return 225f; }
    public String[] getTiposObstaculos() {
        return new String[] {"cone.png", "tronco.png"};
    }
}
