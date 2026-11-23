package gal.usc.etse.inbest.service;

import gal.usc.etse.inbest.model.entity.CashAccount;
import gal.usc.etse.inbest.model.entity.User;
import gal.usc.etse.inbest.model.entity.Wallet;
import gal.usc.etse.inbest.repository.CashAccountRepository;
import gal.usc.etse.inbest.repository.UserRepository;
import gal.usc.etse.inbest.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;

import java.math.BigDecimal;

// Crea al arrancar el usuario con el que opera la plataforma como creador de mercado, su wallet
// (la cuenta admin, que ya firma los despliegues) y su cuenta de efectivo.
// Es idempotente: cada pieza se crea solo si falta, así que arrancar varias veces no duplica nada
// y una ejecución que se quedó a medias se completa en la siguiente
@Slf4j
@Component
public class SystemAccountInitializer implements ApplicationRunner {

    public static final String AUTHENTICATION_ID = "inbest-system";
    public static final String USERNAME = "inbest-system";
    public static final String EMAIL = "system@inbest.local";

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final CashAccountRepository cashAccountRepository;
    private final Credentials adminCredentials;
    private final long chainId;
    private final BigDecimal initialCash;

    public SystemAccountInitializer(
            UserRepository userRepository,
            WalletRepository walletRepository,
            CashAccountRepository cashAccountRepository,
            Credentials adminCredentials,
            @Value("${inbest.blockchain.chain-id}") long chainId,
            @Value("${inbest.system.initial-cash}") BigDecimal initialCash) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.cashAccountRepository = cashAccountRepository;
        this.adminCredentials = adminCredentials;
        this.chainId = chainId;
        this.initialCash = initialCash;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User user = userRepository.findByAuthenticationId(AUTHENTICATION_ID)
                .orElseGet(() -> {
                    log.info("Creating system user {}", USERNAME);
                    return userRepository.save(new User(AUTHENTICATION_ID, USERNAME, EMAIL));
                });

        String address = adminCredentials.getAddress().toLowerCase();
        Wallet wallet = walletRepository.findByChainIdAndAddress(chainId, address)
                .orElseGet(() -> {
                    log.info("Creating system wallet {} on chain {}", address, chainId);
                    return walletRepository.save(new Wallet(user, chainId, address));
                });
        if (!wallet.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException(
                    "Wallet " + address + " belongs to user " + wallet.getUser().getId() + ", not to the system user");
        }

        if (cashAccountRepository.findByUser_Id(user.getId()).isEmpty()) {
            log.info("Creating system cash account with {} EUR", initialCash);
            CashAccount cashAccount = new CashAccount(user);
            cashAccount.setAvailableCash(initialCash);
            cashAccountRepository.save(cashAccount);
        }
    }
}
