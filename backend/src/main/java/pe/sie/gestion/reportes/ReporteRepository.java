package pe.sie.gestion.reportes;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReporteRepository {
    private final JdbcTemplate jdbc;

    public ReporteRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Metricas consultarMetricas(LocalDateTime inicio, LocalDateTime fin) {
        String sql = """
                SELECT COUNT(DISTINCT p.id) AS pedidos,
                       COUNT(DISTINCT CASE WHEN p.estado = 'DESPACHADO' THEN p.id END) AS despachados,
                       COUNT(DISTINCT CASE WHEN p.estado IN ('CONFIRMADO','PREPARACION') THEN p.id END) AS pendientes,
                       COUNT(DISTINCT CASE WHEN p.estado = 'CANCELADO' THEN p.id END) AS cancelados,
                       COALESCE(SUM(CASE WHEN p.estado = 'DESPACHADO'
                           THEN d.cantidad * d.precio_unitario ELSE 0 END), 0) AS ventas
                FROM pedidos p
                LEFT JOIN pedido_detalles d ON d.pedido_id = p.id
                WHERE p.creado_en >= ? AND p.creado_en < ?
                """;
        return jdbc.queryForObject(sql, (rs, row) -> new Metricas(
                rs.getLong("pedidos"), rs.getLong("despachados"), rs.getLong("pendientes"),
                rs.getLong("cancelados"), nvl(rs.getBigDecimal("ventas"))),
                Timestamp.valueOf(inicio), Timestamp.valueOf(fin));
    }

    public List<VentaCanal> ventasPorCanal(LocalDateTime inicio, LocalDateTime fin) {
        String sql = """
                SELECT p.canal, COUNT(DISTINCT p.id) AS pedidos,
                       COALESCE(SUM(d.cantidad * d.precio_unitario), 0) AS ventas
                FROM pedidos p
                LEFT JOIN pedido_detalles d ON d.pedido_id = p.id
                WHERE p.estado = 'DESPACHADO' AND p.creado_en >= ? AND p.creado_en < ?
                GROUP BY p.canal ORDER BY ventas DESC
                """;
        return jdbc.query(sql, (rs, row) -> new VentaCanal(
                rs.getString("canal"), rs.getLong("pedidos"), nvl(rs.getBigDecimal("ventas"))),
                Timestamp.valueOf(inicio), Timestamp.valueOf(fin));
    }

    public List<VentaDiaria> ventasDiarias(LocalDateTime inicio, LocalDateTime fin) {
        String sql = """
                SELECT DATE(p.creado_en) AS fecha, COUNT(DISTINCT p.id) AS pedidos,
                       COALESCE(SUM(d.cantidad * d.precio_unitario), 0) AS ventas
                FROM pedidos p
                LEFT JOIN pedido_detalles d ON d.pedido_id = p.id
                WHERE p.estado = 'DESPACHADO' AND p.creado_en >= ? AND p.creado_en < ?
                GROUP BY DATE(p.creado_en) ORDER BY fecha ASC
                """;
        return jdbc.query(sql, (rs, row) -> new VentaDiaria(
                rs.getDate("fecha").toLocalDate(), rs.getLong("pedidos"), nvl(rs.getBigDecimal("ventas"))),
                Timestamp.valueOf(inicio), Timestamp.valueOf(fin));
    }

    public List<ProductoVendido> productosMasVendidos(LocalDateTime inicio, LocalDateTime fin) {
        String sql = """
                SELECT pr.id, pr.sku, pr.nombre, SUM(d.cantidad) AS unidades,
                       SUM(d.cantidad * d.precio_unitario) AS ventas
                FROM pedidos p
                JOIN pedido_detalles d ON d.pedido_id = p.id
                JOIN productos pr ON pr.id = d.producto_id
                WHERE p.estado = 'DESPACHADO' AND p.creado_en >= ? AND p.creado_en < ?
                GROUP BY pr.id, pr.sku, pr.nombre
                ORDER BY unidades DESC, ventas DESC LIMIT 5
                """;
        return jdbc.query(sql, (rs, row) -> new ProductoVendido(
                rs.getLong("id"), rs.getString("sku"), rs.getString("nombre"),
                rs.getLong("unidades"), nvl(rs.getBigDecimal("ventas"))),
                Timestamp.valueOf(inicio), Timestamp.valueOf(fin));
    }

    public List<StockBajo> productosConStockBajo() {
        String sql = """
                SELECT pr.id, pr.sku, pr.nombre, pr.stock_minimo,
                       COALESCE(s.fisico, 0) - COALESCE(s.reservado, 0) AS disponible
                FROM productos pr
                LEFT JOIN (
                    SELECT m.producto_id,
                           SUM(CASE WHEN m.tipo IN ('ENTRADA','DEVOLUCION') THEN m.cantidad
                                    WHEN m.tipo = 'SALIDA' THEN -m.cantidad
                                    WHEN m.tipo = 'AJUSTE' THEN m.cantidad ELSE 0 END) AS fisico,
                           SUM(CASE WHEN m.tipo = 'RESERVA' AND p.estado IN ('CONFIRMADO','PREPARACION')
                                    THEN m.cantidad ELSE 0 END) AS reservado
                    FROM movimientos_inventario m
                    LEFT JOIN pedidos p ON p.id = m.pedido_id
                    GROUP BY m.producto_id
                ) s ON s.producto_id = pr.id
                WHERE pr.activo = TRUE
                  AND COALESCE(s.fisico, 0) - COALESCE(s.reservado, 0) <= pr.stock_minimo
                ORDER BY disponible ASC, pr.nombre ASC LIMIT 10
                """;
        return jdbc.query(sql, (rs, row) -> new StockBajo(
                rs.getLong("id"), rs.getString("sku"), rs.getString("nombre"),
                rs.getInt("disponible"), rs.getInt("stock_minimo")));
    }

    private static BigDecimal nvl(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }

    public record Metricas(long pedidos, long despachados, long pendientes, long cancelados, BigDecimal ventas) { }
    public record VentaCanal(String canal, long pedidos, BigDecimal ventas) { }
    public record VentaDiaria(LocalDate fecha, long pedidos, BigDecimal ventas) { }
    public record ProductoVendido(Long productoId, String sku, String nombre, long unidades, BigDecimal ventas) { }
    public record StockBajo(Long productoId, String sku, String nombre, int disponible, int minimo) { }
}
