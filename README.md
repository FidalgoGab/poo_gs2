# Territory Predictive Insights

Sistema de console em Java 17 / Spring Boot 3.5 (H2 + JPA) para a Global Solution de POO (FIAP), tema **CitizenTech**, recorte **Calor e energia**. Ele lê sensores de uma zona urbana, guarda as medições, prevê a próxima leitura e emite alertas quando a previsão ultrapassa limiares de risco.

## Público beneficiado e decisão apoiada

- **Quem sofre o problema:** moradores de uma zona sob onda de calor (risco de queda de energia e de calor extremo dentro de casa) e as equipes de manutenção da rede elétrica.
- **Decisão que o sistema apoia:** acionar manutenção preventiva, redistribuir carga entre subestações e avisar a comunidade antes da sobrecarga. O sistema sugere, a pessoa decide.

## Como rodar

Pré-requisito: Java 17+. Maven não precisa estar instalado: o projeto traz o Maven Wrapper (`mvnw`, `mvnw.cmd` e `.mvn/wrapper/`). Na primeira execução o wrapper baixa o Maven 3.9.11 (precisa de internet) para `~/.m2/wrapper`.

```
# Windows
.\mvnw.cmd spring-boot:run
.\mvnw.cmd test

# Linux / macOS
./mvnw spring-boot:run
./mvnw test
```

Com Maven instalado, `mvn spring-boot:run` e `mvn test` também funcionam.

**Nota sobre o wrapper:** o comando `mvn wrapper:wrapper` não conseguiu baixar o plugin (rede com timeout no ambiente de desenvolvimento). Os arquivos foram então criados a partir dos scripts oficiais do Apache Maven Wrapper 3.3.4 (modo `only-script`, sem `maven-wrapper.jar`). O `mvnw.cmd` foi testado com uma distribuição do Maven já presente no cache; o download automático em si não pôde ser testado nesse ambiente. Se o download falhar na sua rede, use um Maven instalado.

### Menu

| Opção | Ação                                                                                              |
| ----- | ------------------------------------------------------------------------------------------------- |
| 1     | Verificar sensores (mostra "funcionando" ou "FORA DE OPERAÇÃO" e lê/grava uma medição de cada um) |
| 2     | Rodar previsão (escolhe sensor e estratégia; opção 0 usa a estratégia padrão)                     |
| 3     | Ver histórico (últimas 20 medições do sensor e os alertas dele)                                   |
| 4     | Gerenciar dados do território: população, limiares, estratégia padrão, janela da previsão         |
| 5     | Status completo                                                                                   |
| 6     | Coletar várias leituras de cada sensor                                                            |
| 7     | Listar alertas: todos (opção 0) ou só de um sensor                                                |
| 8     | Ligar/desligar um sensor simulado                                                                 |
| 9     | Injetar leitura defeituosa (a próxima leitura do sensor sai fora da faixa)                        |
| 0     | Sair                                                                                              |

- Para ter previsão é preciso coletar leituras antes (opção 6): mínimo de 5 para média móvel e 6 para comparação.
- **Demonstração das exceções:** desligue um sensor (8) e use a opção 1: aparece "FORA DE OPERAÇÃO" e "Fonte indisponível: ..." (`FonteIndisponivelException`). Injete uma leitura defeituosa (9) e use a opção 1 ou 6: aparece "Leitura descartada: ..." (`DadosInvalidosException` lançada por `Medicao.de`). O valor inválido nunca chega ao banco.
- Banco H2 **em arquivo**: `./data/territorydb`. Ele é criado automaticamente na primeira execução (`ddl-auto=update`), então não vem versionado: a pasta `data/` está no `.gitignore`. Para limpar o histórico, pare o app e apague a pasta `data/`.
- Não há dados pessoais. Todas as leituras são simuladas com `Random`.

## Testes

- Testes unitários puros (JUnit 5 + Mockito, sem Spring nem banco) para domínio, serviço, fábrica, configuração e menu (o menu roda com `Scanner` roteirizado e repositórios simulados).
- `RepositoriosJpaTest` (`@DataJpaTest`) grava e consulta medições e alertas por sensor, em ordem, num H2 embutido em memória. Ele usa o perfil `test`, que desliga o menu de console, e não toca em `./data/territorydb`.
- Padrão AAA (Arrange / Act / Assert) com comentários. Nos `assertThrows` a chamada fica no lambda, então Act e Assert aparecem juntos (comentado em cada teste).
- `ConfiguracaoSistema` é um Singleton compartilhado, então os testes que o usam chamam `restaurarPadroes()` em `@BeforeEach` e `@AfterEach` para não vazar estado.
- A primeira classe de teste que usa Mockito demora alguns segundos: o Mockito se anexa ao JVM (inline mock maker). Não há `sleep` nem contexto Spring nos testes de serviço.

