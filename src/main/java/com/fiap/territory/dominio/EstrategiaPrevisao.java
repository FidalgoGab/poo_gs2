package com.fiap.territory.dominio;

import com.fiap.territory.excecao.DadosInvalidosException;

import java.util.List;

public abstract class EstrategiaPrevisao {

    // mínimo de leituras = o que a janela da estratégia precisa. Média móvel = 5 (igual ao exemplo do
    // enunciado) e comparação = 6 (2 períodos de 3), então ambas passam de 5 e ficam confiáveis.
    private final String nome;
    private final int minimoLeituras;

    protected EstrategiaPrevisao(String nome, int minimoLeituras) {
        this.nome = nome;
        this.minimoLeituras = minimoLeituras;
    }

    // Valida o histórico e devolve a última leitura como previsão ingênua; subclasses refinam via super.prever().
    public double prever(List<Double> historico) throws DadosInvalidosException {
        if (historico == null || historico.isEmpty()) {
            throw new DadosInvalidosException("O histórico de leituras está vazio.");
        }
        if (historico.size() < minimoLeituras) {
            throw new DadosInvalidosException(String.format(
                    "Histórico insuficiente para %s: %d leituras (mínimo %d).", nome, historico.size(), minimoLeituras));
        }
        if (historico.stream().anyMatch(valor -> valor == null || valor.isNaN() || valor.isInfinite())) {
            throw new DadosInvalidosException("O histórico contém leituras inválidas.");
        }
        return historico.get(historico.size() - 1);
    }

    public abstract String descrever(double valorPrevisto);

    public String getNome() {
        return nome;
    }

    public int getMinimoLeituras() {
        return minimoLeituras;
    }
}
