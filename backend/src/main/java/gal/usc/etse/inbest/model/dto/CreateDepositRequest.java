package gal.usc.etse.inbest.model.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateDepositRequest(
        @NotNull @Positive @DecimalMax("100000") @Digits(integer = 12, fraction = 8) BigDecimal amount
) {
}
