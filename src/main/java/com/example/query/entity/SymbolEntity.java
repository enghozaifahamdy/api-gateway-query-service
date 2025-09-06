package com.example.query.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "symbol")
@Data
public class SymbolEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;
}
