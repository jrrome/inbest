package gal.usc.etse.inbest.controller;

import gal.usc.etse.inbest.model.dto.Wallet;
import gal.usc.etse.inbest.service.UserService;
import gal.usc.etse.inbest.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class WalletController{

    private final UserService userService;
    private final WalletService walletService;

    public WalletController(UserService userService, WalletService walletService) {
        this.userService = userService;
        this.walletService = walletService;
    }

    @GetMapping("/{id}/wallets")
    public ResponseEntity<List<Wallet>> findWallets(@PathVariable("id") Long userId, Authentication authentication) {

        var currentUser = userService.findByUsername(authentication.getName());

        if (currentUser.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!currentUser.get().getId().equals(userId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List <Wallet> wallets = walletService.findByUserId(userId).stream().map(Wallet::from).toList();

        return ResponseEntity.ok(wallets);
    }
}




