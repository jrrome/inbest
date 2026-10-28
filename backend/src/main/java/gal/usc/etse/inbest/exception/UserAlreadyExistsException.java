package gal.usc.etse.inbest.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException() {
        super("El nombre de usuario o el correo ya están registrados.");
    }
}
