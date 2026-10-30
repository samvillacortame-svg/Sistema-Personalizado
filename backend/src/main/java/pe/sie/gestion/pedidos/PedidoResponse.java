package pe.sie.gestion.pedidos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(Long id, Long clienteId, String clienteNombre, CanalPedido canal,
        EstadoPedido estado, LocalDateTime creadoEn, BigDecimal total, List<DetalleResponse> items) {
    static PedidoResponse desde(Pedido pedido) {
        List<DetalleResponse> items = pedido.getDetalles().stream().map(DetalleResponse::desde).toList();
        BigDecimal total = items.stream().map(DetalleResponse::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PedidoResponse(pedido.getId(), pedido.getCliente().getId(), pedido.getCliente().getNombre(),
                pedido.getCanal(), pedido.getEstado(), pedido.getCreadoEn(), total, items);
    }

    public record DetalleResponse(Long productoId, String sku, String productoNombre, int cantidad,
            BigDecimal precioUnitario, BigDecimal subtotal) {
        static DetalleResponse desde(PedidoDetalle detalle) {
            BigDecimal subtotal = detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad()));
            return new DetalleResponse(detalle.getProducto().getId(), detalle.getProducto().getSku(),
                    detalle.getProducto().getNombre(), detalle.getCantidad(), detalle.getPrecioUnitario(), subtotal);
        }
    }
}
