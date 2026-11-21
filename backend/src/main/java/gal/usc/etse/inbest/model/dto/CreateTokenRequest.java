package gal.usc.etse.inbest.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTokenRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 20) String symbol,
        String description
) {
}
