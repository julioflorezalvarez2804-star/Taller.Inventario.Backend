package co.edu.cesde.inventarionoche.application.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.edu.cesde.inventarionoche.application.service.ProductoService;
import co.edu.cesde.inventarionoche.domain.model.Producto;
import co.edu.cesde.inventarionoche.infrastructure.exception.DuplicateResourceException;
import co.edu.cesde.inventarionoche.infrastructure.exception.ResourceNotFoundException;
import co.edu.cesde.inventarionoche.infrastructure.exception.ValidationException;
import co.edu.cesde.inventarionoche.infrastructure.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    @Override
    public Producto save(Producto producto) {
        if (productoRepository.findByNombre(producto.getNombre()).isPresent()) {
            throw new DuplicateResourceException("Ya existe un producto con el nombre " + producto.getNombre());
        }
        producto.setId(null);
        return productoRepository.save(producto);
    }

    @Override
    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    @Override
    public Producto findById(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el producto con id " + id));
    }

    @Override
    public void delete(Long id) {
        Producto producto = findById(id);
        productoRepository.delete(producto);
    }

    @Override
    public Producto update(Producto producto) {
        if (producto.getId() == null) {
            throw new ValidationException("El id es obligatorio para actualizar");
        }
        findById(producto.getId());

        Optional<Producto> otro = productoRepository.findByNombre(producto.getNombre());
        if (otro.isPresent() && !otro.get().getId().equals(producto.getId())) {
            throw new DuplicateResourceException("Ya existe un producto con el nombre " + producto.getNombre());
        }
        return productoRepository.save(producto);
    }
}