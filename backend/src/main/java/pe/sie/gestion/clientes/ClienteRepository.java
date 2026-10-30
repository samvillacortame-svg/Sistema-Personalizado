package pe.sie.gestion.clientes;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByDocumentoAndIdNot(String documento, Long id);
    boolean existsByDocumento(String documento);

    @Query("""
            select c from Cliente c
            where (:incluirInactivos = true or c.activo = true)
              and (:busqueda = ''
                   or lower(c.nombre) like lower(concat('%', :busqueda, '%'))
                   or lower(coalesce(c.documento, '')) like lower(concat('%', :busqueda, '%'))
                   or lower(coalesce(c.telefono, '')) like lower(concat('%', :busqueda, '%')))
            order by c.nombre asc
            """)
    List<Cliente> buscar(@Param("busqueda") String busqueda,
            @Param("incluirInactivos") boolean incluirInactivos);

    Optional<Cliente> findById(Long id);
}
