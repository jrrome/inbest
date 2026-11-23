package gal.usc.etse.inbest.model.dto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;

public record Asset(
    Long id,
    Long tokenId,
    String symbol,
    String name,
    BigDecimal price,
    BigInteger initialSupply,
    BigDecimal spread,
    BigDecimal priceImpact,
    BigInteger maxQuoteSize,
    OffsetDateTime createdAt
) {

    public static Asset from(gal.usc.etse.inbest.model.entity.Asset asset) {
        var token = asset.getToken();
        return new Asset(
            asset.getId(),
            token.getId(),
            token.getSymbol(),
            token.getName(),
            asset.getPrice(),
            asset.getInitialSupply(),
            asset.getSpread(),
            asset.getPriceImpact(),
            asset.getMaxQuoteSize(),
            asset.getCreatedAt()
        );
    }
}
