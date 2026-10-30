package pe.sie.gestion.reportes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class ReporteService {
    private static final ZoneId ZONA_NEGOCIO = ZoneId.of("America/Lima");
    private final ReporteRepository repository;

    public ReporteService(ReporteRepository repository) { this.repository = repository; }

    public ReporteResponse resumen(LocalDate desde, LocalDate hasta) {
        LocalDate hoy = LocalDate.now(ZONA_NEGOCIO);
        LocalDate inicio = desde == null ? hoy.withDayOfMonth(1) : desde;
        LocalDate fin = hasta == null ? hoy : hasta;
        if (fin.isBefore(inicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha final no puede ser anterior a la inicial");
        }
        if (ChronoUnit.DAYS.between(inicio, fin) > 366) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rango máximo del reporte es de 367 días");
        }
        LocalDateTime inicioTiempo = inicio.atStartOfDay(ZONA_NEGOCIO)
                .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime finExclusivo = fin.plusDays(1).atStartOfDay(ZONA_NEGOCIO)
                .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        ReporteRepository.Metricas metricas = repository.consultarMetricas(inicioTiempo, finExclusivo);
        BigDecimal ticket = metricas.despachados() == 0 ? BigDecimal.ZERO
                : metricas.ventas().divide(BigDecimal.valueOf(metricas.despachados()), 2, RoundingMode.HALF_UP);
        return new ReporteResponse(inicio, fin, metricas.pedidos(), metricas.despachados(),
                metricas.pendientes(), metricas.cancelados(), metricas.ventas(), ticket,
                repository.ventasPorCanal(inicioTiempo, finExclusivo),
                repository.ventasDiarias(inicioTiempo, finExclusivo),
                repository.productosMasVendidos(inicioTiempo, finExclusivo),
                repository.productosConStockBajo());
    }
}
