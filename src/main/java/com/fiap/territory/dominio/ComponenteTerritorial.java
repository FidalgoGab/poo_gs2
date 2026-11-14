package com.fiap.territory.dominio;

import com.fiap.territory.excecao.DadosInvalidosException;

public abstract class ComponenteTerritorial {

    private final Long id;
    private String nome;
    private String regiao;

    protected ComponenteTerritorial(Long id, String nome, String regiao) throws DadosInvalidosException {
        this.id = id;
        setNome(nome);
        setRegiao(regiao);
    }

    public abstract StatusTerritorio verificarStatus();

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) throws DadosInvalidosException {
        this.nome = exigirTexto(nome, "nome");
    }

    public String getRegiao() {
        return regiao;
    }

    public void setRegiao(String regiao) throws DadosInvalidosException {
        this.regiao = exigirTexto(regiao, "região");
    }

    // O status não é guardado: vem sempre de verificarStatus() (fonte única).
    public StatusTerritorio getStatus() {
        return verificarStatus();
    }

    private static String exigirTexto(String valor, String campo) throws DadosInvalidosException {
        if (valor == null || valor.isBlank()) {
            throw new DadosInvalidosException("O campo " + campo + " não pode ser vazio.");
        }
        return valor.trim();
    }
}
