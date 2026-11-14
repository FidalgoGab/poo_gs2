package com.fiap.territory.dominio;

import com.fiap.territory.excecao.FonteIndisponivelException;

public interface Sensor {

    String getIdentificador();

    double lerValor() throws FonteIndisponivelException;

    boolean verificarFuncionamento();

    // Liga/desliga o sensor (usado no menu para demonstrar FonteIndisponivelException).
    void definirFuncionamento(boolean funcionando);

    // A próxima leitura sai fora da faixa do sensor (usado para demonstrar DadosInvalidosException).
    void injetarLeituraDefeituosa();

    String retornarTipo();

    String unidade();

    double faixaMinima();

    double faixaMaxima();

    // escala única 0-100. Cada sensor tem sua faixa (ex.: clima 0..50 °C) e o valor vira um índice
    // (valor-min)/(max-min)*100. Assim os mesmos limiares 60/80/95 servem pra todo mundo.
    default double normalizar(double valor) {
        return (valor - faixaMinima()) / (faixaMaxima() - faixaMinima()) * 100.0;
    }
}
