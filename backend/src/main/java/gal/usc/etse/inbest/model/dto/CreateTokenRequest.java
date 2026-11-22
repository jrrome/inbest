package gal.usc.etse.inbest.model.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateTokenRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 20) String symbol,
        String description,
        @NotNull @Positive @Digits(integer = 40, fraction = 18) BigDecimal initialSupply
) {
}
