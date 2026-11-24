package gal.usc.etse.inbest.repository;

import gal.usc.etse.inbest.model.entity.CashTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashTransactionRepository extends JpaRepository<CashTransaction, Long> {

    Page<CashTransaction> findByCashAccount_User_Id(Long userId, Pageable pageable);
}
