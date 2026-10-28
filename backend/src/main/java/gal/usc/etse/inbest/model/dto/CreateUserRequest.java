package gal.usc.etse.inbest.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record CreateUserRequest(
    @NotBlank @Size(max = 30) String username,
    @NotBlank @Email @Size(max = 255) @Pattern(regexp = "\\S+") String email
) {
    public CreateUserRequest {
        if (username != null) {
            username = username.strip();
        }
        if (email != null) {
            email = email.strip().toLowerCase(Locale.ROOT);
        }
    }
}
