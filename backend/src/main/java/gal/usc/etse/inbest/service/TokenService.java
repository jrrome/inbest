package gal.usc.etse.inbest.service;

import gal.usc.etse.inbest.blockchain.contract.SimulatedToken;
import gal.usc.etse.inbest.model.entity.Token;
import gal.usc.etse.inbest.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.util.Optional;

@Service
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
    public Optional<Token> deployToken(Long chainId, String name, String symbol, Short decimals, String description)
            throws Exception
    {
        SimulatedToken simulatedToken = SimulatedToken.deploy(
                web3j,
                txManager,
                contractGasProvider,
                name,
                symbol,
                adminCredentials.getAddress()
        ).send();

        if (simulatedToken == null || !simulatedToken.isValid()) {
            return Optional.empty();
        }

        Token createdToken = new Token(chainId, simulatedToken.getContractAddress(), name, symbol);
        createdToken.setDecimals(decimals);
        createdToken.setDescription(description);

        tokenRepository.save(createdToken);

        return Optional.of(createdToken);
    }

}
