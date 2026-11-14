package com.fiap.territory.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NivelAlertaTest {

    @ParameterizedTest
    @CsvSource({
            "59.99, ",
            "60.0, ATENCAO",
            "79.99, ATENCAO",
            "80.0, ALERTA",
            "94.99, ALERTA",
            "95.0, CRITICO",
            "100.0, CRITICO"
    })
    void deveEscolherNivelPeloLimiarAlcancadoComLimiaresPadrao(double indice, NivelAlerta esperado)
            throws Exception {
        // Arrange
        DadosTerritorio dados = new DadosTerritorio(1000);

        // Act
        Optional<NivelAlerta> nivel = NivelAlerta.paraIndice(indice, dados);

        // Assert
        assertEquals(Optional.ofNullable(esperado), nivel);
    }

    @Test
    void naoDeveGerarNivelAbaixoDoMinimoNemParaIndiceNegativo() throws Exception {
        // Arrange
        DadosTerritorio dados = new DadosTerritorio(1000);

        // Act
        Optional<NivelAlerta> zero = NivelAlerta.paraIndice(0.0, dados);
        Optional<NivelAlerta> negativo = NivelAlerta.paraIndice(-10.0, dados);

        // Assert
        assertEquals(Optional.empty(), zero);
        assertEquals(Optional.empty(), negativo);
    }

    @Test
    void deveRespeitarLimiaresAlteradosNosLimitesExatos() throws Exception {
        // Arrange
        DadosTerritorio dados = new DadosTerritorio(1000);
        dados.alterarLimiares(50.0, 70.0, 90.0);

        // Act
        Optional<NivelAlerta> abaixo = NivelAlerta.paraIndice(49.99, dados);
        Optional<NivelAlerta> exato = NivelAlerta.paraIndice(50.0, dados);

        // Assert
        assertEquals(Optional.empty(), abaixo);
        assertEquals(Optional.of(NivelAlerta.ATENCAO), exato);
    }
}
