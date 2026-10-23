package br.mackenzie;

/** Tudo o que muda de uma fase para outra: dificuldade (velocidades, distancia, obstaculos). */
public interface ConfiguracaoFase {
    /** Velocidade de rolagem do cenario e dos obstaculos. */
    float getVelocidade();
    /** Velocidade lateral do cachorro (pixels por segundo). */
    float getVelocidadeCaramelo();
    /** Distancia total a percorrer para completar a fase. */
    float getDistancia();
    /** Espaco vertical entre obstaculos (menor = mais obstaculos, mais dificil). */
    float getEspacamentoObstaculos();
    String[] getTiposObstaculos();
}
