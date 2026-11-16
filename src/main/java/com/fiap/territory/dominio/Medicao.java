package com.fiap.territory.dominio;

import com.fiap.territory.excecao.DadosInvalidosException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Medicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sensorId;

    @Column(nullable = false)
    private String tipoSensor;

    @Column(nullable = false)
    private double valor;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    protected Medicao() {
    }

    private Medicao(String sensorId, String tipoSensor, double valor, LocalDateTime dataHora) {
        this.sensorId = sensorId;
        this.tipoSensor = tipoSensor;
        this.valor = valor;
        this.dataHora = dataHora;
    }

    // A faixa válida vem do próprio sensor (polimorfismo), evitando if/instanceof por tipo.
    public static Medicao de(Sensor sensor, double valor, LocalDateTime dataHora) throws DadosInvalidosException {
        if (Double.isNaN(valor) || valor < sensor.faixaMinima() || valor > sensor.faixaMaxima()) {
            throw new DadosInvalidosException(String.format(
                    "Valor %.2f fora da faixa do sensor %s (%.2f a %.2f %s).",
                    valor, sensor.getIdentificador(), sensor.faixaMinima(), sensor.faixaMaxima(), sensor.unidade()));
        }
        if (dataHora == null) {
            throw new DadosInvalidosException("A data/hora da medição é obrigatória.");
        }
        return new Medicao(sensor.getIdentificador(), sensor.retornarTipo(), valor, dataHora);
    }

    public Long getId() {
        return id;
    }

    public String getSensorId() {
        return sensorId;
    }

    public String getTipoSensor() {
        return tipoSensor;
    }

    public double getValor() {
        return valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
