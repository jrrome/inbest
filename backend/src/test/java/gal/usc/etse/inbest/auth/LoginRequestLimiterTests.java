package gal.usc.etse.inbest.auth;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginRequestLimiterTests {
    @Test
    void limitsRequestsForTheSameUsernameAcrossDifferentAddresses() {
        var limiter = new LoginRequestLimiter();
        for (int i = 0; i < 3; i++) {
            assertThat(limiter.allow("client-" + i, "user")).isTrue();
        }
        assertThat(limiter.allow("another-client", "user")).isFalse();
    }

    @Test
    void limitsRequestsFromTheSameAddressAcrossDifferentUsernames() {
        var limiter = new LoginRequestLimiter();
        for (int i = 0; i < 30; i++) {
            assertThat(limiter.allow("client", "user-" + i)).isTrue();
        }
        assertThat(limiter.allow("client", "another-user")).isFalse();
    }

    @Test
    void allowsRequestsAgainWhenTheWindowExpires() {
        Clock clock = mock(Clock.class);
        Instant start = Instant.parse("2026-10-28T12:00:00Z");
        when(clock.instant()).thenReturn(start);
        var limiter = new LoginRequestLimiter(clock);
        for (int i = 0; i < 3; i++) {
            assertThat(limiter.allow("client", "user")).isTrue();
        }
        assertThat(limiter.allow("client", "user")).isFalse();
        when(clock.instant()).thenReturn(start.plusSeconds(300));
        assertThat(limiter.allow("client", "user")).isTrue();
    }
}
