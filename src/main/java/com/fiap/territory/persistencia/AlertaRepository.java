package com.fiap.territory.persistencia;

import com.fiap.territory.dominio.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findAllByOrderByDataHoraDesc();

    List<Alerta> findBySensorIdOrderByDataHoraDesc(String sensorId);

    Optional<Alerta> findFirstBySensorIdOrderByIdDesc(String sensorId);
}
