package com.fiap.territory.dominio;

import com.fiap.territory.excecao.DadosInvalidosException;

import java.util.List;

public class PrevisaoComparacao extends EstrategiaPrevisao {

    private final int janela;

    public PrevisaoComparacao(int janela) throws DadosInvalidosException {
        super("Comparação de Períodos", exigirJanelaPositiva(janela) * 2);
        this.janela = janela;
    }

    // previsto = média recente + (média recente - média do período anterior),
    // ou seja, se subiu X de um período pro outro, assume que sobe mais X. Precisa de 2 períodos (2 x janela) de leituras.
    @Override
    public double prever(List<Double> historico) throws DadosInvalidosException {
        super.prever(historico);
        int fim = historico.size();
        double mediaRecente = media(historico.subList(fim - janela, fim));
        double mediaAnterior = media(historico.subList(fim - 2 * janela, fim - janela));
        return mediaRecente + (mediaRecente - mediaAnterior);
    }

    @Override
    public String descrever(double valorPrevisto) {
        return String.format("Tendência entre os últimos 2 períodos de %d leituras: %.2f", janela, valorPrevisto);
    }

    private static double media(List<Double> valores) {
        return valores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private static int exigirJanelaPositiva(int janela) throws DadosInvalidosException {
        if (janela <= 0) {
            throw new DadosInvalidosException("A janela da comparação deve ser maior que zero.");
        }
        return janela;
    }
}
