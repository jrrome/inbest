package gal.usc.etse.inbest.model.dto;

import gal.usc.etse.inbest.model.entity.SettlementStatus;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;

public record Trade(
    Long id,
    Long buyOrderId,
    Long sellOrderId,
    BigInteger quantity,
    BigDecimal price,
    SettlementStatus settlementStatus,
    Long chainId,
    String txHash,
    OffsetDateTime confirmedAt,
    OffsetDateTime executedAt
) {

    public static Trade from(gal.usc.etse.inbest.model.entity.Trade trade) {
        return new Trade(
            trade.getId(),
            trade.getBuyOrder().getId(),
            trade.getSellOrder().getId(),
            trade.getQuantity(),
            trade.getPrice(),
            trade.getSettlementStatus(),
            trade.getChainId(),
            trade.getTxHash(),
            trade.getConfirmedAt(),
            trade.getExecutedAt()
        );
    }
}
