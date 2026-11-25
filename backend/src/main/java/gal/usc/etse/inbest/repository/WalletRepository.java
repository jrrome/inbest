package gal.usc.etse.inbest.repository;

import gal.usc.etse.inbest.model.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);

    Optional<Wallet> findByChainIdAndAddress(Long chainId, String address);
}
