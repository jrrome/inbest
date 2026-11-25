package gal.usc.etse.inbest.exception;

public class WalletKeyException extends RuntimeException {
    public WalletKeyException(String message) {
        super(message);
    }

    public WalletKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
