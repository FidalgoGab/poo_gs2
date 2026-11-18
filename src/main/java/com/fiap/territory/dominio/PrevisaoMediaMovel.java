package com.fiap.territory.dominio;

import com.fiap.territory.excecao.DadosInvalidosException;

import java.util.List;

public class PrevisaoMediaMovel extends EstrategiaPrevisao {

    private final int janela;

    public PrevisaoMediaMovel(int janela) throws DadosInvalidosException {
        super("Média Móvel", exigirJanelaPositiva(janela));
        this.janela = janela;
    }

    // Assume histórico em ordem cronológica: da leitura mais antiga para a mais recente.
    @Override
    public double prever(List<Double> historico) throws DadosInvalidosException {
        super.prever(historico);
        return historico.subList(historico.size() - janela, historico.size())
                .stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElseThrow(() -> new DadosInvalidosException("Não foi possível calcular a média."));
    }

    @Override
    public String descrever(double valorPrevisto) {
        return String.format("Média das últimas %d leituras: %.2f", janela, valorPrevisto);
    }

    private static int exigirJanelaPositiva(int janela) throws DadosInvalidosException {
        if (janela <= 0) {
            throw new DadosInvalidosException("A janela da média móvel deve ser maior que zero.");
        }
        return janela;
    }
}
