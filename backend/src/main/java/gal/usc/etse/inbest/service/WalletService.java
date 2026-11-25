package gal.usc.etse.inbest.service;

import gal.usc.etse.inbest.crypto.EncryptedWalletKey;
import gal.usc.etse.inbest.crypto.WalletKeyCipher;
import gal.usc.etse.inbest.exception.WalletAlreadyExistsException;
import gal.usc.etse.inbest.model.entity.User;
import gal.usc.etse.inbest.model.entity.Wallet;
import gal.usc.etse.inbest.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Service
public class WalletService {

    private final WalletRepository repository;
    private final WalletKeyCipher walletKeyCipher;
    private final EventService eventService;
    private final long chainId;

    public WalletService(
            WalletRepository repository,
            WalletKeyCipher walletKeyCipher,
            EventService eventService,
            @Value("${inbest.blockchain.chain-id}") long chainId) {
        this.repository = repository;
        this.walletKeyCipher = walletKeyCipher;
        this.eventService = eventService;
        this.chainId = chainId;
    }

    public Optional<Wallet> findByUserId(Long userId) {
        return repository.findByUser_Id(userId);
    }

     // Genera el par de claves en local y guarda la privada cifrada. Una dirección existe
     // en cuanto se genera la clave. El ETH para el gas se envía más adelante, cuando haga falta
    @Transactional
    public Wallet createWallet(User user, String name) {
        if (repository.existsByUser_Id(user.getId())) {
            throw new WalletAlreadyExistsException(user.getId());
        }

        ECKeyPair keyPair;
        try {
            keyPair = Keys.createEcKeyPair();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not generate wallet key pair", e);
        }
        String address = "0x" + Keys.getAddress(keyPair);
        byte[] privateKey = Numeric.toBytesPadded(keyPair.getPrivateKey(), 32);

        Wallet wallet;
        try {
            EncryptedWalletKey encrypted = walletKeyCipher.encrypt(address, privateKey);
            wallet = new Wallet(user, chainId, address,
                    encrypted.encryptedPrivateKey(), encrypted.encryptedDek(), encrypted.kekVersion());
        } finally {
            Arrays.fill(privateKey, (byte) 0);
        }
        wallet.setName(name);

        Wallet saved;
        try {
            saved = repository.saveAndFlush(wallet);
        } catch (DataIntegrityViolationException e) {
            throw new WalletAlreadyExistsException(user.getId(), e);
        }

        eventService.logWalletCreateEvent(user, saved);
        log.info("Created wallet {} for user {}", saved.getAddress(), user.getId());
        return saved;
    }
}
