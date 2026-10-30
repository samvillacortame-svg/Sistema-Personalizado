package pe.sie.gestion.clientes;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ClienteService {
    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(String busqueda, boolean incluirInactivos) {
        String termino = busqueda == null ? "" : busqueda.trim();
        return repository.buscar(termino, incluirInactivos).stream().map(ClienteResponse::desde).toList();
    }

    public ClienteResponse crear(ClienteRequest request) {
        String documento = normalizar(request.documento());
        validarDocumentoUnico(documento, null);
        Cliente cliente = new Cliente(requerido(request.nombre()), documento,
                normalizar(request.telefono()), normalizar(request.correo()));
        return ClienteResponse.desde(repository.save(cliente));
    }

    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = obtener(id);
        String documento = normalizar(request.documento());
        validarDocumentoUnico(documento, id);
        cliente.actualizar(requerido(request.nombre()), documento,
                normalizar(request.telefono()), normalizar(request.correo()));
        return ClienteResponse.desde(repository.save(cliente));
    }

    public ClienteResponse cambiarEstado(Long id, boolean activo) {
        Cliente cliente = obtener(id);
        cliente.setActivo(activo);
        return ClienteResponse.desde(repository.save(cliente));
    }

    private Cliente obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el cliente"));
    }

    private void validarDocumentoUnico(String documento, Long id) {
        if (documento == null) return;
        boolean duplicado = id == null ? repository.existsByDocumento(documento)
                : repository.existsByDocumentoAndIdNot(documento, id);
        if (duplicado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un cliente con ese documento");
        }
    }

    private static String requerido(String valor) {
        return valor.trim();
    }

    private static String normalizar(String valor) {
        if (valor == null || valor.isBlank()) return null;
        return valor.trim();
    }
}
