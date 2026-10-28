package gal.usc.etse.inbest.auth;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class LoginRequestLimiter {
    private static final Duration WINDOW = Duration.ofMinutes(5);
    private static final int MAX_ENTRIES = 10_000;

    private final Map<String, Attempts> attempts = new HashMap<>();
    private final Clock clock;

    public LoginRequestLimiter() {
        this(Clock.systemUTC());
    }

    LoginRequestLimiter(Clock clock) {
        this.clock = clock;
    }

    public synchronized boolean allow(String address, String username) {
        Instant now = clock.instant();
        attempts.entrySet().removeIf(entry -> !entry.getValue().resetsAt().isAfter(now));
        return take("ip:" + address, 30, now) && take("user:" + username, 3, now);
    }

    private boolean take(String key, int limit, Instant now) {
        Attempts previous = attempts.get(key);
        if (previous == null) {
            if (attempts.size() >= MAX_ENTRIES) {
                return false;
            }
            attempts.put(key, new Attempts(1, now.plus(WINDOW)));
            return true;
        }
        if (previous.count() >= limit) {
            return false;
        }
        attempts.put(key, new Attempts(previous.count() + 1, previous.resetsAt()));
        return true;
    }

    private record Attempts(int count, Instant resetsAt) {}
}
