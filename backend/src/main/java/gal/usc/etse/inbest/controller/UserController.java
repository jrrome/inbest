package gal.usc.etse.inbest.controller;

import com.fasterxml.jackson.annotation.JsonView;
import gal.usc.etse.inbest.model.dto.CreateUserRequest;
import gal.usc.etse.inbest.model.dto.User;
import gal.usc.etse.inbest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    @JsonView(User.Views.Private.class)
    public ResponseEntity<User> register(@Valid @RequestBody CreateUserRequest request) {
        var user = service.register(request);
        return ResponseEntity.created(URI.create("/api/users/" + user.getId()))
            .body(User.from(user));
    }

    @GetMapping("/{id}")
    @JsonView(User.Views.Public.class)
    public ResponseEntity<User> findById(@PathVariable Long id) {
        var result = service.findById(id);

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var entity = result.get();
        User dto = User.from(entity);

        return ResponseEntity.ok(dto);
    }
}
