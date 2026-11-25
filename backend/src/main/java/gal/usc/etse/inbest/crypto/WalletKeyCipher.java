package gal.usc.etse.inbest.crypto;

/// Cifra y descifra las claves privadas de las wallets custodiadas por la plataforma.
public interface WalletKeyCipher {
    // Arrays de bytes en vez de Strings: un String es inmutable y no se puede borrar de memoria al acabar,
    // se queda en el heap hasta que lo libere el recolector de basura
    EncryptedWalletKey encrypt(String address, byte[] privateKey);

    byte[] decrypt(String address, EncryptedWalletKey encrypted);
}
