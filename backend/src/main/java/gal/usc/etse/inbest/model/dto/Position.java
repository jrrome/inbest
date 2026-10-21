package gal.usc.etse.inbest.model.dto;

import java.math.BigInteger;

public record Position(
    Long id,
    Long walletId,
    Long assetId,
    BigInteger availableQuantity,
    BigInteger reservedQuantity
) {

    public static Position from(gal.usc.etse.inbest.model.entity.Position position) {
        return new Position(
            position.getId(),
            position.getWallet().getId(),
            position.getAsset().getId(),
            position.getAvailableQuantity(),
            position.getReservedQuantity()
        );
    }
}
