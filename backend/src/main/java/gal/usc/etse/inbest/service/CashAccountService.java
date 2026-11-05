package gal.usc.etse.inbest.service;

import gal.usc.etse.inbest.model.entity.CashAccount;
import gal.usc.etse.inbest.repository.CashAccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CashAccountService {
    private final CashAccountRepository repository;

    public CashAccountService(CashAccountRepository repository) {
        this.repository = repository;
    }

    public Optional<CashAccount> findByUserId(Long userId) {
        return repository.findByUser_Id(userId);
    }
}

