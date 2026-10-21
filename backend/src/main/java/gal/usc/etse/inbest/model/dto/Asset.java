package gal.usc.etse.inbest.model.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Asset(
    Long id,
    Long tokenId,
    String symbol,
    String name,
    BigDecimal price,
    OffsetDateTime createdAt
) {

    public static Asset from(gal.usc.etse.inbest.model.entity.Asset asset) {
        return new Asset(
            asset.getId(),
            asset.getToken().getId(),
            asset.getSymbol(),
            asset.getName(),
            asset.getPrice(),
            asset.getCreatedAt()
        );
    }
}
