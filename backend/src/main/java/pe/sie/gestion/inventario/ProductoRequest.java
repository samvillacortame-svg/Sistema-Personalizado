package pe.sie.gestion.inventario;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductoRequest(
        @NotBlank @Size(max = 60) String sku,
        @NotBlank @Size(max = 180) String nombre,
        @Size(max = 500) String descripcion,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) BigDecimal precio,
        @PositiveOrZero int stockMinimo,
        @PositiveOrZero int stockInicial) { }
