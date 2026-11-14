package com.fiap.territory.dominio;

import java.util.Random;

public class SensorEnergia extends SensorSimulado {

    public SensorEnergia(String identificador) {
        this(identificador, new Random());
    }

    public SensorEnergia(String identificador, Random random) {
        super(identificador, random, 0.0, 100.0, 40.0, 100.0);
    }

    @Override
    public String retornarTipo() {
        return "ENERGIA";
    }

    @Override
    public String unidade() {
        return "% da capacidade";
    }
}
