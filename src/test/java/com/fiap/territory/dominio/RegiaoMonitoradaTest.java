package com.fiap.territory.dominio;

import com.fiap.territory.excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegiaoMonitoradaTest {

    private RegiaoMonitorada criarRegiao() throws DadosInvalidosException {
        return new RegiaoMonitorada(1L, "Zona Leste - Subestação 1", "Zona Leste");
    }

    @Test
    void deveIniciarNormalSemAlertas() throws Exception {
        // Arrange
        RegiaoMonitorada regiao = criarRegiao();

        // Act
        StatusTerritorio status = regiao.verificarStatus();

        // Assert
        assertEquals(StatusTerritorio.NORMAL, status);
    }

    @Test
    void deveDerivarStatusDoNivelDoSensor() throws Exception {
        // Arrange
        RegiaoMonitorada regiao = criarRegiao();

        // Act
        regiao.atualizarNivel("CLIMA-01", NivelAlerta.ATENCAO);

        // Assert
        assertEquals(StatusTerritorio.EM_ATENCAO, regiao.verificarStatus());
    }

    @Test
    void deveUsarOMaiorNivelEntreOsSensores() throws Exception {
        // Arrange
        RegiaoMonitorada regiao = criarRegiao();

        // Act
        regiao.atualizarNivel("CLIMA-01", NivelAlerta.ATENCAO);
        regiao.atualizarNivel("TRAFO-01", NivelAlerta.CRITICO);
        regiao.atualizarNivel("ENERGIA-01", NivelAlerta.ALERTA);

        // Assert
        assertEquals(StatusTerritorio.CRITICO, regiao.verificarStatus());
    }

    @Test
    void deveVoltarParaOProximoMaiorNivelQuandoUmSensorNormaliza() throws Exception {
        // Arrange
        RegiaoMonitorada regiao = criarRegiao();
        regiao.atualizarNivel("CLIMA-01", NivelAlerta.ATENCAO);
        regiao.atualizarNivel("TRAFO-01", NivelAlerta.CRITICO);

        // Act: nível nulo = sensor sem alerta ativo
        regiao.atualizarNivel("TRAFO-01", null);

        // Assert
        assertEquals(StatusTerritorio.EM_ATENCAO, regiao.verificarStatus());
    }

    @Test
    void deveExporOMesmoStatusPorGetStatusEVerificarStatus() throws Exception {
        // Arrange
        RegiaoMonitorada regiao = criarRegiao();
        regiao.atualizarNivel("CLIMA-01", NivelAlerta.ALERTA);

        // Act
        StatusTerritorio porGetter = regiao.getStatus();

        // Assert
        assertEquals(regiao.verificarStatus(), porGetter);
    }

    @Test
    void deveLancarDadosInvalidosParaSensorEmBranco() throws Exception {
        // Arrange
        RegiaoMonitorada regiao = criarRegiao();

        // Act + Assert: assertThrows executa a chamada (Act) e verifica a exceção (Assert)
        assertThrows(DadosInvalidosException.class, () -> regiao.atualizarNivel(" ", NivelAlerta.ALERTA));
    }

    @Test
    void deveLancarDadosInvalidosParaNomeVazio() {
        // Arrange
        String nomeVazio = " ";

        // Act + Assert: assertThrows executa a chamada (Act) e verifica a exceção (Assert)
        assertThrows(DadosInvalidosException.class, () -> new RegiaoMonitorada(1L, nomeVazio, "Zona Leste"));
    }
}
