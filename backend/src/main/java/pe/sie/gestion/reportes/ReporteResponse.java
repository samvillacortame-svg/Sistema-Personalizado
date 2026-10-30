package pe.sie.gestion.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReporteResponse(LocalDate desde, LocalDate hasta, long pedidos, long despachados,
        long pendientes, long cancelados, BigDecimal ventas, BigDecimal ticketPromedio,
        List<ReporteRepository.VentaCanal> ventasPorCanal,
        List<ReporteRepository.VentaDiaria> ventasDiarias,
        List<ReporteRepository.ProductoVendido> productosMasVendidos,
        List<ReporteRepository.StockBajo> productosConStockBajo) { }
