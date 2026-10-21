package gal.usc.etse.inbest.model.dto;

import java.math.BigDecimal;

public record CashAccount(
    Long id,
    Long userId,
    String currency,
    BigDecimal availableCash,
    BigDecimal reservedCash
) {

    public static CashAccount from(gal.usc.etse.inbest.model.entity.CashAccount cashAccount) {
        return new CashAccount(
            cashAccount.getId(),
            cashAccount.getUser().getId(),
            cashAccount.getCurrency(),
            cashAccount.getAvailableCash(),
            cashAccount.getReservedCash()
        );
    }
}
