package com.fiap.territory;

import com.fiap.territory.dominio.Alerta;
import com.fiap.territory.dominio.Medicao;
import com.fiap.territory.dominio.Sensor;
import com.fiap.territory.excecao.DadosInvalidosException;
import com.fiap.territory.excecao.FonteIndisponivelException;
import com.fiap.territory.servico.ResultadoPrevisao;
import com.fiap.territory.servico.ServicoMonitoramento;
import com.fiap.territory.servico.TipoPrevisao;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Scanner;

public class MenuConsole {

    private final ServicoMonitoramento servico;
    private final Scanner scanner;
    private boolean entradaEncerrada;

    // O Scanner é de quem chama: aqui nunca é fechado, pra não fechar o System.in.
    public MenuConsole(ServicoMonitoramento servico, Scanner scanner) {
        this.servico = servico;
        this.scanner = scanner;
    }

    public void executar() {
        boolean continuar = true;
        while (continuar && !entradaEncerrada) {
            exibirMenu();
            OptionalInt opcao = lerInteiro("Opção: ");
            if (opcao.isPresent()) {
                continuar = tratarOpcao(opcao.getAsInt());
            }
        }
        System.out.println("Encerrando o sistema.");
    }

    private boolean tratarOpcao(int opcao) {
        try {
            switch (opcao) {
                case 1 -> verificarSensores();
                case 2 -> rodarPrevisao();
                case 3 -> verHistorico();
                case 4 -> gerenciarTerritorio();
                case 5 -> System.out.println(servico.statusCompleto());
                case 6 -> coletarVariasLeituras();
                case 7 -> listarAlertas();
                case 8 -> ligarOuDesligarSensor();
                case 9 -> injetarLeituraDefeituosa();
                case 0 -> {
                    return false;
                }
                default -> System.out.println("Opção inválida: " + opcao);
            }
        } catch (DadosInvalidosException e) {
            System.out.println("Dados inválidos: " + e.getMessage());
        }
        return true;
    }

    private void exibirMenu() {
        System.out.println();
        System.out.println("=== Territory Predictive Insights ===");
        System.out.println("1 - Verificar sensores (lê e grava uma medição)");
        System.out.println("2 - Rodar previsão");
        System.out.println("3 - Ver histórico");
        System.out.println("4 - Gerenciar dados do território");
        System.out.println("5 - Status completo");
        System.out.println("6 - Coletar várias leituras");
        System.out.println("7 - Listar alertas (todos ou por sensor)");
        System.out.println("8 - Ligar/desligar sensor simulado");
        System.out.println("9 - Injetar leitura defeituosa (simulação de falha)");
        System.out.println("0 - Sair");
    }

    private void verificarSensores() {
        servico.verificarSensores().forEach((sensor, funcionando) -> System.out.printf(
                "%s (%s): %s%n", sensor.getIdentificador(), sensor.retornarTipo(),
                funcionando ? "funcionando" : "FORA DE OPERAÇÃO"));
        coletarLeituras(1);
    }

    private void coletarVariasLeituras() {
        lerInteiro("Quantas leituras de cada sensor? ").ifPresent(quantidade -> {
            if (quantidade <= 0) {
                System.out.println("A quantidade deve ser maior que zero.");
            } else {
                coletarLeituras(quantidade);
            }
        });
    }

    // Cada sensor é tratado separado: um sensor com defeito não impede a leitura dos outros.
    private void coletarLeituras(int quantidade) {
        int gravadas = 0;
        for (int i = 0; i < quantidade; i++) {
            for (Sensor sensor : servico.getSensores()) {
                gravadas += lerUmaVez(sensor, quantidade == 1);
            }
        }
        System.out.println("Medições gravadas: " + gravadas);
    }

    private int lerUmaVez(Sensor sensor, boolean detalhar) {
        try {
            Medicao medicao = servico.lerESalvarMedicao(sensor);
            if (detalhar) {
                System.out.printf("%s: %.1f %s (índice %.1f/100)%n", sensor.getIdentificador(), medicao.getValor(),
                        sensor.unidade(), sensor.normalizar(medicao.getValor()));
            }
            return 1;
        } catch (FonteIndisponivelException e) {
            System.out.println("Fonte indisponível: " + e.getMessage());
        } catch (DadosInvalidosException e) {
            System.out.println("Leitura descartada: " + e.getMessage());
        }
        return 0;
    }

