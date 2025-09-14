package com.app.usochicamochabackend.actions.infrastructure.repository;

import com.app.usochicamochabackend.actions.infrastructure.entity.ActionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionRepository extends JpaRepository<ActionEntity, Long> {
    Page<ActionEntity> findByUserId(Long userId, Pageable pageable);
}