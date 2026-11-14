package com.fiap.territory.dominio;

import com.fiap.territory.excecao.FonteIndisponivelException;

import java.util.Random;

public abstract class SensorSimulado implements Sensor {

    private final String identificador;
    private final Random random;
    private final double faixaMinima;
    private final double faixaMaxima;
    private final double simulacaoMinima;
    private final double simulacaoMaxima;
    private boolean funcionando = true;
    private boolean proximaLeituraDefeituosa;

    protected SensorSimulado(String identificador, Random random,
                             double faixaMinima, double faixaMaxima,
                             double simulacaoMinima, double simulacaoMaxima) {
        this.identificador = identificador;
        this.random = random;
        this.faixaMinima = faixaMinima;
        this.faixaMaxima = faixaMaxima;
        this.simulacaoMinima = simulacaoMinima;
        this.simulacaoMaxima = simulacaoMaxima;
    }

    @Override
    public double lerValor() throws FonteIndisponivelException {
        if (!funcionando) {
            throw new FonteIndisponivelException("Sensor " + identificador + " (" + retornarTipo() + ") está fora de operação.");
        }
        if (proximaLeituraDefeituosa) {
            proximaLeituraDefeituosa = false;
            return faixaMaxima + (faixaMaxima - faixaMinima);
        }
        return simulacaoMinima + random.nextDouble() * (simulacaoMaxima - simulacaoMinima);
    }

    @Override
    public void injetarLeituraDefeituosa() {
        this.proximaLeituraDefeituosa = true;
    }

    @Override
    public boolean verificarFuncionamento() {
        return funcionando;
    }

    @Override
    public void definirFuncionamento(boolean funcionando) {
        this.funcionando = funcionando;
    }

    @Override
    public String getIdentificador() {
        return identificador;
    }

    @Override
    public double faixaMinima() {
        return faixaMinima;
    }

    @Override
    public double faixaMaxima() {
        return faixaMaxima;
    }
}
