package pe.sie.gestion.pedidos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    @EntityGraph(attributePaths = {"cliente", "detalles", "detalles.producto"})
    List<Pedido> findAllByOrderByCreadoEnDesc();

    @Override
    @EntityGraph(attributePaths = {"cliente", "detalles", "detalles.producto"})
    Optional<Pedido> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id = :id")
    Optional<Pedido> lockById(@Param("id") Long id);
}
