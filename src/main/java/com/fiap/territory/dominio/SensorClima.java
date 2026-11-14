package com.fiap.territory.dominio;

import java.util.Random;

public class SensorClima extends SensorSimulado {

    public SensorClima(String identificador) {
        this(identificador, new Random());
    }

    public SensorClima(String identificador, Random random) {
        super(identificador, random, 0.0, 50.0, 20.0, 48.0);
    }

    @Override
    public String retornarTipo() {
        return "CLIMA";
    }

    @Override
    public String unidade() {
        return "°C";
    }
}