    private void ligarOuDesligarSensor() {
        Sensor sensor = escolherSensor();
        if (sensor == null) {
            return;
        }
        boolean novoEstado = !sensor.verificarFuncionamento();
        sensor.definirFuncionamento(novoEstado);
        System.out.printf("Sensor %s agora está %s.%n", sensor.getIdentificador(),
                novoEstado ? "funcionando" : "FORA DE OPERAÇÃO");
    }

    // A próxima leitura do sensor sai fora da faixa. Para ver o efeito, use a opção 1 ou 6.
    private void injetarLeituraDefeituosa() {
        Sensor sensor = escolherSensor();
        if (sensor == null) {
            return;
        }
        sensor.injetarLeituraDefeituosa();
        System.out.println("A próxima leitura de " + sensor.getIdentificador() + " virá defeituosa (fora da faixa).");
    }

    private void rodarPrevisao() throws DadosInvalidosException {
        Sensor sensor = escolherSensor();
        if (sensor == null) {
            return;
        }
        Optional<TipoPrevisao> tipo = escolherTipoPrevisao();
        if (tipo.isEmpty()) {
            return;
        }
        exibirResultado(servico.rodarPrevisao(sensor, tipo.get()));
    }

    private void exibirResultado(ResultadoPrevisao resultado) {
        System.out.printf("Previsto: %.1f %s (índice %.1f/100)%n", resultado.valorPrevisto(), resultado.unidade(),
                resultado.indice());
        System.out.println("Significado: " + resultado.interpretacao());
        String prefixo = resultado.alertaRepetido() ? "ALERTA (já registrado, não gravado de novo): " : "ALERTA: ";
        System.out.println(resultado.alerta().map(Alerta::getMensagem).map(m -> prefixo + m)
                .orElse("Sem alerta: abaixo do limiar de atenção."));
    }

    private void verHistorico() {
        Sensor sensor = escolherSensor();
        if (sensor == null) {
            return;
        }
        List<Medicao> medicoes = servico.buscarHistoricoPorSensor(sensor.getIdentificador());
        System.out.printf("Medições de %s (mais recentes primeiro, %d de %d):%n", sensor.getIdentificador(),
                medicoes.size(), servico.contarMedicoes(sensor.getIdentificador()));
        medicoes.forEach(m -> System.out.printf("  %s | %.1f %s%n", m.getDataHora(), m.getValor(), sensor.unidade()));
        exibirAlertas(servico.listarAlertas(sensor.getIdentificador()));
    }

    // Opção 0 = todos os sensores.
    private void listarAlertas() {
        List<Sensor> sensores = servico.getSensores();
        System.out.println("0 - Todos os sensores");
        exibirSensores(sensores);
        OptionalInt escolha = lerInteiro("Sensor: ");
        if (escolha.isEmpty()) {
            return;
        }
        int numero = escolha.getAsInt();
        if (numero == 0) {
            exibirAlertas(servico.listarAlertas());
        } else if (numero < 0 || numero > sensores.size()) {
            System.out.println("Sensor inexistente: " + numero);
        } else {
            exibirAlertas(servico.listarAlertas(sensores.get(numero - 1).getIdentificador()));
        }
    }

    private void exibirSensores(List<Sensor> sensores) {
        for (int i = 0; i < sensores.size(); i++) {
            System.out.printf("%d - %s (%s)%n", i + 1, sensores.get(i).getIdentificador(),
                    sensores.get(i).retornarTipo());
        }
    }

    private void exibirAlertas(List<Alerta> alertas) {
        System.out.println("Alertas emitidos: " + alertas.size());
        alertas.forEach(a -> System.out.printf("  %s | %s%n", a.getDataHora(), a.getMensagem()));
    }

    private void gerenciarTerritorio() throws DadosInvalidosException {
        System.out.println("1 - Alterar população | 2 - Alterar limiares (índice 0-100)");
        System.out.println("3 - Alterar estratégia padrão | 4 - Alterar janela da previsão");
        OptionalInt escolha = lerInteiro("Opção: ");
        if (escolha.isEmpty()) {
            return;
        }
        switch (escolha.getAsInt()) {
            case 1 -> lerInteiro("Nova população: ").ifPresent(this::alterarPopulacao);
            case 2 -> alterarLimiares();
            case 3 -> alterarEstrategiaPadrao();
            case 4 -> lerInteiro("Nova janela (mínimo 5): ").ifPresent(this::alterarJanela);
            default -> System.out.println("Opção inválida: " + escolha.getAsInt());
        }
    }

