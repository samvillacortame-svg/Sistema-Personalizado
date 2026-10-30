package pe.sie.gestion.inventario;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovimientoRepository extends JpaRepository<MovimientoInventario, Long> {
    @Query(value = "select coalesce(sum(case when tipo in ('ENTRADA','DEVOLUCION') then cantidad "
            + "when tipo = 'SALIDA' then -cantidad when tipo = 'AJUSTE' then cantidad else 0 end),0) "
            + "from movimientos_inventario where producto_id = :productoId", nativeQuery = true)
    Long stockFisico(@Param("productoId") Long productoId);

    @Query(value = "select coalesce(sum(m.cantidad),0) from movimientos_inventario m "
            + "join pedidos p on p.id = m.pedido_id where m.producto_id = :productoId "
            + "and m.tipo = 'RESERVA' and p.estado in ('CONFIRMADO','PREPARACION')", nativeQuery = true)
    Long stockReservado(@Param("productoId") Long productoId);

    @Query("select m from MovimientoInventario m join fetch m.producto order by m.creadoEn desc")
    List<MovimientoInventario> recientes();
}
