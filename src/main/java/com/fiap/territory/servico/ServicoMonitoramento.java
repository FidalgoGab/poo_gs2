package com.fiap.territory.servico;

import com.fiap.territory.dominio.Alerta;
import com.fiap.territory.dominio.DadosTerritorio;
import com.fiap.territory.dominio.EstrategiaPrevisao;
import com.fiap.territory.dominio.Medicao;
import com.fiap.territory.dominio.NivelAlerta;
import com.fiap.territory.dominio.RegiaoMonitorada;
import com.fiap.territory.dominio.Sensor;
import com.fiap.territory.excecao.DadosInvalidosException;
import com.fiap.territory.excecao.FonteIndisponivelException;
import com.fiap.territory.persistencia.AlertaRepository;
import com.fiap.territory.persistencia.MedicaoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ServicoMonitoramento {

    private final MedicaoRepository medicaoRepository;
    private final AlertaRepository alertaRepository;
    private final FabricaPrevisao fabricaPrevisao;
    private final ConfiguracaoSistema configuracao;
    private final List<Sensor> sensores;
    // O status da região é derivado dos níveis por sensor guardados na própria região.
    private final RegiaoMonitorada regiao;

    public ServicoMonitoramento(MedicaoRepository medicaoRepository, AlertaRepository alertaRepository,
                                FabricaPrevisao fabricaPrevisao, ConfiguracaoSistema configuracao,
                                List<Sensor> sensores) throws DadosInvalidosException {
        this.medicaoRepository = medicaoRepository;
        this.alertaRepository = alertaRepository;
        this.fabricaPrevisao = fabricaPrevisao;
        this.configuracao = configuracao;
        this.sensores = List.copyOf(sensores);
        this.regiao = new RegiaoMonitorada(1L, "Zona Leste - Subestação 1", "Zona Leste");
        recalcularStatusDoBanco();
    }

    // sem tabela nova pra região. Ao iniciar, o status é refeito pelo último alerta salvo de cada sensor
    // (vale o maior nível; sem alerta = NORMAL). Limitação: se a última previsão do sensor foi normal, não há
    // alerta novo, então o último alerta antigo ainda conta.
    private void recalcularStatusDoBanco() throws DadosInvalidosException {
        for (Sensor sensor : sensores) {
            Optional<Alerta> ultimo = alertaRepository.findFirstBySensorIdOrderByIdDesc(sensor.getIdentificador());
            if (ultimo != null && ultimo.isPresent()) {
                regiao.atualizarNivel(sensor.getIdentificador(), ultimo.get().getNivel());
            }
        }
    }

    public List<Sensor> getSensores() {
        return sensores;
    }

    // Se o sensor estiver desligado lança FonteIndisponivelException; se o valor sair da faixa,
    // Medicao.de lança DadosInvalidosException antes de qualquer gravação.
    public Medicao lerESalvarMedicao(Sensor sensor) throws FonteIndisponivelException, DadosInvalidosException {
        double valor = sensor.lerValor();
        Medicao medicao = Medicao.de(sensor, valor, LocalDateTime.now());
        return medicaoRepository.save(medicao);
    }

    public Map<Sensor, Boolean> verificarSensores() {
        Map<Sensor, Boolean> estados = new LinkedHashMap<>();
        sensores.forEach(sensor -> estados.put(sensor, sensor.verificarFuncionamento()));
        return estados;
    }

    public ResultadoPrevisao rodarPrevisao(Sensor sensor) throws DadosInvalidosException {
        return rodarPrevisao(sensor, configuracao.getEstrategiaPadrao());
    }

    public ResultadoPrevisao rodarPrevisao(Sensor sensor, TipoPrevisao tipo) throws DadosInvalidosException {
        EstrategiaPrevisao estrategia = fabricaPrevisao.criar(tipo, configuracao.getTamanhoJanela());
        List<Double> historico = buscarHistoricoCronologico(sensor, estrategia);

        double valorPrevisto = estrategia.prever(historico);
        double indice = sensor.normalizar(valorPrevisto);
        Optional<NivelAlerta> nivel = NivelAlerta.paraIndice(indice, configuracao.getDadosTerritorio());

        Optional<Alerta> alerta = Optional.empty();
        boolean repetido = false;
        if (nivel.isPresent()) {
            Alerta novo = criarAlerta(sensor, valorPrevisto, indice, nivel.get());
            Optional<Alerta> ultimo = alertaRepository.findFirstBySensorIdOrderByIdDesc(sensor.getIdentificador());
            repetido = ultimo != null && ultimo.isPresent() && ultimo.get().equivaleA(novo);
            alerta = repetido ? ultimo : Optional.of(alertaRepository.save(novo));
        }
        regiao.atualizarNivel(sensor.getIdentificador(), nivel.orElse(null));

        return new ResultadoPrevisao(sensor.getIdentificador(), valorPrevisto, sensor.unidade(), indice,
                estrategia.descrever(valorPrevisto), alerta, repetido);
    }

    // Só as 20 mais recentes saem do banco (ver MedicaoRepository).
    public List<Medicao> buscarHistoricoPorSensor(String sensorId) {
        return medicaoRepository.findTop20BySensorIdOrderByIdDesc(sensorId);
    }

    public long contarMedicoes(String sensorId) {
        return medicaoRepository.countBySensorId(sensorId);
    }

    public List<Alerta> listarAlertas() {
        return alertaRepository.findAllByOrderByDataHoraDesc();
    }

    public List<Alerta> listarAlertas(String sensorId) {
        return alertaRepository.findBySensorIdOrderByDataHoraDesc(sensorId);
    }

    // A população existe só em DadosTerritorio.
    public void alterarPopulacao(int populacao) throws DadosInvalidosException {
        configuracao.getDadosTerritorio().setPopulacao(populacao);
    }

    public void alterarLimiares(double atencao, double alerta, double critico) throws DadosInvalidosException {
        configuracao.getDadosTerritorio().alterarLimiares(atencao, alerta, critico);
    }

    public TipoPrevisao getEstrategiaPadrao() {
        return configuracao.getEstrategiaPadrao();
    }

    public void alterarEstrategiaPadrao(TipoPrevisao tipo) throws DadosInvalidosException {
        configuracao.setEstrategiaPadrao(tipo);
    }

    public void alterarJanela(int janela) throws DadosInvalidosException {
        configuracao.setTamanhoJanela(janela);
    }

    public String statusCompleto() {
        DadosTerritorio dados = configuracao.getDadosTerritorio();
        return String.format(
                "Região: %s%nStatus: %s%nPopulação: %d%nLimiares (índice 0-100): atenção %.1f | alerta %.1f | crítico %.1f%n"
                        + "Estratégia padrão: %s | janela: %d%nMedições gravadas: %d%nAlertas emitidos: %d",
                regiao.getNome(), regiao.getStatus().getDescricao(), dados.getPopulacao(),
                dados.getLimiarAtencao(), dados.getLimiarAlerta(), dados.getLimiarCritico(),
                configuracao.getEstrategiaPadrao().getDescricao(), configuracao.getTamanhoJanela(),
                medicaoRepository.count(), alertaRepository.count());
    }

    // O banco devolve da mais recente pra mais antiga; a estratégia espera antiga -> recente, então inverte.
    private List<Double> buscarHistoricoCronologico(Sensor sensor, EstrategiaPrevisao estrategia)
            throws DadosInvalidosException {
        List<Medicao> recentes = new ArrayList<>(medicaoRepository.findBySensorIdOrderByIdDesc(
                sensor.getIdentificador(), PageRequest.of(0, estrategia.getMinimoLeituras())));
        int faltam = estrategia.getMinimoLeituras() - recentes.size();
        if (faltam > 0) {
            throw new DadosInvalidosException(String.format(
                    "Histórico insuficiente no sensor %s: faltam %d leituras para %s (mínimo %d, há %d).",
                    sensor.getIdentificador(), faltam, estrategia.getNome(), estrategia.getMinimoLeituras(),
                    recentes.size()));
        }
        Collections.reverse(recentes);
        return recentes.stream().map(Medicao::getValor).toList();
    }

    private Alerta criarAlerta(Sensor sensor, double valorPrevisto, double indice, NivelAlerta nivel)
            throws DadosInvalidosException {
        double limite = nivel.limiarEm(configuracao.getDadosTerritorio());
        String mensagem = String.format(
                "Sensor %s (%s): previsto %.1f %s = índice %.1f/100, passou do limite %.1f. Nível: %s.",
                sensor.getIdentificador(), sensor.retornarTipo(), valorPrevisto, sensor.unidade(), indice, limite,
                nivel.getDescricao());
        return new Alerta(sensor.getIdentificador(), nivel, valorPrevisto, limite, mensagem, LocalDateTime.now());
    }
}
