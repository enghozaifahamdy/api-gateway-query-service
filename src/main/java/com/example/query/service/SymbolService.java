package com.example.query.service;

import com.example.query.entity.SymbolEntity;
import com.example.query.repository.SymbolRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SymbolService {

    private final SymbolRepository symbolRepository;

    public SymbolEntity findSymbolByName(String symbol) {
        return symbolRepository.findByName(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Invalid symbol: " + symbol));
    }
}
