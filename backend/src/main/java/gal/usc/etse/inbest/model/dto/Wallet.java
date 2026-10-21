package gal.usc.etse.inbest.model.dto;

public record Wallet(
    Long id,
    Long userId,
    Long chainId,
    String address
) {

    public static Wallet from(gal.usc.etse.inbest.model.entity.Wallet wallet) {
        return new Wallet(
            wallet.getId(),
            wallet.getUser().getId(),
            wallet.getChainId(),
            wallet.getAddress()
        );
    }
}
