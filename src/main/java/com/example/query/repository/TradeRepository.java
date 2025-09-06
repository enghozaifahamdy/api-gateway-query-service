package com.example.query.repository;

import com.example.query.entity.SymbolEntity;
import com.example.query.entity.TradeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, Long> {

    Page<TradeEntity> findBySymbol(SymbolEntity symbol, Pageable pageable);

    Optional<TradeEntity> findByTradeId(Long tradeId);

    void deleteByTradeId(Long tradeId);

    boolean existsByTradeId(Long tradeId);

    Optional<TradeEntity> findFirstBySymbolOrderByCreatedAtDesc(SymbolEntity symbol);
}
