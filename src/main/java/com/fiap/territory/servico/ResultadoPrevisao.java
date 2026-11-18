package com.fiap.territory.servico;

import com.fiap.territory.dominio.Alerta;

import java.util.Optional;

// alertaRepetido = true: o alerta já existia (mesmo sensor, nível e mensagem) e não foi gravado de novo.
public record ResultadoPrevisao(String sensorId, double valorPrevisto, String unidade, double indice,
                                String interpretacao, Optional<Alerta> alerta, boolean alertaRepetido) {
}
