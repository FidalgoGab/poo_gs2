package com.fiap.territory.dominio;

import com.fiap.territory.excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrevisaoComparacaoTest {

    @Test
    void deveProjetarMesmaVariacaoEntreOsDoisUltimosPeriodos() throws Exception {
        // Arrange: média anterior = 10, recente = 20, subiu 10
        PrevisaoComparacao previsao = new PrevisaoComparacao(3);
        List<Double> historico = List.of(10.0, 10.0, 10.0, 20.0, 20.0, 20.0);

        // Act
        double resultado = previsao.prever(historico);

        // Assert: 20 + (20 - 10) = 30
        assertEquals(30.0, resultado, 0.0001);
    }

    @Test
    void deveProjetarQuedaQuandoPeriodoRecenteForMenor() throws Exception {
        // Arrange: anterior = 40, recente = 30
        PrevisaoComparacao previsao = new PrevisaoComparacao(3);
        List<Double> historico = List.of(30.0, 40.0, 50.0, 20.0, 30.0, 40.0);

        // Act
        double resultado = previsao.prever(historico);

        // Assert: 30 + (30 - 40) = 20
        assertEquals(20.0, resultado, 0.0001);
    }

    @Test
    void deveLancarDadosInvalidosComMenosDeSeisLeituras() throws Exception {
        // Arrange
        PrevisaoComparacao previsao = new PrevisaoComparacao(3);
        List<Double> historico = List.of(1.0, 2.0, 3.0, 4.0, 5.0);

        // Act + Assert: assertThrows executa a chamada (Act) e verifica a exceção (Assert)
        assertThrows(DadosInvalidosException.class, () -> previsao.prever(historico));
    }

    @Test
    void deveLancarDadosInvalidosParaJanelaZero() {
        // Arrange
        int janelaInvalida = 0;

        // Act + Assert: assertThrows executa a chamada (Act) e verifica a exceção (Assert)
        assertThrows(DadosInvalidosException.class, () -> new PrevisaoComparacao(janelaInvalida));
    }
}
