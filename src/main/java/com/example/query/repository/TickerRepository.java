package com.example.query.repository;

import com.example.query.entity.SymbolEntity;
import com.example.query.entity.TickerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TickerRepository extends JpaRepository<TickerEntity, Long> {
    Page<TickerEntity> findBySymbol(SymbolEntity symbol, Pageable pageable);
    Optional<TickerEntity> findFirstBySymbolOrderByCreatedAtDesc(SymbolEntity symbol);
}
