package gal.usc.etse.inbest.repository;

import gal.usc.etse.inbest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// User es la entidad y Long el tipo de identificador
public interface UserRepository extends JpaRepository<User, Long> {
    // Con JpaRepository se heredan save() y findById()

    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
