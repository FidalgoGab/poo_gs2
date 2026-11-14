package com.fiap.territory.dominio;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.ToDoubleFunction;

public enum NivelAlerta {
    ATENCAO("Atenção", StatusTerritorio.EM_ATENCAO, DadosTerritorio::getLimiarAtencao),
    ALERTA("Alerta", StatusTerritorio.EM_ALERTA, DadosTerritorio::getLimiarAlerta),
    CRITICO("Crítico", StatusTerritorio.CRITICO, DadosTerritorio::getLimiarCritico);

    private final String descricao;
    private final StatusTerritorio statusCorrespondente;
    private final ToDoubleFunction<DadosTerritorio> limiar;

    NivelAlerta(String descricao, StatusTerritorio statusCorrespondente, ToDoubleFunction<DadosTerritorio> limiar) {
        this.descricao = descricao;
        this.statusCorrespondente = statusCorrespondente;
        this.limiar = limiar;
    }

    public StatusTerritorio getStatusCorrespondente() {
        return statusCorrespondente;
    }

    public String getDescricao() {
        return descricao;
    }

    public double limiarEm(DadosTerritorio dados) {
        return limiar.applyAsDouble(dados);
    }

    // Do mais grave pro mais leve: o primeiro limiar que o índice alcança (>=) define o nível.
    public static Optional<NivelAlerta> paraIndice(double indice, DadosTerritorio dados) {
        return List.of(values()).stream()
                .sorted(Comparator.reverseOrder())
                .filter(nivel -> indice >= nivel.limiarEm(dados))
                .findFirst();
    }
}
