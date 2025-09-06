package com.example.query.service;

import com.example.query.entity.EventTypeEntity;
import com.example.query.entity.SymbolEntity;
import com.example.query.entity.TradeEntity;
import com.example.query.exception.TradeNotFoundException;
import com.example.query.mapper.PayloadMapper;
import com.example.query.model.TradeDto;
import com.example.query.repository.EventTypeRepository;
import com.example.query.repository.SymbolRepository;
import com.example.query.repository.TradeRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TradeService {
    private final TradeRepository tradeRepository;
    private final SymbolService symbolService;
    private final EventTypeService eventTypeService;
    private final PayloadMapper mapper;

    public TradeDto saveTrade(TradeDto dto) {
        TradeEntity trade = mapper.toTradeEntity(dto);

        SymbolEntity symbolEntity = symbolService.findSymbolByName(dto.getSymbol());
        trade.setSymbol(symbolEntity);

        EventTypeEntity eventTypeEntity = eventTypeService.findEventType(dto.getEventType());
        trade.setEventType(eventTypeEntity);

        trade = tradeRepository.save(trade);
        return mapper.toTradeDto(trade);
    }

    public TradeDto getTradeByTradeId(Long tradeId) {
        TradeEntity trade = tradeRepository.findByTradeId(tradeId)
                .orElseThrow(() -> new TradeNotFoundException("Trade with id " + tradeId + " not found"));
        return mapper.toTradeDto(trade);
    }

    public Page<TradeDto> getTradesBySymbol(String symbol, int page, int size, String sortBy, String sortDirection) {
        SymbolEntity symbolEntity = symbolService.findSymbolByName(symbol);

        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Page<TradeEntity> trades = tradeRepository.findBySymbol(
                symbolEntity, PageRequest.of(page, size, sort));

        return trades.map(mapper::toTradeDto);
    }

    @Transactional
    public void deleteByTradeId(Long tradeId) {
        if (!tradeRepository.existsByTradeId(tradeId)) {
            throw new TradeNotFoundException("Trade with id " + tradeId + " not found");
        }
        tradeRepository.deleteByTradeId(tradeId);
    }

    public TradeDto updateTrade(TradeDto tradeDto) {
        return tradeRepository.findByTradeId(tradeDto.getTradeId())
                .map(existingTrade -> {
                    TradeEntity updatedEntity = mapper.toTradeEntity(tradeDto);
                    updatedEntity.setId(existingTrade.getId());

                    SymbolEntity symbolEntity = symbolService.findSymbolByName(tradeDto.getSymbol());
                    updatedEntity.setSymbol(symbolEntity);

                    EventTypeEntity eventTypeEntity = eventTypeService.findEventType(tradeDto.getEventType());
                    updatedEntity.setEventType(eventTypeEntity);

                    TradeEntity saved = tradeRepository.save(updatedEntity);
                    return mapper.toTradeDto(saved);
                })
                .orElseThrow(() -> new TradeNotFoundException("Trade with id " + tradeDto.getTradeId() + " not found"));
    }

    public Page<TradeDto> getAllTrades(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<TradeEntity> trades = tradeRepository.findAll(PageRequest.of(page, size, sort));
        return trades.map(mapper::toTradeDto);
    }

    public TradeDto getLatestTradeBySymbol(String symbol) {
        SymbolEntity symbolEntity = symbolService.findSymbolByName(symbol);

        TradeEntity trade = tradeRepository.findFirstBySymbolOrderByCreatedAtDesc(symbolEntity)
                .orElseThrow(() -> new TradeNotFoundException("No trades found for symbol " + symbol));
        return mapper.toTradeDto(trade);
    }

}
