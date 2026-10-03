package co.edu.cesde.inventarionoche.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.cesde.inventarionoche.domain.model.Movimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
}