package gal.usc.etse.inbest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;

// Configuration le dice a Spring que aquí se define como se crean algunos Beans
// para que él posteriormente los pueda crear e inyectar a las clases que los pidan
@Configuration
public class BlockchainConfig {
    // Con esto se declara la función como receta. destroyMethod indica que se ejecute
    // web3j.shutdown() al terminar
    @Bean(destroyMethod = "shutdown")
    Web3j web3j(@Value("${inbest.blockchain.rpc-url}") String rpcUrl) {
        return Web3j.build(new HttpService(rpcUrl));
    }

    @Bean
    Credentials adminCredentials(@Value("${inbest.blockchain.admin-private-key}") String adminPrivateKey) {
        return Credentials.create(adminPrivateKey);
    }

    @Bean
    TransactionManager transactionManager(
            Web3j web3j,
            Credentials adminCredentials,
            @Value("${inbest.blockchain.chain-id}") long chainId)
    {
        return new RawTransactionManager(
                web3j,
                adminCredentials,
                chainId,
                new PollingTransactionReceiptProcessor(web3j, 500, 20));
    }

    @Bean
    ContractGasProvider contractGasProvider() {
        return new DefaultGasProvider();
    }

}
