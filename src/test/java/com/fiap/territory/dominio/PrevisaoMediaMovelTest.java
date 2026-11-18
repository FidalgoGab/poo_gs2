package com.fiap.territory.dominio;

import com.fiap.territory.excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrevisaoMediaMovelTest {

    @Test
    void deveCalcularMediaDasCincoLeituras() throws Exception {
        // Arrange
        PrevisaoMediaMovel previsao = new PrevisaoMediaMovel(5);
        List<Double> historico = List.of(10.0, 20.0, 30.0, 40.0, 50.0);

        // Act
        double resultado = previsao.prever(historico);

        // Assert
        assertEquals(30.0, resultado, 0.0001);
    }

    @Test
    void deveUsarSomenteAsUltimasLeiturasDaJanela() throws Exception {
        // Arrange: as duas primeiras (1 e 2) ficam fora da janela de 5
        PrevisaoMediaMovel previsao = new PrevisaoMediaMovel(5);
        List<Double> historico = List.of(1.0, 2.0, 10.0, 20.0, 30.0, 40.0, 50.0);

        // Act
        double resultado = previsao.prever(historico);

        // Assert
        assertEquals(30.0, resultado, 0.0001);
    }

    @Test
    void deveLancarDadosInvalidosComMenosLeiturasQueAJanela() throws Exception {
        // Arrange
        PrevisaoMediaMovel previsao = new PrevisaoMediaMovel(5);
        List<Double> historico = List.of(10.0, 20.0, 30.0, 40.0);

        // Act + Assert: assertThrows executa a chamada (Act) e já devolve a exceção para o Assert
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class, () -> previsao.prever(historico));
        assertTrue(erro.getMessage().contains("insuficiente"));
    }
}
