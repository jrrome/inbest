package gal.usc.etse.inbest.repository;

import gal.usc.etse.inbest.model.entity.Order;
import gal.usc.etse.inbest.model.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUser_Id(Long userId, Pageable pageable);

    Page<Order> findByUser_IdAndStatus(Long userId, OrderStatus status, Pageable pageable);
}
