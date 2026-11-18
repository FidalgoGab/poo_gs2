package com.fiap.territory.servico;

import com.fiap.territory.dominio.DadosTerritorio;
import com.fiap.territory.excecao.DadosInvalidosException;

// Singleton com holder (thread-safe e simples). O serviço Spring recebe ele por construtor
// (bean criado em ConfiguracaoBeans com getInstance), então nos testes dá pra passar outro objeto/mock.
public class ConfiguracaoSistema {

    static final int POPULACAO_PADRAO = 50_000;
    static final int JANELA_PADRAO = 5;
    static final TipoPrevisao ESTRATEGIA_PADRAO = TipoPrevisao.MEDIA_MOVEL;

    private final DadosTerritorio dadosTerritorio;
    private TipoPrevisao estrategiaPadrao;
    private int tamanhoJanela;

    private ConfiguracaoSistema() {
        try {
            this.dadosTerritorio = new DadosTerritorio(POPULACAO_PADRAO);
        } catch (DadosInvalidosException e) {
            throw new IllegalStateException("Valores padrão do território são inválidos.", e);
        }
        restaurarPadroes();
    }

    private static class Holder {
        private static final ConfiguracaoSistema INSTANCIA = new ConfiguracaoSistema();
    }

    public static ConfiguracaoSistema getInstance() {
        return Holder.INSTANCIA;
    }

    // Volta tudo ao padrão (população, limiares, estratégia e janela). Usado nos testes para não vazar estado.
    void restaurarPadroes() {
        try {
            dadosTerritorio.setPopulacao(POPULACAO_PADRAO);
            dadosTerritorio.alterarLimiares(DadosTerritorio.LIMIAR_ATENCAO_PADRAO,
                    DadosTerritorio.LIMIAR_ALERTA_PADRAO, DadosTerritorio.LIMIAR_CRITICO_PADRAO);
        } catch (DadosInvalidosException e) {
            throw new IllegalStateException("Valores padrão do território são inválidos.", e);
        }
        estrategiaPadrao = ESTRATEGIA_PADRAO;
        tamanhoJanela = JANELA_PADRAO;
    }

    public DadosTerritorio getDadosTerritorio() {
        return dadosTerritorio;
    }

    // Estratégia usada quando o usuário não escolhe uma na previsão.
    public TipoPrevisao getEstrategiaPadrao() {
        return estrategiaPadrao;
    }

    public void setEstrategiaPadrao(TipoPrevisao estrategiaPadrao) throws DadosInvalidosException {
        if (estrategiaPadrao == null) {
            throw new DadosInvalidosException("A estratégia padrão não pode ser nula.");
        }
        this.estrategiaPadrao = estrategiaPadrao;
    }

    public int getTamanhoJanela() {
        return tamanhoJanela;
    }

    // Janela passada à FabricaPrevisao. Mínimo 5 para manter o mínimo de leituras da previsão (5 e 6).
    public void setTamanhoJanela(int tamanhoJanela) throws DadosInvalidosException {
        if (tamanhoJanela < JANELA_PADRAO) {
            throw new DadosInvalidosException("O tamanho da janela deve ser no mínimo " + JANELA_PADRAO + ".");
        }
        this.tamanhoJanela = tamanhoJanela;
    }
}
