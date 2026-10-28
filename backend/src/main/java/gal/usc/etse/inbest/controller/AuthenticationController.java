package gal.usc.etse.inbest.controller;

import com.fasterxml.jackson.annotation.JsonView;
import gal.usc.etse.inbest.model.dto.User;
import gal.usc.etse.inbest.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AuthenticationController {
    private final UserService userService;

    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/me")
    @JsonView(User.Views.Private.class)
    public User me(Authentication authentication) {
        return userService.findByUsername(authentication.getName())
            .map(User::from)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    @GetMapping("/api/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @GetMapping(value = "/ott/sent", produces = MediaType.TEXT_PLAIN_VALUE)
    public String tokenSent() {
        return "Si la cuenta existe, recibirás un enlace de acceso por correo. "
            + "Comprueba tu buzón y espera unos minutos antes de volver a solicitarlo.";
    }
}
