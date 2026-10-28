package gal.usc.etse.inbest.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiExceptionHandlerTests {
    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @ParameterizedTest
    @ValueSource(strings = {"users_username_key", "uq_users_email"})
    void concurrentDuplicateRegistrationReturnsConflict(String constraint) {
        var cause = new ConstraintViolationException("Duplicate", new SQLException("Duplicate", "23505"), constraint);
        var problem = handler.integrityViolation(new DataIntegrityViolationException("Duplicate", cause));
        assertThat(problem.getStatus()).isEqualTo(409);
    }

    @Test
    void unrelatedIntegrityViolationsAreNotReportedAsDuplicateUsers() {
        var exception = new DataIntegrityViolationException("Cash account failure");
        assertThatThrownBy(() -> handler.integrityViolation(exception)).isSameAs(exception);
    }

    @Test
    void anUnnamedConstraintDoesNotMaskTheOriginalFailure() {
        var cause = new ConstraintViolationException("Unknown constraint",
            new SQLException("Unknown constraint", "23505"), (String) null);
        var exception = new DataIntegrityViolationException("Unknown constraint", cause);
        assertThatThrownBy(() -> handler.integrityViolation(exception)).isSameAs(exception);
    }
}
