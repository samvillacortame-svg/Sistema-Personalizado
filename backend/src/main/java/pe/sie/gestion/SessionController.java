package pe.sie.gestion;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionController {
    @GetMapping("/api/sesion")
    public SessionResponse sesion(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream().map(authority -> authority.getAuthority()).toList();
        return new SessionResponse(authentication.getName(), roles);
    }

    public record SessionResponse(String usuario, List<String> roles) { }
}
