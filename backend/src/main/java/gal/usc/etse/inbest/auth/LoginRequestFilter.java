package gal.usc.etse.inbest.auth;

import gal.usc.etse.inbest.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Se registra solo en la cadena de Spring Security, después de comprobar CSRF.
public class LoginRequestFilter extends OncePerRequestFilter {
    private final RequestMatcher matcher = PathPatternRequestMatcher.withDefaults()
        .matcher(HttpMethod.POST, "/ott/generate");
    private final UserService userService;
    private final LoginRequestLimiter limiter;

    public LoginRequestFilter(UserService userService, LoginRequestLimiter limiter) {
        this.userService = userService;
        this.limiter = limiter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !matcher.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String username = request.getParameter("username");
        if (username == null || username.isBlank() || username.strip().length() > 30
                || !limiter.allow(request.getRemoteAddr(), username.strip())
                || userService.findByUsername(username.strip()).isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/ott/sent");
            return;
        }
        chain.doFilter(request, response);
    }
}
