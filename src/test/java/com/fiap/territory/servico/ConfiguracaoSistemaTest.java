package com.fiap.territory.servico;

import com.fiap.territory.excecao.DadosInvalidosException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfiguracaoSistemaTest {

    // O singleton é compartilhado: restaura os padrões antes e depois de cada teste.
    @BeforeEach
    void restaurarAntes() {
        ConfiguracaoSistema.getInstance().restaurarPadroes();
    }

    @AfterEach
    void restaurarDepois() {
        ConfiguracaoSistema.getInstance().restaurarPadroes();
    }

    @Test
    void deveRetornarSempreAMesmaInstancia() {
        // Arrange
        ConfiguracaoSistema primeira = ConfiguracaoSistema.getInstance();

        // Act
        ConfiguracaoSistema segunda = ConfiguracaoSistema.getInstance();

        // Assert
        assertSame(primeira, segunda);
    }

    @Test
    void deveIniciarComValoresPadrao() {
        // Arrange
        ConfiguracaoSistema config = ConfiguracaoSistema.getInstance();

        // Act
        int janela = config.getTamanhoJanela();
        TipoPrevisao estrategia = config.getEstrategiaPadrao();

        // Assert
        assertEquals(5, janela);
        assertEquals(TipoPrevisao.MEDIA_MOVEL, estrategia);
        assertEquals(60.0, config.getDadosTerritorio().getLimiarAtencao(), 0.0001);
    }

    @Test
    void deveRestaurarPadroesDepoisDeAlteracoes() throws Exception {
        // Arrange
        ConfiguracaoSistema config = ConfiguracaoSistema.getInstance();
        config.getDadosTerritorio().alterarLimiares(10.0, 20.0, 30.0);
        config.getDadosTerritorio().setPopulacao(7);
        config.setEstrategiaPadrao(TipoPrevisao.COMPARACAO);
        config.setTamanhoJanela(9);

        // Act
        config.restaurarPadroes();

        // Assert
        assertEquals(60.0, config.getDadosTerritorio().getLimiarAtencao(), 0.0001);
        assertEquals(80.0, config.getDadosTerritorio().getLimiarAlerta(), 0.0001);
        assertEquals(95.0, config.getDadosTerritorio().getLimiarCritico(), 0.0001);
        assertEquals(ConfiguracaoSistema.POPULACAO_PADRAO, config.getDadosTerritorio().getPopulacao());
        assertEquals(TipoPrevisao.MEDIA_MOVEL, config.getEstrategiaPadrao());
        assertEquals(5, config.getTamanhoJanela());
    }

    @Test
    void deveLancarDadosInvalidosParaJanelaMenorQueOMinimo() {
        // Arrange
        ConfiguracaoSistema config = ConfiguracaoSistema.getInstance();

        // Act + Assert: assertThrows executa a chamada (Act) e verifica a exceção (Assert)
        assertThrows(DadosInvalidosException.class, () -> config.setTamanhoJanela(4));
    }

    @Test
    void deveLancarDadosInvalidosParaEstrategiaNula() {
        // Arrange
        ConfiguracaoSistema config = ConfiguracaoSistema.getInstance();

        // Act + Assert: assertThrows executa a chamada (Act) e verifica a exceção (Assert)
        assertThrows(DadosInvalidosException.class, () -> config.setEstrategiaPadrao(null));
    }
}
