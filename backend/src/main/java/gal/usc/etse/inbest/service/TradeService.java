package gal.usc.etse.inbest.service;

import gal.usc.etse.inbest.model.entity.Trade;
import gal.usc.etse.inbest.repository.TradeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TradeService {

    private final TradeRepository repository;

    public TradeService(TradeRepository repository) {
        this.repository = repository;
    }

    //Trades en los que el usuario es comprador o vendedor
    public Page<Trade> findByUserId(Long userId, Pageable pageable) {
        return repository.findByBuyOrder_User_IdOrSellOrder_User_Id(userId, userId, pageable);
    }
}
