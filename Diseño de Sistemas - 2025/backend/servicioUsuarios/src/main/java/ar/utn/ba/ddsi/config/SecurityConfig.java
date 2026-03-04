package ar.utn.ba.ddsi.config;

import ar.utn.ba.ddsi.filters.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("=== CONFIGURANDO SECURITY ===");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/api/auth/**", "/api/auth/refresh/**", "/api/public/**", "/api/admin/**", "/api/public/user/**", "/error").permitAll();
                    auth.requestMatchers("/api/auth/user/roles-permisos").authenticated();
                    auth.anyRequest().authenticated();
                })
                // 3. Usa la instancia ya inyectada en lugar de crear una nueva
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
/*
    ORIGINAL:
    .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/api/auth/**", "/api/auth/refresh/**").permitAll();
                    auth.requestMatchers("/api/auth/user/roles-permisos").authenticated();
                    auth.anyRequest().authenticated();
                })
)*/