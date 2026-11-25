package gal.usc.etse.inbest.repository;

import gal.usc.etse.inbest.model.entity.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    //Un nombre derivado necesita un parámetro por condición, por eso el id del usuario se pasa dos veces
    Page<Trade> findByBuyOrder_User_IdOrSellOrder_User_Id(Long buyerId, Long sellerId, Pageable pageable);
}
