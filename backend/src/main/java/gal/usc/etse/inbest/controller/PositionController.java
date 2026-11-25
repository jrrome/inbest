package gal.usc.etse.inbest.controller;

import gal.usc.etse.inbest.model.dto.Position;
import gal.usc.etse.inbest.model.entity.User;
import gal.usc.etse.inbest.service.PositionService;
import gal.usc.etse.inbest.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/me/positions")
public class PositionController {

    private final PositionService positionService;
    private final UserService userService;

    public PositionController(PositionService positionService, UserService userService) {
        this.positionService = positionService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Position>> getPositions(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.getOrCreate(jwt);
        List<Position> positions = positionService.findByUserId(user.getId()).stream().map(Position::from).toList();
        return ResponseEntity.ok(positions);
    }
}
