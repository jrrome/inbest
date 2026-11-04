package gal.usc.etse.inbest.service;

import gal.usc.etse.inbest.repository.TokenRepository;

public class TokenService {
    private final TokenRepository tokenRepository;

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }


}
