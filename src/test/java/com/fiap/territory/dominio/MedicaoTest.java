package com.fiap.territory.dominio;

import com.fiap.territory.excecao.DadosInvalidosException;
import com.fiap.territory.excecao.FonteIndisponivelException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MedicaoTest {

    @Test
    void deveCriarMedicaoComValorDentroDaFaixa() throws Exception {
        // Arrange
        SensorClima sensor = new SensorClima("CLIMA-T", new Random(1));
        LocalDateTime agora = LocalDateTime.of(2025, 1, 10, 12, 0);

        // Act
        Medicao medicao = Medicao.de(sensor, 38.5, agora);

        // Assert
        assertEquals("CLIMA-T", medicao.getSensorId());
        assertEquals("CLIMA", medicao.getTipoSensor());
        assertEquals(38.5, medicao.getValor(), 0.0001);
        assertEquals(agora, medicao.getDataHora());
    }

    @Test
    void deveLancarDadosInvalidosQuandoValorForaDaFaixaDoSensor() {
        // Arrange
        SensorClima sensor = new SensorClima("CLIMA-T", new Random(1));

        // Act + Assert: assertThrows executa a chamada (Act) e devolve a exceção para o Assert
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> Medicao.de(sensor, 50.01, LocalDateTime.now()));
        assertTrue(erro.getMessage().contains("CLIMA-T"));
    }

    @Test
    void deveLancarFonteIndisponivelQuandoSensorComDefeitoLerValor() {
        // Arrange
        SensorClima sensor = new SensorClima("CLIMA-T", new Random(1));
        sensor.definirFuncionamento(false);

        // Act + Assert: assertThrows executa a chamada (Act) e verifica a exceção (Assert)
        assertThrows(FonteIndisponivelException.class, sensor::lerValor);
        assertFalse(sensor.verificarFuncionamento());
    }

    @Test
    void deveNormalizarValorParaEscalaDe0a100() {
        // Arrange: clima vai de 0 a 50 °C
        SensorClima sensor = new SensorClima("CLIMA-T", new Random(1));

        // Act
        double indice = sensor.normalizar(40.0);

        // Assert: 40/50*100 = 80
        assertEquals(80.0, indice, 0.0001);
    }
}
