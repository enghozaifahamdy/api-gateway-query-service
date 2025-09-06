package com.example.query.service;

import com.example.query.entity.EventTypeEntity;
import com.example.query.repository.EventTypeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EventTypeService {

    private final EventTypeRepository eventTypeRepository;

    EventTypeEntity findEventType(String eventType) {
        return eventTypeRepository.findByType(eventType)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event type: " + eventType));
    }
}
