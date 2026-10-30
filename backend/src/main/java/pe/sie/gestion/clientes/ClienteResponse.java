package pe.sie.gestion.clientes;

import java.time.LocalDateTime;

public record ClienteResponse(Long id, String nombre, String documento, String telefono,
        String correo, boolean activo, LocalDateTime creadoEn) {
    static ClienteResponse desde(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getNombre(), cliente.getDocumento(),
                cliente.getTelefono(), cliente.getCorreo(), cliente.isActivo(), cliente.getCreadoEn());
    }
}
