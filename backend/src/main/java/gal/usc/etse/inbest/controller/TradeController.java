package gal.usc.etse.inbest.controller;

import gal.usc.etse.inbest.model.dto.Trade;
import gal.usc.etse.inbest.model.entity.User;
import gal.usc.etse.inbest.service.TradeService;
import gal.usc.etse.inbest.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/trades")
public class TradeController {

    private final TradeService tradeService;
    private final UserService userService;

    public TradeController(TradeService tradeService, UserService userService) {
        this.tradeService = tradeService;
        this.userService = userService;
    }

    //Trade no tiene createdAt, se ordena por executedAt
    @GetMapping
    public ResponseEntity<Page<Trade>> getTrades(@AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 20, sort = "executedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        User user = userService.getOrCreate(jwt);
        Page<Trade> page = tradeService.findByUserId(user.getId(), pageable).map(Trade::from);
        return ResponseEntity.ok(page);
    }
}
