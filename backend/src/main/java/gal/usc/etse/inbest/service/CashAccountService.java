package gal.usc.etse.inbest.service;

import gal.usc.etse.inbest.model.entity.CashAccount;
import gal.usc.etse.inbest.model.entity.CashTransaction;
import gal.usc.etse.inbest.model.entity.CashTransactionType;
import gal.usc.etse.inbest.model.entity.User;
import gal.usc.etse.inbest.repository.CashAccountRepository;
import gal.usc.etse.inbest.repository.CashTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CashAccountService {
    private final CashAccountRepository repository;
    private final CashTransactionRepository cashTransactionRepository;

    public CashAccountService(CashAccountRepository repository, CashTransactionRepository cashTransactionRepository) {
        this.repository = repository;
        this.cashTransactionRepository = cashTransactionRepository;
    }

    public Optional<CashAccount> findByUserId(Long userId) {
        return repository.findByUser_Id(userId);
    }

    //Devuelve la cuenta del usuario, si no tiene la crea con saldo 0
    public CashAccount getOrCreate(User user){
        return repository.findByUser_Id(user.getId()).orElseGet(() -> repository.save(new CashAccount(user)));
    }

    //Ingresa amount euros en la cuenta del usuario y registra el movimiento
    @Transactional
    public CashTransaction deposit(User user, BigDecimal amount) {
        //Lee la cuenta bloqueando su fila hasta el final de la transacción, si no existe la crea
        CashAccount account = repository.findWithLockByUser_Id(user.getId()).orElseGet(() -> repository.save(new CashAccount(user)));

        //BigDecimal es inmutable: add() devuelve un número nuevo, no modifica el original
        account.setAvailableCash(account.getAvailableCash().add(amount));

        //Un DEPOSIT nunca tiene trade asociado
        CashTransaction transaction = new CashTransaction(account, CashTransactionType.DEPOSIT, amount, null);
        return cashTransactionRepository.save(transaction);
    }

    //Movimientos de la cuenta del usuario, paginados
    public Page<CashTransaction> findTransactions(Long userId, Pageable pageable) {
        return cashTransactionRepository.findByCashAccount_User_Id(userId, pageable);
    }



}

