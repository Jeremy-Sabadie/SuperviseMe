package fr.superviseme.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/** Sécurisation des routes de l'API SuperviseMe. */
@Configuration
// Active les contrôles d'accès déclarés sur les méthodes REST.
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        // Récupération du mot de passe accessible sans JWT.
                        // Les jetons seront vérifiés par le service métier.
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/public/contact").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/public/realisations",
                                "/api/public/realisations/*/image").permitAll()
                        .requestMatchers("/api/realisations", "/api/realisations/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers("/api/agenda", "/api/agenda/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/dossiers", "/api/dossiers/**").hasAnyRole("ADMIN", "USER")
                        // Consultation partagée ; création réservée à ADMIN au niveau métier.
                        .requestMatchers("/api/taches", "/api/taches/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/clients", "/api/clients/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/demandes-clients", "/api/demandes-clients/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.GET, "/api/utilisateurs").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/utilisateurs").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/utilisateurs/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/utilisateurs/*/desactiver",
                                "/api/utilisateurs/*/reactiver").hasRole("ADMIN")
                        // Configuration des statuts réservée à l'administrateur client.
                        // Le rôle OWNER reste réservé à l'administration globale.
                        .requestMatchers(HttpMethod.GET, "/api/admin/statuts",
                                "/api/admin/statuts/**").authenticated()
                        .requestMatchers("/api/admin/statuts",
                                "/api/admin/statuts/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities =
                new JwtGrantedAuthoritiesConverter();

        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);

        return converter;
    }
}
