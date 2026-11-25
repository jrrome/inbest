package gal.usc.etse.inbest.service;

import gal.usc.etse.inbest.model.entity.Order;
import gal.usc.etse.inbest.model.entity.OrderStatus;
import gal.usc.etse.inbest.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    //Si status es null devuelve todas las órdenes del usuario
    public Page<Order> findByUserId(Long userId, OrderStatus status, Pageable pageable) {
        if (status == null) {
            return repository.findByUser_Id(userId, pageable);
        }
        return repository.findByUser_IdAndStatus(userId, status, pageable);
    }
}
