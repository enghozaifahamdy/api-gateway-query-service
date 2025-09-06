package com.example.query.mapper;


import com.example.query.entity.EventTypeEntity;
import com.example.query.entity.SymbolEntity;
import com.example.query.entity.TickerEntity;
import com.example.query.entity.TradeEntity;
import com.example.query.model.TickerDto;
import com.example.query.model.TradeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface PayloadMapper {

    @Mapping(target = "eventType", ignore = true)
    @Mapping(target = "symbol", ignore = true)
    TradeEntity toTradeEntity(TradeDto dto);

    @Mapping(source = "eventType.type", target = "eventType")
    @Mapping(source = "symbol.name", target = "symbol")
    TradeDto toTradeDto(TradeEntity entity);

    @Mapping(target = "eventType", ignore = true)
    @Mapping(target = "symbol", ignore = true)
    TickerEntity toTickerEntity(TickerDto dto);

    @Mapping(source = "eventType.type", target = "eventType")
    @Mapping(source = "symbol.name", target = "symbol")
    @Mapping(source = "id", target = "tickerId")
    TickerDto toTickerDto(TickerEntity entity);

}