## Estrutura de pacotes

Base `com.fiap.territory`:

- `dominio`: `ComponenteTerritorial` (abstrata) e `RegiaoMonitorada`; `Sensor` (interface) com `SensorClima`, `SensorEnergia`, `SensorTransformador` (via `SensorSimulado`); `DadosTerritorio`; `EstrategiaPrevisao` (abstrata) com `PrevisaoMediaMovel` e `PrevisaoComparacao`; entidades `Medicao` e `Alerta`; enums `NivelAlerta` e `StatusTerritorio`.
- `excecao`: `DadosInvalidosException` e `FonteIndisponivelException`.
- `persistencia`: `MedicaoRepository` e `AlertaRepository` (Spring Data JPA).
- `servico`: `ServicoMonitoramento`, `FabricaPrevisao`, `TipoPrevisao`, `ConfiguracaoSistema`, `ConfiguracaoBeans`, `ResultadoPrevisao`.
- Raiz: `SistemaMonitoramento` (main) e `MenuConsole` (loop com `Scanner`).

**Entidades JPA em `dominio`:** `Medicao` e `Alerta` ficam em `dominio` de propósito, porque a estrutura sugerida no enunciado coloca as classes de domínio ali e `Medicao.de(...)` e `Alerta` carregam as validações do negócio. Em um sistema maior valeria separar entidade JPA de objeto de domínio; aqui isso dobraria as classes sem ganho real.

## Decisões da equipe

- **Escala 0-100 única:** cada sensor tem sua faixa (ex.: clima 0 a 50 °C) e o valor vira índice `(valor-min)/(max-min)*100`. A medição guarda o valor bruto e a previsão é calculada sobre valores brutos. Só o valor previsto é normalizado, para comparar com os limiares.
- **Limiares globais 60/80/95** (atenção/alerta/crítico), iguais para todos os sensores e alteráveis no menu (devem ser positivos e crescentes).
- **Exceções checked:** obrigam quem chama a tratar o erro (dado inválido ou sensor fora do ar).
- **Mínimo de leituras:** 5 na média móvel e 6 na comparação (2 períodos de 3). Abaixo disso a previsão é recusada.
- **Janela configurável:** a `ConfiguracaoSistema` guarda a janela (padrão 5, mínimo 5) e a estratégia padrão (média móvel). A `FabricaPrevisao` recebe a janela ao criar a estratégia: a média móvel usa a janela inteira e a comparação usa dois períodos de metade da janela (arredondada para cima), então janela 5 dá 2 x 3 = 6 leituras. Não há janelas fixas espalhadas pelo código.
- **Fórmula da comparação:** `previsto = média recente + (média recente - média do período anterior)`. O enunciado não define fórmula, esta é simples de explicar.
- **Status da região:** a `RegiaoMonitorada` guarda só o último nível de alerta de cada sensor; o status é sempre derivado do maior nível (sem alerta = Normal), sem campo de status separado. A população existe só em `DadosTerritorio`. Ao iniciar, os níveis são recalculados a partir do último alerta salvo de cada sensor, sem tabela nova. Limitação: se a última previsão do sensor foi normal, o último alerta antigo ainda conta.
- **Alerta duplicado:** antes de gravar, o serviço compara com o último alerta do mesmo sensor. Se o **nível** e a **mensagem** forem iguais (a mensagem traz sensor, valor previsto, índice e limite), o alerta não é gravado de novo e o menu avisa "já registrado". Se o nível ou o valor previsto mudar, grava um novo. Coberto por `ServicoMonitoramentoTest` e `AlertaTest`.
- **Histórico limitado no banco:** o histórico por sensor usa `findTop20BySensorIdOrderByIdDesc` (só 20 linhas saem do banco) e `countBySensorId` para o total. Alertas podem ser filtrados por sensor (`findBySensorIdOrderByDataHoraDesc`), sem concatenação de SQL.
- **Sensor transformador no lugar de água:** combina melhor com calor e energia.

## Padrões criacionais

