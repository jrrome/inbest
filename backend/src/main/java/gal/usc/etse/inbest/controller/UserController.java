package gal.usc.etse.inbest.controller;

import com.fasterxml.jackson.annotation.JsonView;
import gal.usc.etse.inbest.model.dto.User;
import gal.usc.etse.inbest.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: Revisar uso de @Valid y posible creación de ExceptionHandler

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
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
