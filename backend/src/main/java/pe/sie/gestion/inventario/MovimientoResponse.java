package pe.sie.gestion.inventario;

import java.time.LocalDateTime;

public record MovimientoResponse(Long id, Long productoId, String productoNombre, Long pedidoId,
        TipoMovimiento tipo, int cantidad, String motivo, LocalDateTime creadoEn) { }
