package com.fiap.territory.servico;

import com.fiap.territory.dominio.Sensor;
import com.fiap.territory.dominio.SensorClima;
import com.fiap.territory.dominio.SensorEnergia;
import com.fiap.territory.dominio.SensorTransformador;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracaoBeans {

    @Bean
    public ConfiguracaoSistema configuracaoSistema() {
        return ConfiguracaoSistema.getInstance();
    }

    @Bean
    public Sensor sensorClima() {
        return new SensorClima("CLIMA-01");
    }

    @Bean
    public Sensor sensorEnergia() {
        return new SensorEnergia("ENERGIA-01");
    }

    @Bean
    public Sensor sensorTransformador() {
        return new SensorTransformador("TRAFO-01");
    }
}