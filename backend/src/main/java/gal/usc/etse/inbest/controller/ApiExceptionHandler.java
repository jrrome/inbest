package gal.usc.etse.inbest.controller;

import gal.usc.etse.inbest.exception.BlockchainException;
import gal.usc.etse.inbest.exception.WalletAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    // El detalle técnico va al log, al cliente solo un mensaje genérico
    @ExceptionHandler(BlockchainException.class)
    ProblemDetail handleBlockchain(BlockchainException e) {
        log.error(e.getMessage(), e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Blockchain not available, try again later");
    }

    @ExceptionHandler(WalletAlreadyExistsException.class)
    ProblemDetail handleWalletAlreadyExists(WalletAlreadyExistsException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "The user already has a wallet");
    }
}
