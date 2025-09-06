package com.example.query.model;

import lombok.Data;

@Data
public class BaseDto {
    String eventType;
    Long eventTimestamp;
    String symbol;
}
