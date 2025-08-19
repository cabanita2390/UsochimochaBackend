package com.app.usochicamochabackend.order.infrastructure.repository;

import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {}