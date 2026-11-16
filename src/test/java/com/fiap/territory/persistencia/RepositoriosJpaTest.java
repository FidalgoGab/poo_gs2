package com.fiap.territory.persistencia;

import com.fiap.territory.dominio.Alerta;
import com.fiap.territory.dominio.Medicao;
import com.fiap.territory.dominio.NivelAlerta;
import com.fiap.territory.dominio.SensorClima;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Usa o H2 embutido em memória (o @DataJpaTest troca o datasource) e o perfil "test", que desliga o menu de console.
@DataJpaTest
@ActiveProfiles("test")
class RepositoriosJpaTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2025, 1, 10, 12, 0);

    @Autowired
    private MedicaoRepository medicaoRepository;
    @Autowired
    private AlertaRepository alertaRepository;

    private final SensorClima clima = new SensorClima("CLIMA-01", new Random(1));
    private final SensorClima outro = new SensorClima("CLIMA-02", new Random(1));

    private void gravarMedicoes(SensorClima sensor, int quantidade) throws Exception {
        for (int i = 0; i < quantidade; i++) {
            medicaoRepository.save(Medicao.de(sensor, 20.0 + i, BASE.plusMinutes(i)));
        }
    }

    @Test
    void deveGravarEConsultarMedicoesPorSensorDaMaisRecenteParaAMaisAntiga() throws Exception {
        // Arrange
        gravarMedicoes(clima, 3);
        gravarMedicoes(outro, 2);

        // Act
        List<Medicao> medicoes = medicaoRepository.findTop20BySensorIdOrderByIdDesc("CLIMA-01");

        // Assert
        assertEquals(3, medicoes.size());
        assertEquals(22.0, medicoes.get(0).getValor(), 0.0001);
        assertEquals(21.0, medicoes.get(1).getValor(), 0.0001);
        assertEquals(20.0, medicoes.get(2).getValor(), 0.0001);
        assertEquals(3, medicaoRepository.countBySensorId("CLIMA-01"));
    }

    @Test
    void deveLimitarHistoricoANoMaximoVinteMedicoesNoBanco() throws Exception {
        // Arrange
        gravarMedicoes(clima, 25);

        // Act
        List<Medicao> medicoes = medicaoRepository.findTop20BySensorIdOrderByIdDesc("CLIMA-01");

        // Assert: vêm só as 20 mais recentes (valores 44 até 25), mas a contagem total é 25
        assertEquals(20, medicoes.size());
        assertEquals(44.0, medicoes.get(0).getValor(), 0.0001);
        assertEquals(25.0, medicoes.get(19).getValor(), 0.0001);
        assertEquals(25, medicaoRepository.countBySensorId("CLIMA-01"));
    }

    @Test
    void deveBuscarAJanelaDaPrevisaoComPaginacao() throws Exception {
        // Arrange
        gravarMedicoes(clima, 8);

        // Act
        List<Medicao> janela = medicaoRepository.findBySensorIdOrderByIdDesc("CLIMA-01", PageRequest.of(0, 5));

        // Assert
        assertEquals(5, janela.size());
        assertEquals(27.0, janela.get(0).getValor(), 0.0001);
    }

    @Test
    void deveGravarEConsultarAlertasPorSensorEmOrdem() throws Exception {
        // Arrange
        alertaRepository.save(new Alerta("CLIMA-01", NivelAlerta.ATENCAO, 32.0, 60.0, "primeiro", BASE));
        alertaRepository.save(new Alerta("CLIMA-02", NivelAlerta.CRITICO, 48.0, 95.0, "outro sensor", BASE.plusHours(1)));
        alertaRepository.save(new Alerta("CLIMA-01", NivelAlerta.ALERTA, 42.0, 80.0, "segundo", BASE.plusHours(2)));

        // Act
        List<Alerta> doSensor = alertaRepository.findBySensorIdOrderByDataHoraDesc("CLIMA-01");
        List<Alerta> todos = alertaRepository.findAllByOrderByDataHoraDesc();
        Optional<Alerta> ultimo = alertaRepository.findFirstBySensorIdOrderByIdDesc("CLIMA-01");

        // Assert
        assertEquals(2, doSensor.size());
        assertEquals("segundo", doSensor.get(0).getMensagem());
        assertEquals("primeiro", doSensor.get(1).getMensagem());
        assertEquals(3, todos.size());
        assertEquals("segundo", todos.get(0).getMensagem());
        assertTrue(ultimo.isPresent());
        assertEquals(NivelAlerta.ALERTA, ultimo.get().getNivel());
    }
}
