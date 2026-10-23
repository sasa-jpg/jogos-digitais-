package br.mackenzie;

/** Fase 2: floresta. Mais rapida, com mais obstaculos. */
public class FaseFloresta implements ConfiguracaoFase {
    public float getVelocidade() { return 250f; }
    public float getVelocidadeCaramelo() { return 320f; }
    public float getDistancia() { return 3300f; }
    public float getEspacamentoObstaculos() { return 130f; }
    public String[] getTiposObstaculos() {
        return new String[] {"cone.png", "tronco.png"};
    }
}
