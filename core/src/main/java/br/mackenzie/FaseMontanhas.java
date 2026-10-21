package br.mackenzie;

public class FaseMontanhas implements ConfiguracaoFase {
    public String getNomeCenario() { return "fundo_montanhas.png"; }
    public float getVelocidade() { return 260f; }
    public String[] getTiposObstaculos() {
        return new String[] {"cone.png", "tronco.png"};
    }
}
