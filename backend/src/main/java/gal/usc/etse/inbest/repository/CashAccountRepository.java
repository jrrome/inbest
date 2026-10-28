package gal.usc.etse.inbest.repository;

import gal.usc.etse.inbest.model.entity.CashAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CashAccountRepository extends JpaRepository<CashAccount, Long> {
    Optional<CashAccount> findByUserId(Long userId);
}
