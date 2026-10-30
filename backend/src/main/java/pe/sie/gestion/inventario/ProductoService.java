package pe.sie.gestion.inventario;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ProductoService {
    private final ProductoRepository productos;
    private final MovimientoRepository movimientos;

    public ProductoService(ProductoRepository productos, MovimientoRepository movimientos) {
        this.productos = productos;
        this.movimientos = movimientos;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(boolean incluirInactivos) {
        List<Producto> lista = incluirInactivos ? productos.findAllByOrderByNombreAsc()
                : productos.findAllByActivoTrueOrderByNombreAsc();
        return lista.stream().map(this::respuesta).toList();
    }

    public ProductoResponse crear(ProductoRequest request) {
        String sku = request.sku().trim().toUpperCase();
        if (productos.existsBySku(sku)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con ese SKU");
        }
        Producto producto = productos.save(new Producto(sku, request.nombre().trim(), normalizar(request.descripcion()),
                request.precio(), request.stockMinimo()));
        if (request.stockInicial() > 0) {
            movimientos.save(new MovimientoInventario(producto, null, TipoMovimiento.ENTRADA,
                    request.stockInicial(), "Stock inicial"));
        }
        return respuesta(producto);
    }

    public MovimientoResponse registrarMovimiento(Long productoId, MovimientoRequest request) {
        Producto producto = productos.bloquearActivo(productoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el producto activo"));
        int cantidad;
        if (request.tipo() == TipoMovimiento.ENTRADA) {
            if (request.cantidad() <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La entrada debe ser mayor que cero");
            cantidad = request.cantidad();
        } else if (request.tipo() == TipoMovimiento.AJUSTE) {
            if (request.cantidad() == 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ajuste no puede ser cero");
            cantidad = request.cantidad();
            long disponibleLuego = stockFisico(productoId) + cantidad - stockReservado(productoId);
            if (disponibleLuego < 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "El ajuste dejaría el stock por debajo de las unidades reservadas");
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se permiten movimientos de entrada o ajuste manual");
        }
        MovimientoInventario movimiento = movimientos.save(new MovimientoInventario(producto, null,
                request.tipo(), cantidad, normalizar(request.motivo())));
        return desde(movimiento);
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponse> listarMovimientos() {
        return movimientos.recientes().stream().limit(100).map(this::desde).toList();
    }

    public Producto bloquearProductoActivo(Long id) {
        return productos.bloquearActivo(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no disponible"));
    }

    public long stockFisico(Long id) { return movimientos.stockFisico(id); }
    public long stockReservado(Long id) { return movimientos.stockReservado(id); }

    private ProductoResponse respuesta(Producto p) {
        int fisico = Math.toIntExact(stockFisico(p.getId()));
        int reservado = Math.toIntExact(stockReservado(p.getId()));
        int disponible = fisico - reservado;
        return new ProductoResponse(p.getId(), p.getSku(), p.getNombre(), p.getDescripcion(), p.getPrecio(),
                p.getStockMinimo(), fisico, reservado, disponible, disponible <= p.getStockMinimo(), p.isActivo());
    }

    private MovimientoResponse desde(MovimientoInventario m) {
        return new MovimientoResponse(m.getId(), m.getProducto().getId(), m.getProducto().getNombre(),
                m.getPedido() == null ? null : m.getPedido().getId(), m.getTipo(), m.getCantidad(),
                m.getMotivo(), m.getCreadoEn());
    }

    private static String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
