package gal.usc.etse.inbest.repository;

import gal.usc.etse.inbest.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        // No integrarse con el compose
        "spring.docker.compose.enabled=false",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserRepositoryTests {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    private UserRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void saveAssignsIdAndPersistsUser() {
        User saved = repository.saveAndFlush(new User("jaime", "jaime@example.test"));

        // Obliga a leer desde PostgreSQL, en lugar de devolver la instancia en memoria
        entityManager.clear();

        User loaded = repository.findById(saved.getId()).orElseThrow();

        assertThat(saved.getId()).isPositive();
        assertThat(loaded).isNotSameAs(saved);
        assertThat(loaded.getUsername()).isEqualTo("jaime");
        assertThat(loaded.getEmail()).isEqualTo("jaime@example.test");
        assertThat(loaded.getCreatedAt()).isNotNull();
    }

    @Test
    void findByUsernameReturnsMatchingUser() {
        repository.saveAndFlush(new User("ana", "ana@example.test"));
        User expected = repository.saveAndFlush(new User("jaime", "jaime@example.test"));
        entityManager.clear();

        User found = repository.findByUsername("jaime").orElseThrow();

        assertThat(found.getId()).isEqualTo(expected.getId());
        assertThat(found.getUsername()).isEqualTo("jaime");
        assertThat(found.getEmail()).isEqualTo("jaime@example.test");
    }

    @Test
    void findByUsernameReturnsEmptyForUnknownUser() {
        repository.saveAndFlush(new User("jaime", "jaime@example.test"));
        entityManager.clear();

        assertThat(repository.findByUsername("inexistente")).isEmpty();
    }

    @Test
    void existsByUsernameReturnsWhetherUserExists() {
        repository.saveAndFlush(new User("jaime", "jaime@example.test"));
        entityManager.clear();

        assertThat(repository.existsByUsername("jaime")).isTrue();
        assertThat(repository.existsByUsername("inexistente")).isFalse();
    }

    @Test
    void saveRejectsDuplicateUsername() {
        repository.saveAndFlush(new User("jaime", "jaime@example.test"));

        assertThatThrownBy(() ->
                repository.saveAndFlush(new User("jaime", "otro@example.test")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveRejectsDuplicateEmail() {
        repository.saveAndFlush(new User("jaime", "jaime@example.test"));

        assertThatThrownBy(() ->
                repository.saveAndFlush(new User("otro", "jaime@example.test")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "",
            "Jaime@example.test",
            " jaime@example.test",
            "jaime@example.test "
    })
    void saveRejectsNullBlankOrUnnormalizedEmail(String email) {
        assertThatThrownBy(() -> repository.saveAndFlush(new User("jaime", email)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
