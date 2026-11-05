package gal.usc.etse.inbest.repository;

import gal.usc.etse.inbest.model.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    List<Wallet> findByUser_Id(Long userId);
}