    private void alterarEstrategiaPadrao() throws DadosInvalidosException {
        Optional<TipoPrevisao> tipo = escolherTipoPrevisao();
        if (tipo.isPresent()) {
            servico.alterarEstrategiaPadrao(tipo.get());
            System.out.println("Estratégia padrão: " + tipo.get().getDescricao());
        }
    }

    private void alterarJanela(int janela) {
        try {
            servico.alterarJanela(janela);
            System.out.println("Janela atualizada.");
        } catch (DadosInvalidosException e) {
            System.out.println("Dados inválidos: " + e.getMessage());
        }
    }

    private void alterarPopulacao(int populacao) {
        try {
            servico.alterarPopulacao(populacao);
            System.out.println("População atualizada.");
        } catch (DadosInvalidosException e) {
            System.out.println("Dados inválidos: " + e.getMessage());
        }
    }

    private void alterarLimiares() throws DadosInvalidosException {
        OptionalDouble atencao = lerDecimal("Limiar de atenção: ");
        OptionalDouble alerta = lerDecimal("Limiar de alerta: ");
        OptionalDouble critico = lerDecimal("Limiar crítico: ");
        if (atencao.isPresent() && alerta.isPresent() && critico.isPresent()) {
            servico.alterarLimiares(atencao.getAsDouble(), alerta.getAsDouble(), critico.getAsDouble());
            System.out.println("Limiares atualizados.");
        }
    }

    private Sensor escolherSensor() {
        List<Sensor> sensores = servico.getSensores();
        exibirSensores(sensores);
        OptionalInt escolha = lerInteiro("Sensor: ");
        if (escolha.isPresent() && (escolha.getAsInt() < 1 || escolha.getAsInt() > sensores.size())) {
            System.out.println("Sensor inexistente: " + escolha.getAsInt());
            return null;
        }
        return escolha.isPresent() ? sensores.get(escolha.getAsInt() - 1) : null;
    }

    // Opção 0 = estratégia padrão da configuração.
    private Optional<TipoPrevisao> escolherTipoPrevisao() {
        TipoPrevisao[] tipos = TipoPrevisao.values();
        System.out.println("0 - Padrão (" + servico.getEstrategiaPadrao().getDescricao() + ")");
        for (int i = 0; i < tipos.length; i++) {
            System.out.printf("%d - %s%n", i + 1, tipos[i].getDescricao());
        }
        OptionalInt escolha = lerInteiro("Estratégia: ");
        if (escolha.isEmpty()) {
            return Optional.empty();
        }
        if (escolha.getAsInt() == 0) {
            return Optional.of(servico.getEstrategiaPadrao());
        }
        if (escolha.getAsInt() < 1 || escolha.getAsInt() > tipos.length) {
            System.out.println("Estratégia inexistente: " + escolha.getAsInt());
            return Optional.empty();
        }
        return Optional.of(tipos[escolha.getAsInt() - 1]);
    }

    private String lerLinha(String pergunta) {
        System.out.print(pergunta);
        if (!scanner.hasNextLine()) {
            entradaEncerrada = true;
            return null;
        }
        return scanner.nextLine().trim();
    }

    private OptionalInt lerInteiro(String pergunta) {
        String linha = lerLinha(pergunta);
        if (linha == null) {
            return OptionalInt.empty();
        }
        try {
            return OptionalInt.of(Integer.parseInt(linha));
        } catch (NumberFormatException e) {
            System.out.println("Digite um número inteiro (recebido: \"" + linha + "\").");
            return OptionalInt.empty();
        }
    }

    private OptionalDouble lerDecimal(String pergunta) {
        String linha = lerLinha(pergunta);
        if (linha == null) {
            return OptionalDouble.empty();
        }
        try {
            return OptionalDouble.of(Double.parseDouble(linha.replace(',', '.')));
        } catch (NumberFormatException e) {
            System.out.println("Digite um número (recebido: \"" + linha + "\").");
            return OptionalDouble.empty();
        }
    }
}