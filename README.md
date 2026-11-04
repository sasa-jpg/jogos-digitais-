# 🐕 Aventura do Caramelo

**LARA ARTHUSO E SABRINA MIYASAKI**

**Projeto Final — Jogo Sério para Reabilitação com IoT e libGDX**

Jogo desenvolvido em Java com libGDX para apoiar atividades de reabilitação motora infantil por meio de desafios interativos e controle físico utilizando ESP32 e sensor MPU6050.

## 🎯 Sobre o Projeto

A Aventura do Caramelo é um jogo sério voltado para crianças em processo de reabilitação motora. A proposta é transformar movimentos físicos de inclinação lateral do punho em ações dentro do jogo, tornando a atividade mais interativa e motivadora.

O personagem principal é um cachorro caramelo com visual cartunesco. A criança controla o personagem por meio de um dispositivo físico composto por uma placa ESP32 DevKit V1 e um sensor MPU6050, responsável por identificar as inclinações realizadas pelo jogador.

O jogo funciona como uma ferramenta de apoio e gamificação de atividades motoras, não substituindo o acompanhamento de um profissional de saúde.

## 🎮 Mecânica do Jogo

O jogador deve atravessar diferentes cenários, coletar ossos e desviar de obstáculos para avançar pelas fases.

- **Personagem:** cachorro caramelo com visual cartunesco.
- **Objetivo:** atravessar os cenários, coletar itens e evitar obstáculos.
- **Controle:** movimentação lateral do personagem por meio da inclinação do dispositivo físico.
- **Obstáculos:** elementos distribuídos pelos cenários que exigem mudanças de direção.
- **Coletáveis:** ossos que contribuem para a pontuação.
- **Dificuldade:** progressiva ao longo das três fases.
- **Vidas:** o jogador possui três vidas, perdendo uma ao colidir com obstáculos.

### 🌎 As três fases

1. **Cidade — Introdução:** primeiro cenário, com obstáculos como cones, caixas e bueiros abertos.
2. **Floresta — Desafio intermediário:** cenário com cones e troncos, apresentando maior dificuldade.
3. **Montanhas — Desafio avançado:** cenário com cones e troncos, exigindo maior controle dos movimentos e atenção aos obstáculos.

## 🔌 Integração com IoT

O controle do personagem utiliza uma placa ESP32 DevKit V1 integrada ao sensor MPU6050, que possui acelerômetro e giroscópio.

O funcionamento da integração ocorre da seguinte forma:

1. A criança inclina lateralmente o dispositivo físico.
2. O MPU6050 identifica as variações de orientação.
3. A ESP32 realiza a leitura dos dados do sensor.
4. Os dados são enviados ao computador por comunicação serial USB.
5. A aplicação Java recebe e interpreta as informações.
6. O libGDX transforma os dados recebidos em movimentos laterais do personagem Caramelo.

**Fluxo:** movimento físico → MPU6050 → ESP32 → comunicação serial → Java → libGDX.

A proposta é utilizar exclusivamente o dispositivo IoT para controlar o personagem durante a demonstração final do projeto.

## 📊 Métrica de Desempenho

O jogo apresenta uma métrica de desempenho ao final da partida:

**Taxa de obstáculos superados (%):** representa a porcentagem de obstáculos que o jogador conseguiu ultrapassar em relação ao total de obstáculos encontrados.

O cálculo é realizado por meio da seguinte fórmula:

Taxa de obstáculos superados = (Obstáculos superados / Obstáculos encontrados) × 100

Essa métrica permite observar o desempenho do jogador durante a partida, considerando sua capacidade de evitar os obstáculos apresentados nos cenários.

O indicador possui finalidade informativa e não representa uma avaliação clínica ou diagnóstico de evolução motora.

## 🛠️ Tecnologias Utilizadas

- **Java:** linguagem de programação.
- **libGDX:** framework utilizado no desenvolvimento do jogo.
- **Gradle:** gerenciamento e compilação do projeto.
- **ESP32 DevKit V1:** placa responsável pela comunicação com o sensor.
- **MPU6050:** sensor com acelerômetro e giroscópio utilizado para identificar inclinações.
- **Comunicação serial USB:** transmissão dos dados entre a ESP32 e a aplicação Java.
- **Git e GitHub:** versionamento e colaboração no desenvolvimento.
