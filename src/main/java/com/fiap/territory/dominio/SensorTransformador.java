package com.fiap.territory.dominio;

import java.util.Random;

public class SensorTransformador extends SensorSimulado {

    public SensorTransformador(String identificador) {
        this(identificador, new Random());
    }

    public SensorTransformador(String identificador, Random random) {
        super(identificador, random, 20.0, 120.0, 40.0, 110.0);
    }

    @Override
    public String retornarTipo() {
        return "TRANSFORMADOR";
    }

    @Override
    public String unidade() {
        return "°C";
    }
}
