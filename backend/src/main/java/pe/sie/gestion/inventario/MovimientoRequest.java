package pe.sie.gestion.inventario;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MovimientoRequest(
        @NotNull TipoMovimiento tipo,
        int cantidad,
        @Size(max = 240) String motivo) { }
