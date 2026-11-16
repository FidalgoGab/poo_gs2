package com.fiap.territory.dominio;

import com.fiap.territory.excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertaTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2025, 1, 10, 12, 0);

    @Test
    void deveCriarAlertaValido() throws Exception {
        // Arrange
        String mensagem = "Sensor CLIMA-01 acima do limite";

        // Act
        Alerta alerta = new Alerta("CLIMA-01", NivelAlerta.ALERTA, 42.0, 80.0, mensagem, AGORA);

        // Assert
        assertEquals("CLIMA-01", alerta.getSensorId());
        assertEquals(NivelAlerta.ALERTA, alerta.getNivel());
        assertEquals(42.0, alerta.getValorPrevisto(), 0.0001);
        assertEquals(80.0, alerta.getLimiteExcedido(), 0.0001);
        assertEquals(mensagem, alerta.getMensagem());
    }

    @Test
    void deveLancarDadosInvalidosParaSensorEmBranco() {
        // Arrange
        String sensorEmBranco = " ";

        // Act + Assert: assertThrows executa a chamada (Act) e verifica a exceção (Assert)
        assertThrows(DadosInvalidosException.class,
                () -> new Alerta(sensorEmBranco, NivelAlerta.ALERTA, 1.0, 80.0, "msg", AGORA));
    }

    @Test
    void deveLancarDadosInvalidosParaNivelNulo() {
        // Arrange
        NivelAlerta nivelNulo = null;

        // Act + Assert: assertThrows executa a chamada (Act) e verifica a exceção (Assert)
        assertThrows(DadosInvalidosException.class,
                () -> new Alerta("CLIMA-01", nivelNulo, 1.0, 80.0, "msg", AGORA));
    }

    @Test
    void deveLancarDadosInvalidosParaMensagemVazia() {
        // Arrange
        String mensagemVazia = "";

        // Act + Assert: assertThrows executa a chamada (Act) e verifica a exceção (Assert)
        assertThrows(DadosInvalidosException.class,
                () -> new Alerta("CLIMA-01", NivelAlerta.ALERTA, 1.0, 80.0, mensagemVazia, AGORA));
    }

    @Test
    void deveLancarDadosInvalidosParaDataHoraNula() {
        // Arrange
        LocalDateTime semData = null;

        // Act + Assert: assertThrows executa a chamada (Act) e verifica a exceção (Assert)
        assertThrows(DadosInvalidosException.class,
                () -> new Alerta("CLIMA-01", NivelAlerta.ALERTA, 1.0, 80.0, "msg", semData));
    }

    @Test
    void deveSerEquivalenteQuandoSensorNivelEMensagemSaoIguais() throws Exception {
        // Arrange
        Alerta a = new Alerta("CLIMA-01", NivelAlerta.ALERTA, 42.0, 80.0, "msg", AGORA);
        Alerta b = new Alerta("CLIMA-01", NivelAlerta.ALERTA, 42.0, 80.0, "msg", AGORA.plusHours(1));

        // Act
        boolean equivalentes = a.equivaleA(b);

        // Assert: a data/hora não entra no critério
        assertTrue(equivalentes);
    }

    @Test
    void naoDeveSerEquivalenteQuandoNivelOuMensagemMudam() throws Exception {
        // Arrange
        Alerta base = new Alerta("CLIMA-01", NivelAlerta.ALERTA, 42.0, 80.0, "msg", AGORA);
        Alerta outroNivel = new Alerta("CLIMA-01", NivelAlerta.CRITICO, 42.0, 95.0, "msg", AGORA);
        Alerta outraMensagem = new Alerta("CLIMA-01", NivelAlerta.ALERTA, 43.0, 80.0, "outra", AGORA);

        // Act
        boolean porNivel = base.equivaleA(outroNivel);
        boolean porMensagem = base.equivaleA(outraMensagem);

        // Assert
        assertFalse(porNivel);
        assertFalse(porMensagem);
        assertFalse(base.equivaleA(null));
    }
}
