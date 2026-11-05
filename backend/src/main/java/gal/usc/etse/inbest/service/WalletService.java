package gal.usc.etse.inbest.service;

import gal.usc.etse.inbest.model.entity.Wallet;
import gal.usc.etse.inbest.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletService {

    private final WalletRepository repository;

    public WalletService(WalletRepository repository) {
        this.repository = repository;
    }

    public List<Wallet> findByUserId(Long userId) {
        return repository.findByUser_Id(userId);
    }
}
