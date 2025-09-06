package com.example.query.service;

import com.example.query.entity.EventTypeEntity;
import com.example.query.entity.SymbolEntity;
import com.example.query.entity.TickerEntity;
import com.example.query.exception.TickerNotFoundException;
import com.example.query.mapper.PayloadMapper;
import com.example.query.model.TickerDto;
import com.example.query.repository.EventTypeRepository;
import com.example.query.repository.SymbolRepository;
import com.example.query.repository.TickerRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TickerService {
    private final TickerRepository tickerRepository;
    private final SymbolService symbolService;
    private final EventTypeService eventTypeService;
    private final PayloadMapper mapper;

    public TickerDto saveTicker(TickerDto dto) {
        SymbolEntity symbol = symbolService.findSymbolByName(dto.getSymbol());
        EventTypeEntity eventType = eventTypeService.findEventType(dto.getEventType());

        TickerEntity ticker = mapper.toTickerEntity(dto);
        ticker.setSymbol(symbol);
        ticker.setEventType(eventType);

        ticker = tickerRepository.save(ticker);
        return mapper.toTickerDto(ticker);
    }

    public TickerDto getTickerById(Long tickerId) {
        TickerEntity ticker = tickerRepository.findById(tickerId)
                .orElseThrow(() -> new TickerNotFoundException("Ticker with id " + tickerId + " not found"));
        return mapper.toTickerDto(ticker);
    }

    public Page<TickerDto> getTickersBySymbol(String symbolName, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        SymbolEntity symbol = symbolService.findSymbolByName(symbolName);

        Page<TickerEntity> tickers = tickerRepository.findBySymbol(symbol, PageRequest.of(page, size, sort));
        return tickers.map(mapper::toTickerDto);
    }

    @Transactional
    public void deleteById(Long tickerId) {
        if (!tickerRepository.existsById(tickerId)) {
            throw new TickerNotFoundException("Ticker with id " + tickerId + " not found");
        }
        tickerRepository.deleteById(tickerId);
    }

    public TickerDto updateTicker(TickerDto tickerDto) {
        return tickerRepository.findById(tickerDto.getTickerId()).map(
                existingTicker -> {
                    SymbolEntity symbol = symbolService.findSymbolByName(tickerDto.getSymbol());
                    EventTypeEntity eventType = eventTypeService.findEventType(tickerDto.getEventType());

                    TickerEntity updatedEntity = mapper.toTickerEntity(tickerDto);
                    updatedEntity.setId(existingTicker.getId());
                    updatedEntity.setSymbol(symbol);
                    updatedEntity.setEventType(eventType);

                    TickerEntity saved = tickerRepository.save(updatedEntity);
                    return mapper.toTickerDto(saved);
                }).orElseThrow(() ->
                new TickerNotFoundException("Ticker with id " + tickerDto.getTickerId() + " not found")
        );
    }

    public Page<TickerDto> getAllTickers(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<TickerEntity> tickers = tickerRepository.findAll(PageRequest.of(page, size, sort));
        return tickers.map(mapper::toTickerDto);
    }

    public TickerDto getLatestTickerBySymbol(String symbol) {
        SymbolEntity symbolEntity = symbolService.findSymbolByName(symbol);

        TickerEntity ticker = tickerRepository.findFirstBySymbolOrderByCreatedAtDesc(symbolEntity)
                .orElseThrow(() -> new TickerNotFoundException("No ticker found for symbol " + symbol));
        return mapper.toTickerDto(ticker);
    }

}
