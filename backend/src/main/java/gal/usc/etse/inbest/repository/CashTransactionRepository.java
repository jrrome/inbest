package gal.usc.etse.inbest.repository;

import gal.usc.etse.inbest.model.entity.CashTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashTransactionRepository extends JpaRepository<CashTransaction, Long> {
}
