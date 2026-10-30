package pe.sie.gestion.clientes;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 160, message = "El nombre no puede superar 160 caracteres")
        String nombre,
        @Size(max = 24, message = "El documento no puede superar 24 caracteres")
        String documento,
        @Size(max = 32, message = "El teléfono no puede superar 32 caracteres")
        String telefono,
        @Email(message = "Ingresa un correo válido")
        @Size(max = 160, message = "El correo no puede superar 160 caracteres")
        String correo) {
}
