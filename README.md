**LARA ARTHUSO E SABRINA MIYASAKI**

# 🐕 Aventura do Caramelo

**Projeto Final — Jogo Sério para Reabilitação com IoT e libGDX**

Jogo desenvolvido em Java com libGDX para apoiar atividades de reabilitação motora infantil por meio de desafios interativos e controle físico com Arduino e sensor MPU6050.

## 🎯 Sobre o Projeto

A Aventura do Caramelo é um jogo sério voltado para crianças em processo de reabilitação motora. A proposta é transformar movimentos físicos de coordenação em ações dentro do jogo, tornando a atividade mais interativa e motivadora.

O personagem principal é um cachorro caramelo com visual cartunesco. A criança controla o personagem utilizando um dispositivo físico com Arduino e sensor MPU6050, que identifica inclinações e movimentos.

O jogo é uma ferramenta de apoio e gamificação de atividades motoras, não substituindo o acompanhamento de um profissional de saúde.

## 🎮 Mecânica do Jogo

O jogador deve percorrer os cenários, coletar itens e desviar de obstáculos para concluir cada fase.

- **Personagem:** cachorro caramelo com visual cartunesco.
- **Objetivo:** percorrer o cenário, coletar itens e chegar ao final da fase.
- **Obstáculos:** elementos que exigem controle e mudança de direção.
- **Coletáveis:** ossos e outros objetos que contribuem para a pontuação.
- **Dificuldade:** progressiva ao longo das três fases.
- **Pontuação:** pode considerar itens coletados, tempo, colisões e desempenho dos movimentos.

### 🌎 As três fases

1. **Cidade — Introdução:** cenário inicial com menos obstáculos e menor velocidade, para familiarização com a mecânica.
2. **Floresta — Desafio intermediário:** maior quantidade de obstáculos e necessidade de mudanças de direção.
3. **Montanhas — Desafio avançado:** maior dificuldade, exigindo movimentos mais rápidos e consistentes.

## 🔌 Integração com IoT

O controle do personagem será realizado por meio de um Arduino conectado ao sensor MPU6050, que possui acelerômetro e giroscópio.

O funcionamento proposto é:

1. A criança movimenta ou inclina o dispositivo.
2. O MPU6050 identifica a orientação e o movimento.
3. O Arduino processa as leituras do sensor.
4. Os dados são enviados ao computador por comunicação serial.
5. A aplicação Java recebe os dados.
6. O libGDX transforma as informações em movimentos do Caramelo.

**Fluxo:** movimento físico → MPU6050 → Arduino → comunicação serial → Java → libGDX.

A proposta é utilizar exclusivamente o dispositivo IoT para controlar o personagem na versão final.

**Status:** a integração física com o Arduino e o MPU6050 ainda precisa ser implementada e testada.

## 📊 Métricas de Avaliação

O projeto prevê a possibilidade de observar indicadores de desempenho durante as partidas, como:

- Precisão dos movimentos.
- Velocidade de reação.
- Consistência dos movimentos.
- Quantidade de itens coletados.
- Tempo para concluir cada fase.
- Colisões com obstáculos.

Essas informações podem ajudar a acompanhar o desempenho da criança durante as atividades, sem substituir avaliações clínicas.

## 🛠️ Tecnologias Utilizadas

- **Java:** linguagem de programação.
- **libGDX:** desenvolvimento do jogo.
- **Gradle:** gerenciamento e compilação do projeto.
- **Arduino:** comunicação entre o dispositivo físico e o jogo.
- **MPU6050:** sensor de acelerômetro e giroscópio.
- **Comunicação serial:** transmissão dos dados do Arduino para a aplicação Java.
- **Git e GitHub:** versionamento e colaboração.
