package gal.usc.etse.inbest.controller;

import com.fasterxml.jackson.annotation.JsonView;
import gal.usc.etse.inbest.model.dto.User;
import gal.usc.etse.inbest.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CurrentUserController {

    private final UserService userService;

    public CurrentUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @JsonView(User.Views.Private.class)
    public ResponseEntity<User> currentUser(@AuthenticationPrincipal Jwt jwt) {

        var entity = userService.getOrCreate(jwt);

        User dto = User.from(entity);
        return ResponseEntity.ok(dto);
    }
}