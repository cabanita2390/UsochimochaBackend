package com.app.usochicamochabackend.auth.security;

import com.app.usochicamochabackend.auth.application.service.UserDetailsServiceImp;
import com.app.usochicamochabackend.auth.security.filter.JwtTokenValidator;
import com.app.usochicamochabackend.auth.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.MediaType;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtils jwtUtils;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtTokenValidator(jwtUtils), BasicAuthenticationFilter.class)
                .authorizeHttpRequests(http -> {
                    http.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    http.requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll();
                    http.requestMatchers(HttpMethod.GET, "/uploads/**").permitAll();
                    http.requestMatchers(
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/v3/api-docs.yaml").permitAll();
                    http.requestMatchers("/api/v1/auth/**").permitAll();
                    http.requestMatchers(HttpMethod.POST, "/api/oil-changes/motor").hasAnyRole("MECANIC", "ADMIN");
                    http.requestMatchers(HttpMethod.POST, "/api/oil-changes/hydraulic").hasAnyRole("MECANIC", "ADMIN");
                    http.requestMatchers(HttpMethod.POST, "/api/v1/user/{id}/change-password").hasAnyRole("ADMIN");
                    http.requestMatchers(HttpMethod.POST, "/api/v1/inspection/**").hasAnyRole("MECANIC", "ADMIN");
                    http.requestMatchers("/new-data/notifications/**").hasRole("ADMIN");
                    http.requestMatchers("/api/actions/**").hasRole("ADMIN");
                    http.requestMatchers("/api/v1/results/**").hasRole("ADMIN");
                    http.requestMatchers(HttpMethod.POST, "/api/v1/inspection/**").hasRole("MECANIC");
                    http.requestMatchers("/api/v1/inspection/**").hasRole("ADMIN");
                    http.requestMatchers(HttpMethod.GET, "/api/v1/machine/**").hasAnyRole("MECANIC", "ADMIN");
                    http.requestMatchers("/api/v1/machine/**").hasRole("ADMIN");
                    http.requestMatchers(HttpMethod.GET, "/api/v1/moto/**").hasAnyRole("MECANIC", "ADMIN");
                    http.requestMatchers(HttpMethod.POST, "/api/v1/moto/**").hasAnyRole("MECANIC", "ADMIN");
                    http.requestMatchers(HttpMethod.POST, "/api/v1/user").permitAll();
                    http.requestMatchers("/api/v1/user/**").hasRole("ADMIN");
                    http.requestMatchers("/api/v1/order/**").hasRole("ADMIN");
                    http.requestMatchers("/api/v1/curriculum/**").hasRole("ADMIN");
                    http.requestMatchers(HttpMethod.GET, "/api/oil-changes/**").hasRole("ADMIN");
                    http.requestMatchers(HttpMethod.POST, "/api/oil-changes/**").hasRole("MECANIC");
                    http.requestMatchers("/api/oil-changes/**").hasRole("ADMIN");
                    http.requestMatchers(HttpMethod.GET, "/api/v1/oil/brand/**").hasAnyRole("MECANIC", "ADMIN");
                    http.requestMatchers("/api/v1/oil/brand/**").hasRole("ADMIN");
                    http.requestMatchers("/oil_change/notifications/**").hasRole("ADMIN");
                    http.anyRequest().hasRole("ADMIN");
                })
                .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler()));

        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:8080");
        configuration.addAllowedOrigin("https://usochimochabackend.onrender.com");
        configuration.addAllowedOrigin("http://localhost:5173");
        configuration.addAllowedOrigin("http://localhost:5174");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsServiceImp userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\": \"Access denied\"}");
        };
    }

}
