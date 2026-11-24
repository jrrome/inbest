package gal.usc.etse.inbest.controller;

import gal.usc.etse.inbest.model.dto.CashTransaction;
import gal.usc.etse.inbest.model.dto.CreateDepositRequest;
import gal.usc.etse.inbest.model.entity.User;
import gal.usc.etse.inbest.service.CashAccountService;
import gal.usc.etse.inbest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import gal.usc.etse.inbest.model.dto.CashAccount;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/me/cash")
public class CashAccountController {
    private final CashAccountService cashAccountService;
    private final UserService userService;

    public CashAccountController(CashAccountService cashAccountService, UserService userService) {
        this.cashAccountService = cashAccountService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<CashAccount> getCashAccount(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.getOrCreate(jwt);
        CashAccount dto = CashAccount.from(cashAccountService.getOrCreate(user));
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/deposits")
    public ResponseEntity<CashTransaction> deposit(@Valid @RequestBody CreateDepositRequest request, @AuthenticationPrincipal Jwt jwt) {
        User user = userService.getOrCreate(jwt);
        CashTransaction dto = CashTransaction.from(cashAccountService.deposit(user, request.amount()));

        //Obtener la localización
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(dto.id()).toUri();
        return ResponseEntity.created(location).body(dto);
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<CashTransaction>> getTransactions(@AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        User user = userService.getOrCreate(jwt);
        Page<CashTransaction> page = cashAccountService.findTransactions(user.getId(), pageable).map(CashTransaction::from);
        return ResponseEntity.ok(page);
    }

}
