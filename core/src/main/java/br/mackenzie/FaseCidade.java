package br.mackenzie;

/** Fase 1: cidade. Ritmo tranquilo para aprender os controles. */
public class FaseCidade implements ConfiguracaoFase {
    public float getVelocidade() { return 190f; }
    public float getVelocidadeCaramelo() { return 260f; }
    public float getDistancia() { return 2400f; }
    public float getEspacamentoObstaculos() { return 150f; }
    public String[] getTiposObstaculos() {
        return new String[] {"cone.png", "caixa.png", "bueiro_aberto.png"};
    }
}
