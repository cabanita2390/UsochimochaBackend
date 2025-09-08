package com.app.usochicamochabackend.order.infrastructure.repository;

import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> getAllByInspectionId(Long inspectionId);
}