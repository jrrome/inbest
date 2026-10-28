package gal.usc.etse.inbest.auth;

import gal.usc.etse.inbest.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.InMemoryOneTimeTokenService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.ott.GenerateOneTimeTokenFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    InMemoryOneTimeTokenService oneTimeTokenService() {
        return new InMemoryOneTimeTokenService();
    }

    @Bean
    UserDetailsService userDetailsService(UserService userService) {
        return username -> {
            var user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
            // El principal requiere un password; no se habilita autenticación por contraseña.
            return User.withUsername(user.getUsername()).password("").roles("USER").build();
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, UserService userService,
            LoginRequestLimiter limiter, LoginTokenMailHandler mailHandler) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/csrf", "/api/users/{id}").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                .requestMatchers("/login", "/login/ott", "/ott/generate", "/ott/sent", "/error")
                    .permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                PathPatternRequestMatcher.withDefaults().matcher("/api/**")
            ))
            .oneTimeTokenLogin(ott -> ott
                .tokenGenerationSuccessHandler(mailHandler)
                .generateRequestResolver(request ->
                    new GenerateOneTimeTokenRequest(request.getParameter("username").strip()))
                .defaultSuccessUrl("/api/me", true)
                .permitAll()
            )
            .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll())
            .addFilterBefore(new LoginRequestFilter(userService, limiter), GenerateOneTimeTokenFilter.class);
        return http.build();
    }
}
