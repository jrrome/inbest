package gal.usc.etse.inbest.model.dto;

import gal.usc.etse.inbest.model.entity.CashTransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CashTransaction(
    Long id,
    Long cashAccountId,
    Long tradeId,
    CashTransactionType type,
    BigDecimal amount,
    OffsetDateTime createdAt
) {

    public static CashTransaction from(gal.usc.etse.inbest.model.entity.CashTransaction cashTransaction) {
        var trade = cashTransaction.getTrade();
        return new CashTransaction(
            cashTransaction.getId(),
            cashTransaction.getCashAccount().getId(),
            trade == null ? null : trade.getId(),
            cashTransaction.getType(),
            cashTransaction.getAmount(),
            cashTransaction.getCreatedAt()
        );
    }
}
