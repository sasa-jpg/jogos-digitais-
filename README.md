# As Aventuras de Caramelito
### Jogo sério para reabilitação física infantil

Uma solução gamificada que busca transformar exercícios motores repetitivos
em uma experiência interativa e divertida, incentivando a participação
de crianças em atividades de reabilitação física.

## 🎯 Sobre o Projeto

- **Público-alvo:** Crianças em processo de reabilitação física.
- **Objetivo:** Estimular a coordenação motora e tornar as atividades
  de reabilitação mais motivadoras.
- **Gênero:** Jogo de progressão e desvio de obstáculos.
- **Tecnologia principal:** Java com libGDX.

## 🎮 Mecânica do Jogo

O jogador controla Caramelito, um cachorrinho caramelo que precisa
avançar pelo cenário e desviar de obstáculos para completar cada fase.

O jogo contará com três fases de dificuldade progressiva:

1. **Cidade:** primeira etapa da aventura.
2. **Floresta:** desafios com dificuldade intermediária.
3. **Montanhas:** etapa final, com desafios mais complexos.

A dificuldade poderá aumentar conforme a progressão do jogador,
considerando fatores como a quantidade, a frequência e a disposição
dos obstáculos.

## 🔌 Integração com IoT

O projeto prevê a utilização de um Arduino integrado ao sensor MPU6050,
que possui acelerômetro e giroscópio.

O sensor será utilizado para identificar a inclinação do dispositivo
e transmitir os dados ao jogo, permitindo controlar os movimentos
laterais de Caramelito.

**Fluxo planejado:**

MPU6050 → Arduino → Comunicação serial → Java/libGDX → Movimentação do personagem

A integração física está em desenvolvimento e será validada durante
os testes com o hardware.

## 📊 Métricas de Avaliação

O jogo prevê o acompanhamento de indicadores de desempenho durante
as atividades:

- **Precisão dos movimentos:** capacidade de realizar os movimentos
  necessários para desviar dos obstáculos.
- **Velocidade de reação:** tempo de resposta entre um estímulo do jogo
  e a movimentação identificada pelo sistema.
- **Consistência:** regularidade das respostas de movimento ao longo
  da atividade.

As métricas serão definidas e implementadas conforme o desenvolvimento
da integração com o sensor.

## 🛠️ Tecnologias Utilizadas

- **Java:** linguagem de programação.
- **libGDX:** framework para desenvolvimento do jogo.
- **Arduino:** plataforma de hardware para aquisição dos movimentos.
- **MPU6050:** sensor de aceleração e rotação.
- **Comunicação serial:** mecanismo previsto para transmitir os dados
  do Arduino ao jogo.

## 🚧 Status do Projeto

Projeto acadêmico em desenvolvimento.

As mecânicas do jogo, os cenários e a integração com o hardware
estão sujeitos a implementação, ajustes e testes.

## 👥 Equipe
Lara Arthuso
Sabrina Miyasaki
Projeto desenvolvido como atividade acadêmica da disciplina de Jogos Digitais.
