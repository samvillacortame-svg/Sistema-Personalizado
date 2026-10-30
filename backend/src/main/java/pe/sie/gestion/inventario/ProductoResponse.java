package pe.sie.gestion.inventario;

import java.math.BigDecimal;

public record ProductoResponse(Long id, String sku, String nombre, String descripcion,
        BigDecimal precio, int stockMinimo, int stockFisico, int stockReservado, int stockDisponible,
        boolean stockBajo, boolean activo) { }
