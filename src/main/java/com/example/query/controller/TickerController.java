package com.example.query.controller;

import com.example.query.model.TickerDto;
import com.example.query.service.TickerService;
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
@RequestMapping("/internal/ticker")
@AllArgsConstructor
public class TickerController {

    private TickerService tickerService;

    @GetMapping(value = "/{tickerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    TickerDto getTickerById(@PathVariable(value = "tickerId") Long tickerId) {
        return tickerService.getTickerById(tickerId);
    }

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    TickerDto saveTicker(@RequestBody TickerDto tickerDto) {
        return tickerService.saveTicker(tickerDto);
    }

    @DeleteMapping(value = "/{tickerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    void deleteTicker(@PathVariable(value = "tickerId") Long tickerId) {
        tickerService.deleteById(tickerId);
    }

    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    TickerDto updateTicker(@RequestBody TickerDto tickerDto) {
        return tickerService.updateTicker(tickerDto);
    }

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Page<TickerDto> getAllTickers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        return tickerService.getAllTickers(page, size, sortBy, sortDirection);
    }

    @GetMapping(value = "/symbol/{symbol}/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    TickerDto getLatestTicker(@PathVariable("symbol") String symbol) {
        return tickerService.getLatestTickerBySymbol(symbol);
    }

    @GetMapping(value = "/symbol/{symbol}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Page<TickerDto> getTickersBySymbol(@PathVariable(value = "symbol") String symbol,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size,
                                       @RequestParam(defaultValue = "id") String sortBy,
                                       @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        return tickerService.getTickersBySymbol(symbol, page, size, sortBy, sortDirection);
    }
}
