package gal.usc.etse.inbest.controller;

import gal.usc.etse.inbest.model.dto.Order;
import gal.usc.etse.inbest.model.entity.OrderStatus;
import gal.usc.etse.inbest.model.entity.User;
import gal.usc.etse.inbest.service.OrderService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<Order>> getOrders(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "status", required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        User user = userService.getOrCreate(jwt);
        Page<Order> page = orderService.findByUserId(user.getId(), status, pageable).map(Order::from);
        return ResponseEntity.ok(page);
    }
}
