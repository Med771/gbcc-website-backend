package backend.website.gbcc.config;

import backend.website.gbcc.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/product").permitAll()
                        .requestMatchers(HttpMethod.POST, "/news").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/news/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/news/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/refresh", "/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.POST, "/account/customer/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/promotion", "/promotion/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/promotion").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/promotion/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/promotion/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/promotion/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/support/conversations/my").authenticated()
                        .requestMatchers(HttpMethod.GET, "/support/conversations").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/support/conversations/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/support/conversations").permitAll()
                        .requestMatchers(HttpMethod.POST, "/support/conversations/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/support/conversations/{conversationId:[0-9a-fA-F\\-]{36}}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/contact-requests").permitAll()
                        .requestMatchers(HttpMethod.GET, "/contact-requests").authenticated()
                        .requestMatchers(HttpMethod.GET, "/contact-requests/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/contact-requests/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/cooperation-requests").permitAll()
                        .requestMatchers(HttpMethod.GET, "/cooperation-requests").authenticated()
                        .requestMatchers(HttpMethod.GET, "/cooperation-requests/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/cooperation-requests/**").authenticated()
                        .requestMatchers("/order/**").authenticated()
                        .requestMatchers("/account/**").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Username/password authentication is disabled");
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
