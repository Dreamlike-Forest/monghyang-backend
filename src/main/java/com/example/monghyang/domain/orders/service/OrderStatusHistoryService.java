package com.example.monghyang.domain.orders.service;

import com.example.monghyang.domain.orders.entity.OrderStatusHistory;
import com.example.monghyang.domain.orders.entity.Orders;
import com.example.monghyang.domain.orders.entity.PaymentStatus;
import com.example.monghyang.domain.orders.repository.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderStatusHistoryService {
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Transactional
    public void setPaymentStatusPending(Orders orders) {
        orderStatusHistoryRepository.save(OrderStatusHistory.orderToStatusReasonCodeOf(orders, PaymentStatus.PENDING, "pending"));
    }

    @Transactional
    public void updatePaymentStatusPaid(Orders orders) {
        orders.setPaid();
        orderStatusHistoryRepository.save(OrderStatusHistory.orderToStatusReasonCodeOf(orders, PaymentStatus.PAID, "paid"));
    }

    @Transactional
    public void updatePaymentStatusFailed(Orders orders) {
        orders.setFailed();
        orderStatusHistoryRepository.save(OrderStatusHistory.orderToStatusReasonCodeOf(orders, PaymentStatus.FAILED, "failed"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePaymentStatusCanceled(Orders orders) {
        orders.setCanceled();
        orderStatusHistoryRepository.save(OrderStatusHistory.orderToStatusReasonCodeOf(orders, PaymentStatus.CANCELED, "canceled"));
    }
}
