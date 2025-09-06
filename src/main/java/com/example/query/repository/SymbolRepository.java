package com.example.query.repository;

import com.example.query.entity.SymbolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SymbolRepository extends JpaRepository<SymbolEntity, Long> {
    Optional<SymbolEntity> findByName(String symbol);
}
