package pe.sie.gestion.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/estado").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/ventas/**").hasAnyRole("ADMIN", "VENTAS")
                        .requestMatchers("/api/almacen/**").hasAnyRole("ADMIN", "ALMACEN")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    UserDetailsService userDetailsService(Environment environment, PasswordEncoder passwordEncoder) {
        List<UserDetails> users = new ArrayList<>();
        addUser(users, environment, passwordEncoder, "APP_ADMIN", "ADMIN");
        addUser(users, environment, passwordEncoder, "APP_VENTAS", "VENTAS");
        addUser(users, environment, passwordEncoder, "APP_ALMACEN", "ALMACEN");
        return new InMemoryUserDetailsManager(users);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void addUser(List<UserDetails> users, Environment environment, PasswordEncoder encoder,
            String prefix, String role) {
        String username = environment.getProperty(prefix + "_USERNAME", "").trim();
        String password = environment.getProperty(prefix + "_PASSWORD", "");
        if (!username.isBlank() && !password.isBlank()) {
            users.add(User.withUsername(username).password(encoder.encode(password)).roles(role).build());
        }
    }
}
