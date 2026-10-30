package pe.sie.gestion.pedidos;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PedidoRequest(
        @NotNull Long clienteId,
        @NotNull CanalPedido canal,
        @NotEmpty List<@Valid PedidoItemRequest> items) { }
