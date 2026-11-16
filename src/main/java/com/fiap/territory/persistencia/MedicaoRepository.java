package com.fiap.territory.persistencia;

import com.fiap.territory.dominio.Medicao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicaoRepository extends JpaRepository<Medicao, Long> {

    // Histórico exibido: só as 20 mais recentes saem do banco (ordena pelo id, auto-incremento,
    // pra não depender da precisão do relógio quando duas leituras saem juntas).
    List<Medicao> findTop20BySensorIdOrderByIdDesc(String sensorId);

    long countBySensorId(String sensorId);

    List<Medicao> findBySensorIdOrderByIdDesc(String sensorId, Pageable pageable);
}
