package co.edu.cesde.inventarionoche.application.service;

import java.util.List;

import co.edu.cesde.inventarionoche.domain.model.Producto;

public interface ProductoService {

    Producto save(Producto producto);

    List<Producto> findAll();

    Producto findById(Long id);

    void delete(Long id);

    Producto update(Producto producto);
}