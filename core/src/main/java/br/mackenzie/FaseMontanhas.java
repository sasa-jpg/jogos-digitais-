package br.mackenzie;

public class FaseMontanhas implements ConfiguracaoFase {
    public float getVelocidade() { return 310f; }
    public float getVelocidadeCaramelo() { return 380f; }
    public float getDistancia() { return 4300f; }
    public float getEspacamentoObstaculos() { return 125f; }
    public String[] getTiposObstaculos() {
        return new String[] {"cone.png", "tronco.png"};
    }
}
