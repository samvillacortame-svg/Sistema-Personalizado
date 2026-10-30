package pe.sie.gestion.pedidos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

public record PedidoItemRequest(@NotNull Long productoId, @Positive @Max(1000000) int cantidad) { }
