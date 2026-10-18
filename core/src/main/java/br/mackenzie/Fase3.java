package br.mackenzie;

/** Responsabilidades específicas da fase: cenário, velocidade e posicionamento dos objetos. */
public class Fase3 implements ConfiguracaoFase {
    @Override
    public String getNomeCenario() {
        return "fase3_montanhas.png";
    }

    @Override
    public float getVelocidade() {
        return 350f;
    }

    @Override
    public float getDistanciaNecessaria() {
        return 3000f;
    }

    @Override
    public Obstaculo[] criarCones() {
        return new Obstaculo[] {
            new Obstaculo(800, 80),
            new Obstaculo(1300, 80),
            new Obstaculo(1900, 80),
            new Obstaculo(2500, 80)
        };
    }

    @Override
    public Coletavel[] criarOssinhos() {
        return new Coletavel[] {
            new Coletavel(500, 120),
            new Coletavel(1000, 180),
            new Coletavel(1450, 120),
            new Coletavel(1700, 180),
            new Coletavel(2150, 120),
            new Coletavel(2400, 180)
        };
    }

    @Override
    public void reiniciarObjetos(Obstaculo[] cones, Coletavel[] ossinhos) {
        float[] posicoesCones = {800, 1300, 1900, 2500};
        for (int i = 0; i < cones.length; i++) {
            cones[i].getSprite().setPosition(posicoesCones[i], 80);
        }

        float[] posicoesX = {500, 1000, 1450, 1700, 2150, 2400};
        float[] posicoesY = {120, 180, 120, 180, 120, 180};
        for (int i = 0; i < ossinhos.length; i++) {
            ossinhos[i].getSprite().setPosition(posicoesX[i], posicoesY[i]);
        }
    }
}
