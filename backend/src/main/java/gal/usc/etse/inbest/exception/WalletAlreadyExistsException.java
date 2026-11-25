package gal.usc.etse.inbest.exception;

public class WalletAlreadyExistsException extends RuntimeException {
    public WalletAlreadyExistsException(Long userId) {
        super("User " + userId + " already has a wallet");
    }

    public WalletAlreadyExistsException(Long userId, Throwable cause) {
        super("User " + userId + " already has a wallet", cause);
    }
}
