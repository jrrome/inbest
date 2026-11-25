package gal.usc.etse.inbest.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/// Claves maestras (KEK) por versión, en Base64, y la versión con la que se cifran las wallets nuevas
@ConfigurationProperties("inbest.wallet")
public record WalletKeyProperties(Map<Short, String> masterKeys, short currentKekVersion) {
}
