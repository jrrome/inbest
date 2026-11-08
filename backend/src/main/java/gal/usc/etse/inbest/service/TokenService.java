package gal.usc.etse.inbest.service;

import gal.usc.etse.inbest.blockchain.contract.SimulatedToken;
import gal.usc.etse.inbest.exception.BlockchainException;
import gal.usc.etse.inbest.model.entity.Token;
import gal.usc.etse.inbest.repository.TokenRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.util.Locale;

@Service
@Validated
public class TokenService {
    private final TokenRepository tokenRepository;
    private final Web3j web3j;
    private final TransactionManager txManager;
    private final ContractGasProvider contractGasProvider;
    private final Credentials adminCredentials;
    private final long chainId;


    public TokenService(
            TokenRepository tokenRepository,
            Web3j web3j,
            TransactionManager transactionManager,
            ContractGasProvider contractGasProvider,
            Credentials adminCredentials,
            @Value("${inbest.blockchain.chain-id}") long chainId)
    {
        this.tokenRepository = tokenRepository;
        this.web3j = web3j;
        this.txManager = transactionManager;
        this.contractGasProvider = contractGasProvider;
        this.adminCredentials = adminCredentials;
        this.chainId = chainId;
    }

    // https://docs.web3j.io/latest/quickstart/
    public Token deployToken(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 20) String symbol,
            String description)
    {
        SimulatedToken simulatedToken;
        short decimals;
        try {
            simulatedToken = SimulatedToken.deploy(
                    web3j,
                    txManager,
                    contractGasProvider,
                    name,
                    symbol,
                    adminCredentials.getAddress()
            ).send();
            decimals = simulatedToken.decimals().send().shortValueExact();
        } catch (Exception e) {
            throw new BlockchainException("No se pudo desplegar el token " + symbol, e);
        }

        // Locale.ROOT son cosas del idioma
        String address = simulatedToken.getContractAddress().toLowerCase(Locale.ROOT);
        Token createdToken = new Token(chainId, address, name, symbol);
        createdToken.setDecimals(decimals);
        createdToken.setDescription(description);

        return tokenRepository.save(createdToken);
    }
}
