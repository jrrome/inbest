package gal.usc.etse.inbest.exception;

public class BlockchainPersistenceInconsistencyException extends RuntimeException {
    public BlockchainPersistenceInconsistencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
