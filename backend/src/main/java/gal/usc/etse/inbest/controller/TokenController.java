package gal.usc.etse.inbest.controller;

import gal.usc.etse.inbest.model.dto.CreateTokenRequest;
import gal.usc.etse.inbest.model.dto.Token;
import gal.usc.etse.inbest.model.entity.User;
import gal.usc.etse.inbest.service.TokenService;
import gal.usc.etse.inbest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {
    private final TokenService tokenService;
    private final UserService userService;

    public TokenController(TokenService tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    /*
    @GetMapping("/{id}")
    public Token getTokenById(@PathVariable Long id) {
        Token found = tokenService.
    }
    */

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Token> deployToken(@Valid @RequestBody CreateTokenRequest request,
                                             @AuthenticationPrincipal Jwt jwt) {
        User user = userService.getOrCreate(jwt);
        Token dto = Token.from(tokenService.deployToken(user, request.name(), request.symbol(), request.description(),
                request.initialSupply()));
        // Obtener la localización
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(dto.id()).toUri();
        return ResponseEntity.created(location).body(dto);
    }
}
