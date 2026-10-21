package gal.usc.etse.inbest.model.dto;

import gal.usc.etse.inbest.model.entity.OrderSide;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;

public record Order(
    Long id,
    Long userId,
    Long walletId,
    Long assetId,
    OrderSide side,
    BigInteger quantity,
    BigInteger filledQuantity,
    BigDecimal price,
    String status,
    OffsetDateTime createdAt
) {

    public static Order from(gal.usc.etse.inbest.model.entity.Order order) {
        return new Order(
            order.getId(),
            order.getUser().getId(),
            order.getWallet().getId(),
            order.getAsset().getId(),
            order.getSide(),
            order.getQuantity(),
            order.getFilledQuantity(),
            order.getPrice(),
            order.getStatus(),
            order.getCreatedAt()
        );
    }
}
