package pe.sie.gestion;

import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EstadoController {
    private final JdbcTemplate jdbcTemplate;

    public EstadoController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/estado")
    public Map<String, String> estado() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return Map.of("estado", "disponible", "api", "conectada",
                "baseDeDatos", result != null && result == 1 ? "conectada" : "desconectada");
    }
}
