package gal.usc.etse.inbest.model.dto;

import com.fasterxml.jackson.annotation.JsonView;

import java.time.OffsetDateTime;

public record User(
    @JsonView(Views.Private.class)
    Long id,
    @JsonView(Views.Public.class)
    String username,
    @JsonView(Views.Private.class)
    String email,
    @JsonView(Views.Private.class)
    OffsetDateTime createdAt
) {

    public static User from(gal.usc.etse.inbest.model.entity.User user) {
        return new User(user.getId(), user.getUsername(), user.getEmail(), user.getCreatedAt());
    }

    public interface Views {
        interface Public {}
        interface Private extends Public {}
    }
}
