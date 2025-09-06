package com.example.query.controller;

import com.example.query.model.TradeDto;
import com.example.query.service.TradeService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/trade")
@AllArgsConstructor
public class TradeController {
    private TradeService tradeService;

    @GetMapping(value = "/{tradeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    TradeDto getTradeByTradeId(@PathVariable(value = "tradeId") Long tradeId) {
        return tradeService.getTradeByTradeId(tradeId);
    }

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    TradeDto saveTrade(@RequestBody TradeDto tradeDto) {
        return tradeService.saveTrade(tradeDto);
    }

    @DeleteMapping(value = "/{tradeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    void deleteTrade(@PathVariable(value = "tradeId") Long tradeId) {
        tradeService.deleteByTradeId(tradeId);
    }

    @PutMapping("")
    public TradeDto updateTrade(@RequestBody TradeDto tradeDto) {
        return tradeService.updateTrade(tradeDto);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<TradeDto> getAllTrades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "tradeId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        return tradeService.getAllTrades(page, size, sortBy, sortDirection);
    }

    @GetMapping(value = "/symbol/{symbol}/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    public TradeDto getLatestTradeBySymbol(@PathVariable String symbol) {
        return tradeService.getLatestTradeBySymbol(symbol);
    }

    @GetMapping(value = "/symbol/{symbol}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Page<TradeDto> getTradesBySymbol(@PathVariable(value = "symbol") String symbol,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size,
                                     @RequestParam(defaultValue = "tradeId") String sortBy,
                                     @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        return tradeService.getTradesBySymbol(symbol, page, size, sortBy, sortDirection);
    }
}
