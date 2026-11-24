package gal.usc.etse.inbest.model.dto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public record Position(
    Long id,
    Long walletId,
    Long assetId,
    BigInteger availableQuantity,
    BigInteger reservedQuantity,
    BigDecimal value
) {

    public static Position from(gal.usc.etse.inbest.model.entity.Position position) {
        var asset = position.getAsset();
        //Valor en euros de todo lo que tiene (disponible + reservado): cantidad / 10^decimales × precio
        BigInteger total = position.getAvailableQuantity().add(position.getReservedQuantity());
        BigDecimal value = new BigDecimal(total)
                .movePointLeft(asset.getToken().getDecimals())
                .multiply(asset.getPrice())
                .setScale(8, RoundingMode.HALF_UP);

        return new Position(
            position.getId(),
            position.getWallet().getId(),
            asset.getId(),
            position.getAvailableQuantity(),
            position.getReservedQuantity(),
            value
        );
    }
}
