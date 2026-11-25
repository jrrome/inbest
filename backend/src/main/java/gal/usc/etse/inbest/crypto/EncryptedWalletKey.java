package gal.usc.etse.inbest.crypto;

public record EncryptedWalletKey(String encryptedPrivateKey, String encryptedDek, short kekVersion) {
}
