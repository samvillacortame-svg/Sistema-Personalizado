package pe.sie.gestion.inventario;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductoController {
    private final ProductoService service;

    public ProductoController(ProductoService service) { this.service = service; }

    @GetMapping("/api/ventas/productos")
    public List<ProductoResponse> listarVentas() { return service.listar(false); }

    @GetMapping("/api/almacen/productos")
    public List<ProductoResponse> listarAlmacen(@RequestParam(defaultValue = "false") boolean incluirInactivos) {
        return service.listar(incluirInactivos);
    }

    @PostMapping("/api/almacen/productos")
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest request) { return service.crear(request); }

    @PostMapping("/api/almacen/productos/{id}/movimientos")
    public MovimientoResponse movimiento(@PathVariable Long id, @Valid @RequestBody MovimientoRequest request) {
        return service.registrarMovimiento(id, request);
    }

    @GetMapping("/api/almacen/movimientos")
    public List<MovimientoResponse> movimientos() { return service.listarMovimientos(); }
}
