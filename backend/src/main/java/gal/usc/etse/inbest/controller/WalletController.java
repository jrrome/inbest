package gal.usc.etse.inbest.controller;

import gal.usc.etse.inbest.model.dto.CreateWalletRequest;
import gal.usc.etse.inbest.model.dto.Wallet;
import gal.usc.etse.inbest.model.entity.User;
import gal.usc.etse.inbest.service.UserService;
import gal.usc.etse.inbest.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

// Cada usuario tiene como mucho una wallet
@RestController
@RequestMapping("/api")
public class WalletController {

    private final UserService userService;
    private final WalletService walletService;

    public WalletController(UserService userService, WalletService walletService) {
        this.userService = userService;
        this.walletService = walletService;
    }

    @GetMapping("/users/{id}/wallet")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Wallet> findWallet(@PathVariable("id") Long userId) {
        return ResponseEntity.of(walletService.findByUserId(userId).map(Wallet::from));
    }

    @GetMapping("/me/wallet")
    public ResponseEntity<Wallet> findWallet(@AuthenticationPrincipal Jwt jwt) {
        User currentUser = userService.getOrCreate(jwt);
        return ResponseEntity.of(walletService.findByUserId(currentUser.getId()).map(Wallet::from));
    }

    // El cuerpo es opcional: solo lleva el nombre de la wallet
    @PostMapping("/me/wallet")
    public ResponseEntity<Wallet> createWallet(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody(required = false) CreateWalletRequest request) {
        User currentUser = userService.getOrCreate(jwt);
        String name = request == null ? null : request.name();
        Wallet dto = Wallet.from(walletService.createWallet(currentUser, name));
        var location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(dto);
    }
}
