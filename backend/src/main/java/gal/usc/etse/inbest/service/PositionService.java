package gal.usc.etse.inbest.service;

import gal.usc.etse.inbest.model.entity.Position;
import gal.usc.etse.inbest.repository.PositionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PositionService {

    private final PositionRepository repository;

    public PositionService(PositionRepository repository) {
        this.repository = repository;
    }

    //Posiciones de todas las wallets del usuario
    public List<Position> findByUserId(Long userId) {
        return repository.findByWallet_User_Id(userId);
    }
}
