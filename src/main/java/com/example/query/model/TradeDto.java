package com.example.query.model;

import lombok.Data;

@Data
public class TradeDto extends BaseDto {
    private Long tradeId;
    private String price;
    private String quantity;
    private Long tradeTime;
    private Boolean isBuyerMarketMaker;
    private Boolean ignore;
}
