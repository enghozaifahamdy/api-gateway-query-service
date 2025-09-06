package com.example.query.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import javax.persistence.JoinColumn;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade")
@Data
public class TradeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "event_type_id", nullable = false)
    private EventTypeEntity eventType;
    @Column(name = "event_timestamp")
    private Long eventTimestamp;
    @ManyToOne(optional = false)
    @JoinColumn(name = "symbol_id", nullable = false)
    private SymbolEntity symbol;
    @Column(name = "trade_id")
    private Long tradeId;
    private String price;
    private String quantity;
    @Column(name = "trade_time")
    private Long tradeTime;
    @Column(name = "is_buyer_market_maker")
    private Boolean isBuyerMarketMaker;
    @Column(name="created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