- **Factory (`FabricaPrevisao`):** quem pede uma previsão só informa o `TipoPrevisao` (e a janela da configuração). A fábrica usa um mapa tipo -> criador, então uma estratégia nova é uma linha nova, sem `if/instanceof` espalhado pelo serviço e pelo menu.
- **Singleton (`ConfiguracaoSistema`):** limiares, população, estratégia padrão e janela precisam ser os mesmos em todo o sistema (o menu altera e o serviço lê). Uma instância única evita configurações divergentes. Ele é entregue ao serviço por construtor (bean em `ConfiguracaoBeans`), o que permite trocar por outro objeto nos testes, e tem `restaurarPadroes()` para isolar o estado entre testes. É mutável e sem sincronização: aceitável porque o menu é de uma única thread.
- **Por que não Builder:** os objetos do projeto têm poucos atributos obrigatórios e são criados por construtores ou métodos estáticos que já validam. Builder só acrescentaria código sem resolver problema real.

## Reflexão crítica

**Quando o sistema deve se calar?** Com menos de 5 leituras (6 na comparação) a previsão não é confiável, então o sistema recusa e diz quantas leituras faltam, em vez de chutar. Também não gera alerta quando o índice previsto fica abaixo de 60, não repete o mesmo alerta e não lê sensor fora de operação nem grava leitura fora da faixa.

**O que os dados não representam?** São simulados com `Random`, não vêm de sensores reais. Há um sensor por tipo para a zona inteira, então ele não representa um ponto específico (um bairro ou transformador). Um limiar global simplifica e deixa de fora diferenças locais, como casas com idosos ou hospitais, que toleram menos risco.

**Por que o alerta apoia e não decide?** O alerta diz que a previsão passou de um limite, mas não conhece o contexto: obra em andamento, evento na região, sensor descalibrado. Quem decide acionar manutenção, redistribuir carga ou avisar moradores é a equipe, que pode julgar e assumir a responsabilidade. Um aviso falso automático também custa confiança da comunidade.

## Uso de IA

A IA (assistente de código) foi usada em várias partes do projeto, em iterações guiadas pela equipe. Não há marcação em todos os trechos gerados, então esta seção é o registro de referência.

**Onde foi usada e o que foi gerado ou alterado:**

- Código de domínio e serviço: `Sensor`/`SensorSimulado` e subclasses, `Medicao`, `Alerta`, `NivelAlerta`, `DadosTerritorio`, `EstrategiaPrevisao` e as duas previsões, `RegiaoMonitorada`, `FabricaPrevisao`, `ConfiguracaoSistema`, `ServicoMonitoramento` (geração inicial e, na revisão final, ajustes de janela configurável, alerta duplicado, histórico limitado e fonte única de população/status).
- Persistência: repositórios Spring Data (`MedicaoRepository`, `AlertaRepository`) e `application.properties`.
- Menu: `MenuConsole` e `SistemaMonitoramento` (opções de ligar/desligar sensor, injetar leitura defeituosa e listar alertas por sensor foram acrescentadas na revisão final).
- Testes: os testes unitários, `RepositoriosJpaTest` (`@DataJpaTest`) e `MenuConsoleTest`.
- Maven Wrapper: `mvnw` e `mvnw.cmd` copiados dos scripts oficiais do Apache Maven Wrapper 3.3.4.
- Este README.
- Marcações pontuais de "Auxilio de IA" existem só em `SistemaMonitoramento` e `application.properties`; o restante não está marcado no código.

**Como foi revisado e validado:**

- Leitura do código gerado e das mudanças, procurando regras erradas, duplicação e código sem uso.
- Testes executados com `mvn test` (JUnit 5, Mockito e `@DataJpaTest`), com valores calculados à mão nos casos de previsão, limites de nível e alertas.
- Execução do menu com entrada roteirizada: sensor desligado ("FORA DE OPERAÇÃO" e "Fonte indisponível"), leitura defeituosa ("Leitura descartada") e alerta repetido não gravado de novo.
- Ajustes manuais pela equipe depois da geração.

**[PREENCHER pela equipe]** (confirmar os detalhes reais):

- Ferramenta/modelo de IA usado: **\_\_**
- Quem revisou cada parte e em que data: **\_\_**
- Trechos que a equipe reescreveu à mão: **\_\_**

## Divisão de tarefas

| Integrante      | Responsabilidade sugerida               |
| --------------- | --------------------------------------- |
| Gabriel Fidalgo | Domínio + exceções                      |
| Gustavo Maia    | Persistência (entidades e repositórios) |
| Gustavo Rossi   | Previsões + alertas +                   |
