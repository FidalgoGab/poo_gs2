package com.fiap.territory;

import com.fiap.territory.dominio.Sensor;
import com.fiap.territory.dominio.SensorClima;
import com.fiap.territory.persistencia.AlertaRepository;
import com.fiap.territory.persistencia.MedicaoRepository;
import com.fiap.territory.servico.ConfiguracaoSistema;
import com.fiap.territory.servico.FabricaPrevisao;
import com.fiap.territory.servico.ServicoMonitoramento;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// Roda o menu com entrada roteirizada e repositórios simulados (sem banco).
@ExtendWith(MockitoExtension.class)
class MenuConsoleTest {

    private static final String ID = "CLIMA-T";

    @Mock
    private MedicaoRepository medicaoRepository;
    @Mock
    private AlertaRepository alertaRepository;

    private final PrintStream saidaOriginal = System.out;
    private final ByteArrayOutputStream saida = new ByteArrayOutputStream();
    private SensorClima sensor;
    private MenuConsole menu;

    @BeforeEach
    void preparar() throws Exception {
        System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));
        sensor = new SensorClima(ID, new Random(7));
    }

    @AfterEach
    void restaurarSaida() {
        System.setOut(saidaOriginal);
    }

    private String executar(String roteiro) throws Exception {
        ServicoMonitoramento servico = new ServicoMonitoramento(medicaoRepository, alertaRepository,
                new FabricaPrevisao(), ConfiguracaoSistema.getInstance(), List.<Sensor>of(sensor));
        menu = new MenuConsole(servico, new Scanner(roteiro));
        menu.executar();
        return saida.toString(StandardCharsets.UTF_8);
    }

    @Test
    void deveMostrarForaDeOperacaoEFonteIndisponivelParaSensorDesligado() throws Exception {
        // Arrange: opção 8 desliga o sensor 1; opção 1 verifica os sensores; 0 sai
        String roteiro = "8\n1\n1\n0\n";

        // Act
        String texto = executar(roteiro);

        // Assert
        assertTrue(texto.contains("FORA DE OPERAÇÃO"));
        assertTrue(texto.contains("Fonte indisponível: "));
        verify(medicaoRepository, never()).save(any());
    }

    @Test
    void deveDescartarLeituraDefeituosaSemGravarNoBanco() throws Exception {
        // Arrange: opção 9 injeta a falha no sensor 1; opção 1 lê o sensor; 0 sai
        String roteiro = "9\n1\n1\n0\n";

        // Act
        String texto = executar(roteiro);

        // Assert
        assertTrue(texto.contains("Leitura descartada: "));
        assertTrue(texto.contains("Medições gravadas: 0"));
        verify(medicaoRepository, never()).save(any());
    }

    @Test
    void deveListarAlertasDeTodosOuSoDoSensorEscolhido() throws Exception {
        // Arrange: opção 7 com 0 (todos) e depois com 1 (só o sensor 1)
        String roteiro = "7\n0\n7\n1\n0\n";

        // Act
        String texto = executar(roteiro);

        // Assert
        verify(alertaRepository).findAllByOrderByDataHoraDesc();
        verify(alertaRepository).findBySensorIdOrderByDataHoraDesc(ID);
        assertTrue(texto.contains("Alertas emitidos: 0"));
    }

    @Test
    void deveLigarOSensorDeNovoQuandoAOpcaoDeLigarDesligarERepetida() throws Exception {
        // Arrange: desliga e liga o sensor 1
        String roteiro = "8\n1\n8\n1\n0\n";

        // Act
        String texto = executar(roteiro);

        // Assert
        assertTrue(texto.contains("FORA DE OPERAÇÃO"));
        assertTrue(sensor.verificarFuncionamento());
        assertFalse(texto.contains("Fonte indisponível"));
    }
}
