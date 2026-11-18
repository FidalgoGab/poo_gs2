package com.fiap.territory.servico;

import com.fiap.territory.dominio.EstrategiaPrevisao;
import com.fiap.territory.dominio.PrevisaoComparacao;
import com.fiap.territory.dominio.PrevisaoMediaMovel;
import com.fiap.territory.excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FabricaPrevisaoTest {

    @Test
    void deveCriarEstrategiaCorrespondenteParaCadaTipo() throws Exception {
        // Arrange
        FabricaPrevisao fabrica = new FabricaPrevisao();

        // Act
        EstrategiaPrevisao media = fabrica.criar(TipoPrevisao.MEDIA_MOVEL, 5);
        EstrategiaPrevisao comparacao = fabrica.criar(TipoPrevisao.COMPARACAO, 5);

        // Assert
        assertInstanceOf(PrevisaoMediaMovel.class, media);
        assertInstanceOf(PrevisaoComparacao.class, comparacao);
    }

    @Test
    void deveDerivarOsMinimosDeLeiturasDaJanelaRecebida() throws Exception {
        // Arrange
        FabricaPrevisao fabrica = new FabricaPrevisao();

        // Act
        EstrategiaPrevisao media = fabrica.criar(TipoPrevisao.MEDIA_MOVEL, 5);
        EstrategiaPrevisao comparacao = fabrica.criar(TipoPrevisao.COMPARACAO, 5);
        EstrategiaPrevisao mediaMaior = fabrica.criar(TipoPrevisao.MEDIA_MOVEL, 8);

        // Assert: janela 5 -> média 5 leituras e comparação 2 x 3 = 6; janela 8 -> média 8
        assertEquals(5, media.getMinimoLeituras());
        assertEquals(6, comparacao.getMinimoLeituras());
        assertEquals(8, mediaMaior.getMinimoLeituras());
    }

    @Test
    void deveLancarDadosInvalidosParaTipoNulo() {
        // Arrange
        FabricaPrevisao fabrica = new FabricaPrevisao();

        // Act + Assert: assertThrows executa a chamada (Act) e verifica a exceção (Assert)
        assertThrows(DadosInvalidosException.class, () -> fabrica.criar(null, 5));
    }
}
