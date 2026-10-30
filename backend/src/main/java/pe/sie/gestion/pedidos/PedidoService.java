package pe.sie.gestion.pedidos;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pe.sie.gestion.clientes.Cliente;
import pe.sie.gestion.clientes.ClienteRepository;
import pe.sie.gestion.inventario.MovimientoInventario;
import pe.sie.gestion.inventario.MovimientoRepository;
import pe.sie.gestion.inventario.Producto;
import pe.sie.gestion.inventario.ProductoService;
import pe.sie.gestion.inventario.TipoMovimiento;

@Service
@Transactional
public class PedidoService {
    private final PedidoRepository pedidos;
    private final ClienteRepository clientes;
    private final ProductoService productos;
    private final MovimientoRepository movimientos;

    public PedidoService(PedidoRepository pedidos, ClienteRepository clientes,
            ProductoService productos, MovimientoRepository movimientos) {
        this.pedidos = pedidos;
        this.clientes = clientes;
        this.productos = productos;
        this.movimientos = movimientos;
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        return pedidos.findAllByOrderByCreadoEnDesc().stream().map(PedidoResponse::desde).toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PedidoResponse crear(PedidoRequest request) {
        Cliente cliente = clientes.findById(request.clienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el cliente"));
        if (!cliente.isActivo()) throw new ResponseStatusException(HttpStatus.CONFLICT, "El cliente está inactivo");

        Map<Long, Integer> cantidades = new TreeMap<>();
        request.items().forEach(item -> cantidades.merge(item.productoId(), item.cantidad(), Math::addExact));
        List<Producto> seleccionados = new ArrayList<>();
        for (Map.Entry<Long, Integer> linea : cantidades.entrySet()) {
            Producto producto = productos.bloquearProductoActivo(linea.getKey());
            long disponible = productos.stockFisico(producto.getId()) - productos.stockReservado(producto.getId());
            if (disponible < linea.getValue()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Stock insuficiente para " + producto.getNombre() + ". Disponible: " + disponible);
            }
            seleccionados.add(producto);
        }

        Pedido pedido = pedidos.save(new Pedido(cliente, request.canal()));
        for (int index = 0; index < seleccionados.size(); index++) {
            Producto producto = seleccionados.get(index);
            int cantidad = cantidades.get(producto.getId());
            pedido.agregarDetalle(new PedidoDetalle(producto, cantidad, producto.getPrecio()));
            movimientos.save(new MovimientoInventario(producto, pedido, TipoMovimiento.RESERVA,
                    cantidad, "Reserva por pedido #" + pedido.getId()));
        }
        return PedidoResponse.desde(pedidos.save(pedido));
    }

    public PedidoResponse cancelar(Long id) {
        Pedido pedido = obtener(id);
        if (pedido.getEstado() != EstadoPedido.CONFIRMADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden cancelar pedidos confirmados");
        }
        pedido.cambiarEstado(EstadoPedido.CANCELADO);
        return PedidoResponse.desde(pedido);
    }

    public PedidoResponse preparar(Long id) {
        Pedido pedido = obtener(id);
        exigirEstado(pedido, EstadoPedido.CONFIRMADO, "Solo se pueden preparar pedidos confirmados");
        pedido.cambiarEstado(EstadoPedido.PREPARACION);
        return PedidoResponse.desde(pedido);
    }

    public PedidoResponse despachar(Long id) {
        Pedido pedido = obtener(id);
        exigirEstado(pedido, EstadoPedido.PREPARACION, "El pedido debe estar en preparación para despacharlo");
        for (PedidoDetalle detalle : pedido.getDetalles()) {
            movimientos.save(new MovimientoInventario(detalle.getProducto(), pedido, TipoMovimiento.SALIDA,
                    detalle.getCantidad(), "Despacho del pedido #" + pedido.getId()));
        }
        pedido.marcarDespachado(LocalDateTime.now(ZoneOffset.UTC));
        return PedidoResponse.desde(pedido);
    }

    public PedidoResponse registrarDevolucion(Long id) {
        Pedido pedido = obtener(id);
        exigirEstado(pedido, EstadoPedido.DESPACHADO, "Solo se pueden devolver pedidos despachados");
        for (PedidoDetalle detalle : pedido.getDetalles()) {
            movimientos.save(new MovimientoInventario(detalle.getProducto(), pedido, TipoMovimiento.DEVOLUCION,
                    detalle.getCantidad(), "Devolución del pedido #" + pedido.getId()));
        }
        pedido.cambiarEstado(EstadoPedido.DEVUELTO);
        return PedidoResponse.desde(pedido);
    }

    private Pedido obtener(Long id) {
        return pedidos.lockById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el pedido"));
    }

    private void exigirEstado(Pedido pedido, EstadoPedido esperado, String mensaje) {
        if (pedido.getEstado() != esperado) throw new ResponseStatusException(HttpStatus.CONFLICT, mensaje);
    }
}
