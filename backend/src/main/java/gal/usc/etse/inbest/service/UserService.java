package gal.usc.etse.inbest.service;

import gal.usc.etse.inbest.model.entity.User;
import gal.usc.etse.inbest.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    /*
     * Keycloak es la fuente de verdad: el usuario se crea en su primera petición
     * y en las siguientes se copian los datos del token si han cambiado.
     * No es transaccional: si dos peticiones crean el mismo usuario a la vez,
     * la que pierde recibe la violación del unique en su propia transacción y relee la fila
     */
    public User getOrCreate(Jwt jwt) {
        String authenticationId = jwt.getSubject();
        Optional<User> existing = repository.findByAuthenticationId(authenticationId);
        if (existing.isPresent()) {
            User user = existing.get();
            return sync(user, jwt) ? repository.save(user) : user;
        }

        User user = new User(authenticationId, null, null);
        sync(user, jwt);
        try {
            return repository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            return repository.findByAuthenticationId(authenticationId).orElseThrow(() -> e);
        }
    }

    private boolean sync(User user, Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");

        boolean changed = !Objects.equals(user.getUsername(), username)
                || !Objects.equals(user.getEmail(), email)
                || !Objects.equals(user.getFirstName(), firstName)
                || !Objects.equals(user.getLastName(), lastName);

        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return changed;
    }

}
