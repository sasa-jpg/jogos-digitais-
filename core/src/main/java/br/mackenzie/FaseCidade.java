package br.mackenzie;

public class FaseCidade implements ConfiguracaoFase {
    public String getNomeCenario() { return "fundo_cidade.png"; }
    public float getVelocidade() { return 190f; }
    public String[] getTiposObstaculos() {
        return new String[] {"cone.png", "caixa.png", "bueiro_aberto.png"};
    }
}
