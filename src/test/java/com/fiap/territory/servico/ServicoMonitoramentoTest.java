package com.fiap.territory.servico;

import com.fiap.territory.dominio.Alerta;
import com.fiap.territory.dominio.Medicao;
import com.fiap.territory.dominio.NivelAlerta;
import com.fiap.territory.dominio.Sensor;
import com.fiap.territory.dominio.SensorClima;
import com.fiap.territory.excecao.DadosInvalidosException;
import com.fiap.territory.excecao.FonteIndisponivelException;
import com.fiap.territory.persistencia.AlertaRepository;
import com.fiap.territory.persistencia.MedicaoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoMonitoramentoTest {

    private static final String ID = "CLIMA-T";

    @Mock
    private MedicaoRepository medicaoRepository;
    @Mock
    private AlertaRepository alertaRepository;

    private ConfiguracaoSistema configuracao;
    private SensorClima sensor;

    // O singleton é compartilhado: restaura os padrões antes e depois de cada teste para não vazar estado.
    @BeforeEach
    void preparar() {
        configuracao = ConfiguracaoSistema.getInstance();
        configuracao.restaurarPadroes();
        sensor = new SensorClima(ID, new Random(42));
    }

    @AfterEach
    void limpar() {
        configuracao.restaurarPadroes();
    }

    // Sensor de clima vai de 0 a 50 °C: índice = valor * 2. 32 °C = 64 (atenção), 42 °C = 84 (alerta),
    // 48 °C = 96 (crítico), 20 °C = 40 (normal).
    private ServicoMonitoramento criarServico() throws DadosInvalidosException {
        return new ServicoMonitoramento(medicaoRepository, alertaRepository, new FabricaPrevisao(),
                configuracao, List.<Sensor>of(sensor));
    }

    private List<Medicao> historicoConstante(double valor, int quantidade) throws DadosInvalidosException {
        Medicao medicao = Medicao.de(sensor, valor, LocalDateTime.now());
        return Collections.nCopies(quantidade, medicao);
    }

    // O repositório devolve da mais recente para a mais antiga: o primeiro grupo é o mais recente.
    private List<Medicao> historicoEmDoisPeriodos(double recente, double anterior, int porPeriodo)
            throws DadosInvalidosException {
        List<Medicao> lista = new ArrayList<>(historicoConstante(recente, porPeriodo));
        lista.addAll(historicoConstante(anterior, porPeriodo));
        return lista;
    }

    private void simularHistorico(List<Medicao> medicoes) {
        when(medicaoRepository.findBySensorIdOrderByIdDesc(eq(ID), any(Pageable.class))).thenReturn(medicoes);
    }

    private Alerta salvarAlertaEcapturar() {
        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void deveGerarAlertaDeNivelAtencaoQuandoPrevisaoPassaDe60() throws Exception {
        // Arrange
        simularHistorico(historicoConstante(32.0, 5));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));
        ServicoMonitoramento servico = criarServico();

        // Act
        ResultadoPrevisao resultado = servico.rodarPrevisao(sensor, TipoPrevisao.MEDIA_MOVEL);

        // Assert
        Alerta alerta = salvarAlertaEcapturar();
        assertEquals(NivelAlerta.ATENCAO, alerta.getNivel());
        assertEquals(60.0, alerta.getLimiteExcedido(), 0.0001);
        assertEquals(64.0, resultado.indice(), 0.0001);
    }

    @Test
    void deveGerarAlertaDeNivelAlertaQuandoPrevisaoPassaDe80() throws Exception {
        // Arrange
        simularHistorico(historicoConstante(42.0, 5));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));
        ServicoMonitoramento servico = criarServico();

        // Act
        ResultadoPrevisao resultado = servico.rodarPrevisao(sensor, TipoPrevisao.MEDIA_MOVEL);

        // Assert
        Alerta alerta = salvarAlertaEcapturar();
        assertEquals(NivelAlerta.ALERTA, alerta.getNivel());
        assertEquals(ID, alerta.getSensorId());
        assertEquals(42.0, alerta.getValorPrevisto(), 0.0001);
        assertEquals(84.0, resultado.indice(), 0.0001);
    }

    @Test
    void deveGerarAlertaDeNivelCriticoQuandoPrevisaoPassaDe95() throws Exception {
        // Arrange
        simularHistorico(historicoConstante(48.0, 5));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));
        ServicoMonitoramento servico = criarServico();

        // Act
        ResultadoPrevisao resultado = servico.rodarPrevisao(sensor, TipoPrevisao.MEDIA_MOVEL);

        // Assert
        Alerta alerta = salvarAlertaEcapturar();
        assertEquals(NivelAlerta.CRITICO, alerta.getNivel());
        assertEquals(96.0, resultado.indice(), 0.0001);
        assertTrue(servico.statusCompleto().contains("Status: Crítico"));
    }

    @Test
    void naoDeveGerarAlertaQuandoPrevisaoFicaAbaixoDe60() throws Exception {
        // Arrange
        simularHistorico(historicoConstante(20.0, 5));
        ServicoMonitoramento servico = criarServico();

        // Act
        ResultadoPrevisao resultado = servico.rodarPrevisao(sensor, TipoPrevisao.MEDIA_MOVEL);

        // Assert
        verify(alertaRepository, never()).save(any());
        assertTrue(resultado.alerta().isEmpty());
    }

    @Test
    void deveLancarDadosInvalidosENaoGerarAlertaComHistoricoInsuficiente() throws Exception {
        // Arrange
        simularHistorico(historicoConstante(42.0, 4));
        ServicoMonitoramento servico = criarServico();

        // Act + Assert (exceção): assertThrows executa a chamada (Act) e verifica a exceção (Assert)
        assertThrows(DadosInvalidosException.class, () -> servico.rodarPrevisao(sensor, TipoPrevisao.MEDIA_MOVEL));

        // Assert (efeito)
        verify(alertaRepository, never()).save(any());
    }

    @Test
    void deveGerarAlertaPelaPrevisaoPorComparacaoViaServico() throws Exception {
        // Arrange: período recente 36 °C, anterior 30 °C -> previsto 36 + 6 = 42 °C (índice 84)
        simularHistorico(historicoEmDoisPeriodos(36.0, 30.0, 3));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));
        ServicoMonitoramento servico = criarServico();

        // Act
        ResultadoPrevisao resultado = servico.rodarPrevisao(sensor, TipoPrevisao.COMPARACAO);

        // Assert
        assertEquals(42.0, resultado.valorPrevisto(), 0.0001);
        assertEquals(NivelAlerta.ALERTA, salvarAlertaEcapturar().getNivel());
    }

    @Test
    void deveIncluirSensorValorPrevistoELimiteNaMensagemDoAlerta() throws Exception {
        // Arrange
        simularHistorico(historicoConstante(42.0, 5));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));
        ServicoMonitoramento servico = criarServico();

        // Act
        servico.rodarPrevisao(sensor, TipoPrevisao.MEDIA_MOVEL);

        // Assert: o formato usa o locale padrão, então o esperado é montado com o mesmo String.format
        String mensagem = salvarAlertaEcapturar().getMensagem();
        assertTrue(mensagem.contains(ID));
        assertTrue(mensagem.contains(String.format("previsto %.1f", 42.0)));
        assertTrue(mensagem.contains(String.format("limite %.1f", 80.0)));
    }

    @Test
    void naoDeveGravarAlertaDuplicadoQuandoUltimoTemMesmoNivelEMensagem() throws Exception {
        // Arrange: primeira previsão grava o alerta; a segunda encontra o mesmo como último do sensor
        simularHistorico(historicoConstante(42.0, 5));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));
        ServicoMonitoramento servico = criarServico();
        servico.rodarPrevisao(sensor, TipoPrevisao.MEDIA_MOVEL);
        Alerta primeiro = salvarAlertaEcapturar();
        when(alertaRepository.findFirstBySensorIdOrderByIdDesc(ID)).thenReturn(Optional.of(primeiro));

        // Act
        ResultadoPrevisao segundo = servico.rodarPrevisao(sensor, TipoPrevisao.MEDIA_MOVEL);

        // Assert: continua só 1 gravação e o resultado avisa que é repetido
        verify(alertaRepository, times(1)).save(any(Alerta.class));
        assertTrue(segundo.alertaRepetido());
        assertEquals(primeiro, segundo.alerta().orElseThrow());
    }

    @Test
    void deveGravarNovoAlertaQuandoONivelMudou() throws Exception {
        // Arrange: último alerta salvo foi ATENCAO; a nova previsão é ALERTA
        Alerta anterior = new Alerta(ID, NivelAlerta.ATENCAO, 32.0, 60.0, "antigo", LocalDateTime.now());
        when(alertaRepository.findFirstBySensorIdOrderByIdDesc(ID)).thenReturn(Optional.of(anterior));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));
        simularHistorico(historicoConstante(42.0, 5));
        ServicoMonitoramento servico = criarServico();

        // Act
        ResultadoPrevisao resultado = servico.rodarPrevisao(sensor, TipoPrevisao.MEDIA_MOVEL);

        // Assert
        verify(alertaRepository, times(1)).save(any(Alerta.class));
        assertFalse(resultado.alertaRepetido());
    }

    @Test
    void deveUsarEstrategiaPadraoDaConfiguracaoQuandoNaoInformada() throws Exception {
        // Arrange: padrão COMPARACAO exige 6 leituras, mas só há 5
        configuracao.setEstrategiaPadrao(TipoPrevisao.COMPARACAO);
        simularHistorico(historicoConstante(42.0, 5));
        ServicoMonitoramento servico = criarServico();

        // Act + Assert: assertThrows executa a chamada (Act) e devolve a exceção para o Assert
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> servico.rodarPrevisao(sensor));
        assertTrue(erro.getMessage().contains("Comparação"));
    }

    @Test
    void deveUsarAJanelaDaConfiguracaoNaPrevisao() throws Exception {
        // Arrange: janela 7 exige 7 leituras na média móvel, mas só há 6
        configuracao.setTamanhoJanela(7);
        simularHistorico(historicoConstante(42.0, 6));
        ServicoMonitoramento servico = criarServico();

        // Act + Assert: assertThrows executa a chamada (Act) e devolve a exceção para o Assert
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> servico.rodarPrevisao(sensor, TipoPrevisao.MEDIA_MOVEL));
        assertTrue(erro.getMessage().contains("mínimo 7"));
    }

    @Test
    void deveSalvarMedicaoValidaLidaDoSensor() throws Exception {
        // Arrange
        when(medicaoRepository.save(any(Medicao.class))).thenAnswer(inv -> inv.getArgument(0));
        ServicoMonitoramento servico = criarServico();

        // Act
        Medicao salva = servico.lerESalvarMedicao(sensor);

        // Assert
        ArgumentCaptor<Medicao> captor = ArgumentCaptor.forClass(Medicao.class);
        verify(medicaoRepository).save(captor.capture());
        assertEquals(ID, captor.getValue().getSensorId());
        assertTrue(salva.getValor() >= 20.0 && salva.getValor() <= 48.0);
    }

    @Test
    void naoDeveSalvarMedicaoQuandoSensorEstaForaDeOperacao() throws Exception {
        // Arrange
        sensor.definirFuncionamento(false);
        ServicoMonitoramento servico = criarServico();

        // Act + Assert (exceção): assertThrows executa a chamada (Act) e verifica a exceção (Assert)
        assertThrows(FonteIndisponivelException.class, () -> servico.lerESalvarMedicao(sensor));

        // Assert (efeito)
        verify(medicaoRepository, never()).save(any());
    }

    @Test
    void deveDescartarLeituraDefeituosaSemGravarNoBanco() throws Exception {
        // Arrange
        sensor.injetarLeituraDefeituosa();
        ServicoMonitoramento servico = criarServico();

        // Act + Assert (exceção): assertThrows executa a chamada (Act) e verifica a exceção (Assert)
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> servico.lerESalvarMedicao(sensor));
        assertTrue(erro.getMessage().contains("fora da faixa"));

        // Assert (efeito): o valor inválido não chega ao repositório
        verify(medicaoRepository, never()).save(any());
    }

    @Test
    void deveMostrarSensorForaDeOperacaoNaVerificacao() throws Exception {
        // Arrange
        sensor.definirFuncionamento(false);
        ServicoMonitoramento servico = criarServico();

        // Act
        boolean funcionando = servico.verificarSensores().get(sensor);

        // Assert
        assertFalse(funcionando);
    }

    @Test
    void deveAlterarPopulacaoSomenteEmDadosTerritorio() throws Exception {
        // Arrange
        ServicoMonitoramento servico = criarServico();

        // Act
        servico.alterarPopulacao(1234);

        // Assert
        assertEquals(1234, configuracao.getDadosTerritorio().getPopulacao());
        assertTrue(servico.statusCompleto().contains("População: 1234"));
    }

    @Test
    void deveRecusarPopulacaoNegativaSemAlterarOValorAtual() throws Exception {
        // Arrange
        ServicoMonitoramento servico = criarServico();

        // Act + Assert (exceção): assertThrows executa a chamada (Act) e verifica a exceção (Assert)
        assertThrows(DadosInvalidosException.class, () -> servico.alterarPopulacao(-5));

        // Assert (estado)
        assertEquals(ConfiguracaoSistema.POPULACAO_PADRAO, configuracao.getDadosTerritorio().getPopulacao());
    }

    @Test
    void deveBuscarHistoricoLimitadoEListarAlertasPorSensor() throws Exception {
        // Arrange
        Medicao medicao = Medicao.de(sensor, 30.0, LocalDateTime.now());
        when(medicaoRepository.findTop20BySensorIdOrderByIdDesc(ID)).thenReturn(List.of(medicao));
        when(alertaRepository.findBySensorIdOrderByDataHoraDesc(ID)).thenReturn(List.of());
        ServicoMonitoramento servico = criarServico();

        // Act
        List<Medicao> historico = servico.buscarHistoricoPorSensor(ID);
        List<Alerta> alertas = servico.listarAlertas(ID);

        // Assert
        assertEquals(List.of(medicao), historico);
        assertTrue(alertas.isEmpty());
    }

    @Test
    void deveRecalcularStatusDaRegiaoPeloUltimoAlertaSalvoAoIniciar() throws Exception {
        // Arrange
        Alerta ultimo = new Alerta(ID, NivelAlerta.CRITICO, 49.0, 95.0, "teste", LocalDateTime.now());
        when(alertaRepository.findFirstBySensorIdOrderByIdDesc(ID)).thenReturn(Optional.of(ultimo));

        // Act
        ServicoMonitoramento servico = criarServico();

        // Assert
        assertTrue(servico.statusCompleto().contains("Status: Crítico"));
    }

    @Test
    void deveIniciarComStatusNormalQuandoNaoHaAlertasSalvos() throws Exception {
        // Arrange: mock sem configuração devolve Optional vazio

        // Act
        ServicoMonitoramento servico = criarServico();

        // Assert
        assertTrue(servico.statusCompleto().contains("Status: Normal"));
    }
}
