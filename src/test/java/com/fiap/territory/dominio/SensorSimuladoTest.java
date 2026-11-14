package com.fiap.territory.dominio;

import com.fiap.territory.excecao.DadosInvalidosException;
import com.fiap.territory.excecao.FonteIndisponivelException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensorSimuladoTest {

    @Test
    void deveLerNormalmenteDepoisDeReligarOSensor() throws Exception {
        // Arrange
        SensorClima sensor = new SensorClima("CLIMA-T", new Random(1));
        sensor.definirFuncionamento(false);
        sensor.definirFuncionamento(true);

        // Act
        double valor = sensor.lerValor();

        // Assert
        assertTrue(sensor.verificarFuncionamento());
        assertTrue(valor >= 20.0 && valor <= 48.0);
    }

    @Test
    void deveLancarFonteIndisponivelComMensagemDoSensorDesligado() {
        // Arrange
        SensorEnergia sensor = new SensorEnergia("ENERGIA-T", new Random(1));
        sensor.definirFuncionamento(false);

        // Act + Assert: assertThrows executa a chamada (Act) e devolve a exceção para o Assert
        FonteIndisponivelException erro = assertThrows(FonteIndisponivelException.class, sensor::lerValor);
        assertTrue(erro.getMessage().contains("ENERGIA-T"));
    }

    @Test
    void deveProduzirValorForaDaFaixaSomenteNaProximaLeituraInjetada() throws Exception {
        // Arrange
        SensorTransformador sensor = new SensorTransformador("TRAFO-T", new Random(1));
        sensor.injetarLeituraDefeituosa();

        // Act
        double defeituosa = sensor.lerValor();
        double normal = sensor.lerValor();

        // Assert
        assertTrue(defeituosa > sensor.faixaMaxima());
        assertFalse(normal > sensor.faixaMaxima());
    }

    @Test
    void deveRejeitarLeituraDefeituosaEmMedicaoDe() throws Exception {
        // Arrange
        SensorClima sensor = new SensorClima("CLIMA-T", new Random(1));
        sensor.injetarLeituraDefeituosa();
        double valor = sensor.lerValor();

        // Act + Assert: assertThrows executa a chamada (Act) e verifica a exceção (Assert)
        assertThrows(DadosInvalidosException.class, () -> Medicao.de(sensor, valor, LocalDateTime.now()));
    }
}
