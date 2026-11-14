package com.fiap.territory.dominio;

import com.fiap.territory.excecao.DadosInvalidosException;

// limiares globais (um só jogo de valores pra todos os sensores). Valem pro índice 0-100 de cada sensor
// (ver Sensor.normalizar), não pro valor bruto em °C ou %.
public class DadosTerritorio {

    public static final double LIMIAR_ATENCAO_PADRAO = 60.0;
    public static final double LIMIAR_ALERTA_PADRAO = 80.0;
    public static final double LIMIAR_CRITICO_PADRAO = 95.0;

    private int populacao;
    private double limiarAtencao;
    private double limiarAlerta;
    private double limiarCritico;

    public DadosTerritorio(int populacao) throws DadosInvalidosException {
        this(populacao, LIMIAR_ATENCAO_PADRAO, LIMIAR_ALERTA_PADRAO, LIMIAR_CRITICO_PADRAO);
    }

    public DadosTerritorio(int populacao, double limiarAtencao, double limiarAlerta, double limiarCritico)
            throws DadosInvalidosException {
        setPopulacao(populacao);
        alterarLimiares(limiarAtencao, limiarAlerta, limiarCritico);
    }

    public int getPopulacao() {
        return populacao;
    }

    public void setPopulacao(int populacao) throws DadosInvalidosException {
        if (populacao < 0) {
            throw new DadosInvalidosException("A população não pode ser negativa: " + populacao);
        }
        this.populacao = populacao;
    }

    // Validação atômica: só altera o estado se os três valores forem positivos e estritamente crescentes.
    public void alterarLimiares(double atencao, double alerta, double critico) throws DadosInvalidosException {
        if (atencao <= 0 || alerta <= 0 || critico <= 0) {
            throw new DadosInvalidosException("Os limiares devem ser maiores que zero.");
        }
        if (!(atencao < alerta && alerta < critico)) {
            throw new DadosInvalidosException(String.format(
                    "Os limiares devem ser estritamente crescentes (atenção < alerta < crítico): %.2f, %.2f, %.2f",
                    atencao, alerta, critico));
        }
        this.limiarAtencao = atencao;
        this.limiarAlerta = alerta;
        this.limiarCritico = critico;
    }

    public double getLimiarAtencao() {
        return limiarAtencao;
    }

    public double getLimiarAlerta() {
        return limiarAlerta;
    }

    public double getLimiarCritico() {
        return limiarCritico;
    }
}
