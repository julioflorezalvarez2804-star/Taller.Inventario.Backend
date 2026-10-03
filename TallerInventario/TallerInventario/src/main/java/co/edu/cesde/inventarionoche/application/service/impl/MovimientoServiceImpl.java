package co.edu.cesde.inventarionoche.application.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.cesde.inventarionoche.application.service.MovimientoService;
import co.edu.cesde.inventarionoche.domain.model.Movimiento;
import co.edu.cesde.inventarionoche.domain.model.Producto;
import co.edu.cesde.inventarionoche.domain.model.TipoMovimiento;
import co.edu.cesde.inventarionoche.infrastructure.exception.InsufficientStockException;
import co.edu.cesde.inventarionoche.infrastructure.exception.ResourceNotFoundException;
import co.edu.cesde.inventarionoche.infrastructure.exception.ValidationException;
import co.edu.cesde.inventarionoche.infrastructure.repository.MovimientoRepository;
import co.edu.cesde.inventarionoche.infrastructure.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional
    public Movimiento save(Movimiento movimiento) {
        Producto producto = buscarProducto(movimiento);
        aplicarMovimiento(producto, movimiento.getTipo(), movimiento.getCantidad());

        productoRepository.save(producto);
        movimiento.setId(null);
        movimiento.setProducto(producto);
        return movimientoRepository.save(movimiento);
    }

    @Override
    public List<Movimiento> findAll() {
        return movimientoRepository.findAll();
    }

    @Override
    public Movimiento findById(Long id) {
        return movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el movimiento con id " + id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Movimiento movimiento = findById(id);

        Producto producto = movimiento.getProducto();
        revertirMovimiento(producto, movimiento.getTipo(), movimiento.getCantidad());
        productoRepository.save(producto);

        movimientoRepository.delete(movimiento);
    }

    @Override
    @Transactional
    public Movimiento update(Movimiento movimiento) {
        if (movimiento.getId() == null) {
            throw new ValidationException("El id es obligatorio para actualizar");
        }
        Movimiento existente = findById(movimiento.getId());

        // primero deshago el movimiento viejo
        Producto productoViejo = existente.getProducto();
        revertirMovimiento(productoViejo, existente.getTipo(), existente.getCantidad());
        productoRepository.save(productoViejo);

        // despues aplico el nuevo
        Producto productoNuevo = buscarProducto(movimiento);
        aplicarMovimiento(productoNuevo, movimiento.getTipo(), movimiento.getCantidad());
        productoRepository.save(productoNuevo);

        existente.setProducto(productoNuevo);
        existente.setCantidad(movimiento.getCantidad());
        existente.setTipo(movimiento.getTipo());
        return movimientoRepository.save(existente);
    }

    private Producto buscarProducto(Movimiento movimiento) {
        if (movimiento.getProducto() == null || movimiento.getProducto().getId() == null) {
            throw new ValidationException("El producto es obligatorio");
        }
        Long idProducto = movimiento.getProducto().getId();
        return productoRepository.findById(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el producto con id " + idProducto));
    }

    private void aplicarMovimiento(Producto producto, TipoMovimiento tipo, int cantidad) {
        if (tipo == TipoMovimiento.ENTRADA) {
            producto.sumarStock(cantidad);
        } else {
            if (producto.getStock() < cantidad) {
                throw new InsufficientStockException("Stock insuficiente. Disponible: "
                        + producto.getStock() + ", solicitado: " + cantidad);
            }
            producto.restarStock(cantidad);
        }
    }

    private void revertirMovimiento(Producto producto, TipoMovimiento tipo, int cantidad) {
        if (tipo == TipoMovimiento.ENTRADA) {
            if (producto.getStock() < cantidad) {
                throw new InsufficientStockException("No se puede deshacer la entrada, el stock quedaria negativo");
            }
            producto.restarStock(cantidad);
        } else {
            producto.sumarStock(cantidad);
        }
    }
}