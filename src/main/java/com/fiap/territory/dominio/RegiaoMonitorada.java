package com.fiap.territory.dominio;

import com.fiap.territory.excecao.DadosInvalidosException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class RegiaoMonitorada extends ComponenteTerritorial {

    // Único estado de alerta da região: último nível de cada sensor. O status é sempre derivado daqui.
    // A população fica só em DadosTerritorio (fonte única).
    private final Map<String, NivelAlerta> nivelPorSensor = new HashMap<>();

    public RegiaoMonitorada(Long id, String nome, String regiao) throws DadosInvalidosException {
        super(id, nome, regiao);
    }

    // O status é o do maior nível entre os sensores (sem alerta = NORMAL). O mapeamento
    // nível -> status fica no enum NivelAlerta, sem if/instanceof.
    @Override
    public StatusTerritorio verificarStatus() {
        return nivelPorSensor.values().stream()
                .max(Comparator.naturalOrder())
                .map(NivelAlerta::getStatusCorrespondente)
                .orElse(StatusTerritorio.NORMAL);
    }

    // Passar nível null = o sensor não tem alerta ativo.
    public void atualizarNivel(String sensorId, NivelAlerta nivel) throws DadosInvalidosException {
        if (sensorId == null || sensorId.isBlank()) {
            throw new DadosInvalidosException("O sensor do nível de alerta é obrigatório.");
        }
        if (nivel == null) {
            nivelPorSensor.remove(sensorId);
        } else {
            nivelPorSensor.put(sensorId, nivel);
        }
    }
}
