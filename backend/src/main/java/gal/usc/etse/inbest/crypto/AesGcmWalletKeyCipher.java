package gal.usc.etse.inbest.crypto;

import gal.usc.etse.inbest.exception.WalletKeyException;
import org.springframework.stereotype.Component;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/// Cifrado en sobre con AES-256-GCM
/// Cada wallet tiene su propia DEK, que cifra la clave privada, y la DEK se cifra con la clave maestra
/// (KEK) de la versión actual. Las claves maestras se leen de la configuración (fácil cambiarlo por un KMS)
@Component
public class AesGcmWalletKeyCipher implements WalletKeyCipher {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_LENGTH = 32;
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 16;

    private final SecureRandom random = new SecureRandom();
    private final Map<Short, SecretKey> masterKeys;
    private final short currentKekVersion;

    public AesGcmWalletKeyCipher(WalletKeyProperties properties) {
        if (properties.masterKeys() == null || properties.masterKeys().isEmpty()) {
            throw new IllegalStateException("No wallet master keys configured (inbest.wallet.master-keys)");
        }
        Map<Short, SecretKey> keys = new HashMap<>();
        properties.masterKeys().forEach((version, encoded) -> keys.put(version, decodeMasterKey(version, encoded)));
        if (!keys.containsKey(properties.currentKekVersion())) {
            throw new IllegalStateException("No wallet master key for current version " + properties.currentKekVersion());
        }
        // Copia inmutable
        this.masterKeys = Map.copyOf(keys);
        this.currentKekVersion = properties.currentKekVersion();
    }

    @Override
    public EncryptedWalletKey encrypt(String address, byte[] privateKey) {
        byte[] aad = aad(address);
        byte[] dek = new byte[KEY_LENGTH];
        random.nextBytes(dek);
        try {
            String encryptedPrivateKey = seal(new SecretKeySpec(dek, "AES"), privateKey, aad);
            String encryptedDek = seal(masterKeys.get(currentKekVersion), dek, aad);
            return new EncryptedWalletKey(encryptedPrivateKey, encryptedDek, currentKekVersion);
        } finally {
            Arrays.fill(dek, (byte) 0);
        }
    }

    @Override
    public byte[] decrypt(String address, EncryptedWalletKey encrypted) {
        SecretKey kek = masterKeys.get(encrypted.kekVersion());
        if (kek == null) {
            throw new WalletKeyException("No wallet master key for version " + encrypted.kekVersion());
        }
        byte[] aad = aad(address);
        byte[] dek = open(kek, encrypted.encryptedDek(), aad);
        try {
            return open(new SecretKeySpec(dek, "AES"), encrypted.encryptedPrivateKey(), aad);
        } finally {
            Arrays.fill(dek, (byte) 0);
        }
    }

    // Devuelve en Base64 el IV seguido del texto cifrado, que ya lleva el tag al final
    private String seal(SecretKey key, byte[] plaintext, byte[] aad) {
        byte[] iv = new byte[IV_LENGTH];
        random.nextBytes(iv);
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH * 8, iv));
            cipher.updateAAD(aad);
            byte[] ciphertext = cipher.doFinal(plaintext);
            byte[] sealed = ByteBuffer.allocate(IV_LENGTH + ciphertext.length).put(iv).put(ciphertext).array();
            return Base64.getEncoder().encodeToString(sealed);
        } catch (GeneralSecurityException e) {
            throw new WalletKeyException("Could not encrypt wallet key", e);
        }
    }

    private byte[] open(SecretKey key, String sealed, byte[] aad) {
        byte[] data;
        try {
            data = Base64.getDecoder().decode(sealed);
        } catch (IllegalArgumentException e) {
            throw new WalletKeyException("Encrypted wallet key is not valid Base64", e);
        }
        if (data.length <= IV_LENGTH + TAG_LENGTH) {
            throw new WalletKeyException("Encrypted wallet key is too short");
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH * 8, data, 0, IV_LENGTH));
            cipher.updateAAD(aad);
            return cipher.doFinal(data, IV_LENGTH, data.length - IV_LENGTH);
        } catch (AEADBadTagException e) {
            throw new WalletKeyException("Encrypted wallet key was altered or belongs to another wallet", e);
        } catch (GeneralSecurityException e) {
            throw new WalletKeyException("Could not decrypt wallet key", e);
        }
    }

    private static byte[] aad(String address) {
        return address.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8);
    }

    private static SecretKey decodeMasterKey(short version, String encoded) {
        byte[] key;
        try {
            key = Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Wallet master key " + version + " is not valid Base64", e);
        }
        if (key.length != KEY_LENGTH) {
            throw new IllegalStateException("Wallet master key " + version + " must be " + KEY_LENGTH + " bytes");
        }
        return new SecretKeySpec(key, "AES");
    }
}
