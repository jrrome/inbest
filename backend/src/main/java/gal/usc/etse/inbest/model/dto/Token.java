package gal.usc.etse.inbest.model.dto;

import java.time.OffsetDateTime;

public record Token(
    Long id,
    Long chainId,
    String contractAddress,
    String name,
    String symbol,
    Short decimals,
    OffsetDateTime createdAt,
    String description
) {

    public static Token from(gal.usc.etse.inbest.model.entity.Token token) {
        return new Token(
            token.getId(),
            token.getChainId(),
            token.getContractAddress(),
            token.getName(),
            token.getSymbol(),
            token.getDecimals(),
            token.getCreatedAt(),
            token.getDescription()
        );
    }
}
