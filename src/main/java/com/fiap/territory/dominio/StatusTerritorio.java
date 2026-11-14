package com.fiap.territory.dominio;

public enum StatusTerritorio {
    NORMAL("Normal"),
    EM_ATENCAO("Em atenção"),
    EM_ALERTA("Em alerta"),
    CRITICO("Crítico");

    private final String descricao;

    StatusTerritorio(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
