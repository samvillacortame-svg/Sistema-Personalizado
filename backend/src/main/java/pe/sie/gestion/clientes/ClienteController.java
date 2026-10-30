package pe.sie.gestion.clientes;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/ventas/clientes")
public class ClienteController {
    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    public List<ClienteResponse> listar(
            @RequestParam(defaultValue = "") String busqueda,
            @RequestParam(defaultValue = "false") boolean incluirInactivos) {
        return service.listar(busqueda, incluirInactivos);
    }

    @PostMapping
    public ClienteResponse crear(@Valid @RequestBody ClienteRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public ClienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return service.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public ClienteResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoCliente request) {
        return service.cambiarEstado(id, request.activo());
    }

    public record EstadoCliente(@NotNull Boolean activo) { }
}
