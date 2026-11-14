package com.fiap.territory.dominio;

import com.fiap.territory.excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DadosTerritorioTest {

    @Test
    void deveAlterarLimiaresValidos() throws Exception {
        // Arrange
        DadosTerritorio dados = new DadosTerritorio(1000);

        // Act
        dados.alterarLimiares(50.0, 70.0, 90.0);

        // Assert
        assertEquals(50.0, dados.getLimiarAtencao(), 0.0001);
        assertEquals(70.0, dados.getLimiarAlerta(), 0.0001);
        assertEquals(90.0, dados.getLimiarCritico(), 0.0001);
    }

    @Test
    void deveManterLimiaresAntigosQuandoNovosForemInvalidos() throws Exception {
        // Arrange
        DadosTerritorio dados = new DadosTerritorio(1000);

        // Act + Assert (exceção): fora de ordem (alerta < atenção); assertThrows executa a chamada
        assertThrows(DadosInvalidosException.class, () -> dados.alterarLimiares(70.0, 60.0, 90.0));

        // Assert (estado): continua 60/80/95
        assertEquals(60.0, dados.getLimiarAtencao(), 0.0001);
        assertEquals(80.0, dados.getLimiarAlerta(), 0.0001);
        assertEquals(95.0, dados.getLimiarCritico(), 0.0001);
    }

    @Test
    void deveLancarDadosInvalidosParaPopulacaoNegativa() throws Exception {
        // Arrange
        DadosTerritorio dados = new DadosTerritorio(1000);

        // Act + Assert (exceção): assertThrows executa a chamada
        assertThrows(DadosInvalidosException.class, () -> dados.setPopulacao(-1));

        // Assert (estado)
        assertEquals(1000, dados.getPopulacao());
    }
}
