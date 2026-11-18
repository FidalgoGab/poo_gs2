package com.fiap.territory.servico;

public enum TipoPrevisao {
    MEDIA_MOVEL("Média móvel"),
    COMPARACAO("Comparação de períodos");

    private final String descricao;

    TipoPrevisao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}