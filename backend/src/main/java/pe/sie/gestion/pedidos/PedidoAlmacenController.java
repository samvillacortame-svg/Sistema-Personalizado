package pe.sie.gestion.pedidos;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/almacen/pedidos")
public class PedidoAlmacenController {
    private final PedidoService service;

    public PedidoAlmacenController(PedidoService service) { this.service = service; }

    @GetMapping
    public List<PedidoResponse> listar() { return service.listar(); }

    @PostMapping("/{id}/preparar")
    public PedidoResponse preparar(@PathVariable Long id) { return service.preparar(id); }

    @PostMapping("/{id}/despachar")
    public PedidoResponse despachar(@PathVariable Long id) { return service.despachar(id); }

    @PostMapping("/{id}/devolver")
    public PedidoResponse devolver(@PathVariable Long id) { return service.registrarDevolucion(id); }
}
