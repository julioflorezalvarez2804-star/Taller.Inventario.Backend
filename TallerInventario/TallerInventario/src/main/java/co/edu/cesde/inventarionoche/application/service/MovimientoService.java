package co.edu.cesde.inventarionoche.application.service;

import java.util.List;

import co.edu.cesde.inventarionoche.domain.model.Movimiento;

public interface MovimientoService {

    Movimiento save(Movimiento movimiento);

    List<Movimiento> findAll();

    Movimiento findById(Long id);

    void delete(Long id);

    Movimiento update(Movimiento movimiento);
}