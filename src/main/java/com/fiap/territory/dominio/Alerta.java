package com.fiap.territory.dominio;

import com.fiap.territory.excecao.DadosInvalidosException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sensorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelAlerta nivel;

    @Column(nullable = false)
    private double valorPrevisto;

    @Column(nullable = false)
    private double limiteExcedido;

    @Column(nullable = false, length = 500)
    private String mensagem;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    protected Alerta() {
    }

    public Alerta(String sensorId, NivelAlerta nivel, double valorPrevisto, double limiteExcedido,
                  String mensagem, LocalDateTime dataHora) throws DadosInvalidosException {
        if (sensorId == null || sensorId.isBlank() || nivel == null || dataHora == null
                || mensagem == null || mensagem.isBlank()) {
            throw new DadosInvalidosException("Sensor, nível, mensagem e data/hora do alerta são obrigatórios.");
        }
        this.sensorId = sensorId;
        this.nivel = nivel;
        this.valorPrevisto = valorPrevisto;
        this.limiteExcedido = limiteExcedido;
        this.mensagem = mensagem;
        this.dataHora = dataHora;
    }

    // Critério de duplicado: mesmo sensor, mesmo nível e mesma mensagem (a mensagem traz valor previsto e limite).
    public boolean equivaleA(Alerta outro) {
        return outro != null && sensorId.equals(outro.sensorId) && nivel == outro.nivel
                && mensagem.equals(outro.mensagem);
    }

    public Long getId() {
        return id;
    }

    public String getSensorId() {
        return sensorId;
    }

    public NivelAlerta getNivel() {
        return nivel;
    }

    public double getValorPrevisto() {
        return valorPrevisto;
    }

    public double getLimiteExcedido() {
        return limiteExcedido;
    }

    public String getMensagem() {
        return mensagem;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
