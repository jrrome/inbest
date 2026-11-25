package gal.usc.etse.inbest.service;

import gal.usc.etse.inbest.blockchain.contract.SimulatedToken;
import gal.usc.etse.inbest.exception.BlockchainException;
import gal.usc.etse.inbest.exception.BlockchainPersistenceInconsistencyException;
import gal.usc.etse.inbest.model.entity.Token;
import gal.usc.etse.inbest.model.entity.User;
import gal.usc.etse.inbest.repository.TokenRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;

@Service
@Validated
@Slf4j
public class TokenService {
    private static final int TOKEN_DECIMALS = 18;

    private final TokenRepository tokenRepository;
    private final Web3j web3j;
    private final TransactionManager txManager;
    private final ContractGasProvider contractGasProvider;
    private final Credentials adminCredentials;
    private final long chainId;
    private final EventService eventService;
    private final TransactionTemplate transactionTemplate;

    public TokenService(
            TokenRepository tokenRepository,
            Web3j web3j,
            TransactionManager transactionManager,
            ContractGasProvider contractGasProvider,
            Credentials adminCredentials,
            @Value("${inbest.blockchain.chain-id}") long chainId, EventService eventService, TransactionTemplate transactionTemplate)
    {
        this.tokenRepository = tokenRepository;
        this.web3j = web3j;
        this.txManager = transactionManager;
        this.contractGasProvider = contractGasProvider;
        this.adminCredentials = adminCredentials;
        this.chainId = chainId;
        this.eventService = eventService;
        this.transactionTemplate = transactionTemplate;
    }

    // para continuar, ahora estabas implementando el logging aquí, queda también una excepción custom,
    // eventual consistency, event log, etc., ver lo de AOP

    // Esto podría ser una transacción heterogénea. La fuente de verdad es la blockchain, la base de datos debe
    // describir el estado de la blockchain, por lo que sea eventualmente consistente es aceptable (de momento no
    // lo es)
    public Token deployToken(
            User user,
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 20) String symbol,
            String description,
            @NotNull @Positive BigDecimal initialSupply)
    {
        // TODO: evento preparación creación de token
        log.info("User {} is deploying token {} ({})", user.getId(), name, symbol);
        // La emisión llega en tokens y el contrato la recibe en unidades mínimas
        BigInteger initialSupplyUnits = initialSupply.movePointRight(TOKEN_DECIMALS).toBigIntegerExact();
        SimulatedToken simulatedToken;
        short decimals;
        String txHash;
        try {
            // La emisión inicial va a la wallet del sistema, que usa la cuenta admin
            simulatedToken = SimulatedToken.deploy(
                    web3j,
                    txManager,
                    contractGasProvider,
                    name,
                    symbol,
                    adminCredentials.getAddress(),
                    adminCredentials.getAddress(),
                    initialSupplyUnits
            ).send();
        } catch (Exception e) {
            log.error("Error deploying the token {} ({})", name, symbol);
            throw new BlockchainException("Error deploying the token " + symbol, e);
        }
        try {
            decimals = simulatedToken.decimals().send().shortValueExact();
            TransactionReceipt receipt = simulatedToken.getTransactionReceipt().orElse(null);
            if (receipt == null || receipt.getTransactionHash() == null) {
                throw new IllegalStateException("Deploy Receipt not available");
            }
            txHash = receipt.getTransactionHash().toLowerCase(Locale.ROOT);
        } catch (Exception e) {
            log.error("Error getting token {} details", symbol);
            throw new BlockchainException("Error getting token " + symbol + " details", e);
        }
        // TODO: evento token creado en la blockchain
        log.info("Token {} ({}) successfully deployed in blockchain", name, symbol);

        String address = simulatedToken.getContractAddress().toLowerCase(Locale.ROOT);
        Token createdToken = new Token(chainId, address, name, symbol);
        createdToken.setDecimals(decimals);
        createdToken.setDescription(description);

        log.info("Saving token {} ({}) to the database", name, symbol);
        try {
            // Transacción que envuelve el guardado del token creado y el evento
            return transactionTemplate.execute(status -> {
                Token savedToken = tokenRepository.save(createdToken);
                eventService.logTokenDeployEvent(user, savedToken, txHash);
                return savedToken;
            });
        } catch (Exception e) {
            // TODO: evento error creando token
            // TODO: el objetivo es que el sistema se recupere solo de la inconsistencia, añadir a cola?
            log.error("Error saving the token {} ({}) to the database, leaving inconsistent state between blockchain " +
                    "and database", name, symbol);
            throw new BlockchainPersistenceInconsistencyException("Error saving the token " + symbol +
                    " to the database", e);
        }
    }
}
