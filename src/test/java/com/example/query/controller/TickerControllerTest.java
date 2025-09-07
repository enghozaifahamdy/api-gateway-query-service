package com.example.query.controller;

import com.example.query.entity.UserEntity;
import com.example.query.model.TickerDto;
import com.example.query.repository.UserRepository;
import com.example.query.service.TickerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TickerController.class)
@ActiveProfiles("test")
class TickerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TickerService tickerService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private TickerDto tickerDto;
    private UserEntity mockUser;
    private static final String VALID_API_KEY = "EQmdzY5Cvvg1QZuwyR1t9AVVuqZj5YD5d7pv8nrh";
    private static final String INVALID_API_KEY = "12345";

    @BeforeEach
    void setUp() {
        // Setup test ticker DTO
        tickerDto = new TickerDto();
        tickerDto.setTickerId(1L);
        tickerDto.setSymbol("BTCUSDT");
        tickerDto.setEventType("24hrTicker");
        tickerDto.setLastPrice("50000.00");
        tickerDto.setPriceChange("1000.00");
        tickerDto.setPriceChangePercent("2.00");
        tickerDto.setEventTimestamp(System.currentTimeMillis());

        // Setup mock user for API key validation
        mockUser = new UserEntity();
        mockUser.setId(1L);
        mockUser.setApiKey(VALID_API_KEY);
        mockUser.setActive(true);
        mockUser.setFirstName("Test");
        mockUser.setLastName("User");
        mockUser.setEmail("test@example.com");

        // Configure UserRepository to return valid user for the API key
        when(userRepository.findByApiKeyAndActive(VALID_API_KEY, true))
                .thenReturn(Optional.of(mockUser));

        // Configure UserRepository to return empty for invalid API key
        when(userRepository.findByApiKeyAndActive(INVALID_API_KEY, true))
                .thenReturn(Optional.empty());

        // Configure UserRepository to return empty for any other API key
        when(userRepository.findByApiKeyAndActive(argThat(key ->
                !VALID_API_KEY.equals(key) && !INVALID_API_KEY.equals(key)), eq(true)))
                .thenReturn(Optional.empty());
    }

    @Test
    @WithMockUser
    void shouldGetTickerById() throws Exception {
        // Given
        Long tickerId = 1L;
        when(tickerService.getTickerById(tickerId)).thenReturn(tickerDto);

        // When & Then
        mockMvc.perform(get("/internal/ticker/{tickerId}", tickerId)
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tickerId", is(1)))
                .andExpect(jsonPath("$.symbol", is("BTCUSDT")))
                .andExpect(jsonPath("$.eventType", is("24hrTicker")))
                .andExpect(jsonPath("$.lastPrice", is("50000.00")))
                .andExpect(jsonPath("$.priceChange", is("1000.00")));

        verify(tickerService).getTickerById(tickerId);
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);
    }

    @Test
    @WithMockUser
    void shouldSaveTicker() throws Exception {
        // Given
        TickerDto inputDto = new TickerDto();
        inputDto.setSymbol("ETHUSDT");
        inputDto.setEventType("24hrTicker");
        inputDto.setLastPrice("3000.00");

        TickerDto savedDto = new TickerDto();
        savedDto.setTickerId(2L);
        savedDto.setSymbol("ETHUSDT");
        savedDto.setEventType("24hrTicker");
        savedDto.setLastPrice("3000.00");

        when(tickerService.saveTicker(any(TickerDto.class))).thenReturn(savedDto);

        // When & Then
        mockMvc.perform(post("/internal/ticker")
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tickerId", is(2)))
                .andExpect(jsonPath("$.symbol", is("ETHUSDT")))
                .andExpect(jsonPath("$.lastPrice", is("3000.00")));

        verify(tickerService).saveTicker(any(TickerDto.class));
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);
    }

    @Test
    @WithMockUser
    void shouldDeleteTicker() throws Exception {
        // Given
        Long tickerId = 1L;
        doNothing().when(tickerService).deleteById(tickerId);

        // When & Then
        mockMvc.perform(delete("/internal/ticker/{tickerId}", tickerId)
                        .header("X-API-KEY", VALID_API_KEY)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(tickerService).deleteById(tickerId);
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);
    }

    @Test
    @WithMockUser
    void shouldUpdateTicker() throws Exception {
        // Given
        TickerDto updateDto = new TickerDto();
        updateDto.setTickerId(1L);
        updateDto.setSymbol("BTCUSDT");
        updateDto.setLastPrice("51000.00");

        when(tickerService.updateTicker(any(TickerDto.class))).thenReturn(updateDto);

        // When & Then
        mockMvc.perform(put("/internal/ticker")
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tickerId", is(1)))
                .andExpect(jsonPath("$.lastPrice", is("51000.00")));

        verify(tickerService).updateTicker(any(TickerDto.class));
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);
    }

    @Test
    @WithMockUser
    void shouldGetAllTickersWithDefaultPagination() throws Exception {
        // Given
        List<TickerDto> tickers = Arrays.asList(tickerDto);
        PageImpl<TickerDto> page = new PageImpl<>(tickers, PageRequest.of(0, 20), 1);

        when(tickerService.getAllTickers(0, 20, "id", "asc")).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/internal/ticker")
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].tickerId", is(1)))
                .andExpect(jsonPath("$.content[0].symbol", is("BTCUSDT")))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.size", is(20)))
                .andExpect(jsonPath("$.number", is(0)));

        verify(tickerService).getAllTickers(0, 20, "id", "asc");
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);
    }

    @Test
    @WithMockUser
    void shouldGetAllTickersWithCustomPagination() throws Exception {
        // Given
        List<TickerDto> tickers = Arrays.asList(tickerDto);
        PageImpl<TickerDto> page = new PageImpl<>(tickers, PageRequest.of(1, 10), 15);

        when(tickerService.getAllTickers(1, 10, "lastPrice", "desc")).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/internal/ticker")
                        .header("X-API-KEY", VALID_API_KEY)
                        .param("page", "1")
                        .param("size", "10")
                        .param("sortBy", "lastPrice")
                        .param("sortDirection", "desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(15)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.number", is(1)));

        verify(tickerService).getAllTickers(1, 10, "lastPrice", "desc");
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);
    }

    @Test
    @WithMockUser
    void shouldGetLatestTickerBySymbol() throws Exception {
        // Given
        String symbol = "BTCUSDT";
        when(tickerService.getLatestTickerBySymbol(symbol)).thenReturn(tickerDto);

        // When & Then
        mockMvc.perform(get("/internal/ticker/symbol/{symbol}/latest", symbol)
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.symbol", is("BTCUSDT")))
                .andExpect(jsonPath("$.lastPrice", is("50000.00")));

        verify(tickerService).getLatestTickerBySymbol(symbol);
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);
    }

    @Test
    @WithMockUser
    void shouldGetTickersBySymbol() throws Exception {
        // Given
        String symbol = "BTCUSDT";
        List<TickerDto> tickers = Arrays.asList(tickerDto);
        PageImpl<TickerDto> page = new PageImpl<>(tickers, PageRequest.of(0, 20), 1);

        when(tickerService.getTickersBySymbol(symbol, 0, 20, "id", "asc")).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/internal/ticker/symbol/{symbol}", symbol)
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].symbol", is("BTCUSDT")));

        verify(tickerService).getTickersBySymbol(symbol, 0, 20, "id", "asc");
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);
    }

    @Test
    @WithMockUser
    void shouldGetTickersBySymbolWithCustomPagination() throws Exception {
        // Given
        String symbol = "ETHUSDT";
        List<TickerDto> tickers = Arrays.asList(tickerDto);
        PageImpl<TickerDto> page = new PageImpl<>(tickers, PageRequest.of(1, 5), 10);

        when(tickerService.getTickersBySymbol(symbol, 1, 5, "lastPrice", "desc")).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/internal/ticker/symbol/{symbol}", symbol)
                        .header("X-API-KEY", VALID_API_KEY)
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortBy", "lastPrice")
                        .param("sortDirection", "desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
//                .andExpected(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(10)))
                .andExpect(jsonPath("$.size", is(5)))
                .andExpect(jsonPath("$.number", is(1)));

        verify(tickerService).getTickersBySymbol(symbol, 1, 5, "lastPrice", "desc");
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);
    }

    // Security Tests

    @Test
    void shouldReturnUnauthorizedForInvalidApiKey() throws Exception {
        // When & Then
        mockMvc.perform(get("/internal/ticker/1")
                        .header("X-API-KEY", INVALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userRepository).findByApiKeyAndActive(INVALID_API_KEY, true);
        verify(tickerService, never()).getTickerById(any());
    }

    @Test
    void shouldReturnUnauthorizedWhenNoApiKey() throws Exception {
        // When & Then
        mockMvc.perform(get("/internal/ticker/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userRepository, never()).findByApiKeyAndActive(any(), any());
        verify(tickerService, never()).getTickerById(any());
    }

    @Test
    void shouldReturnUnauthorizedForInactiveUser() throws Exception {
        // Given - Setup inactive user
        UserEntity inactiveUser = new UserEntity();
        inactiveUser.setApiKey("inactive-key");
        inactiveUser.setActive(false);

        when(userRepository.findByApiKeyAndActive("inactive-key", true))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/internal/ticker/1")
                        .header("X-API-KEY", "inactive-key")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userRepository).findByApiKeyAndActive("inactive-key", true);
        verify(tickerService, never()).getTickerById(any());
    }

    @Test
    void shouldReturnUnauthorizedForEmptyApiKey() throws Exception {
        // When & Then
        mockMvc.perform(get("/internal/ticker/1")
                        .header("X-API-KEY", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userRepository, never()).findByApiKeyAndActive(any(), any());
        verify(tickerService, never()).getTickerById(any());
    }

    @Test
    void shouldReturnUnauthorizedForNullApiKey() throws Exception {
        // When & Then
        mockMvc.perform(get("/internal/ticker/1")
                        .header("X-API-KEY", (String) null)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userRepository, never()).findByApiKeyAndActive(any(), any());
        verify(tickerService, never()).getTickerById(any());
    }

    // Error Handling Tests

    @Test
    @WithMockUser
    void shouldHandleInvalidTickerIdFormat() throws Exception {
        // When & Then
        mockMvc.perform(get("/internal/ticker/{tickerId}", "invalid-id")
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(tickerService, never()).getTickerById(any());
    }

    @Test
    @WithMockUser
    void shouldHandleEmptyRequestBodyForSave() throws Exception {
        // Given
        when(tickerService.saveTicker(any(TickerDto.class))).thenReturn(tickerDto);

        // When & Then
        mockMvc.perform(post("/internal/ticker")
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(tickerService).saveTicker(any(TickerDto.class));
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);
    }

    @Test
    @WithMockUser
    void shouldHandleMissingRequestBodyForUpdate() throws Exception {
        // When & Then
        mockMvc.perform(put("/internal/ticker")
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(tickerService, never()).updateTicker(any());
    }

    @Test
    @WithMockUser
    void shouldVerifyApiKeyValidationForAllEndpoints() throws Exception {
        // Test that all endpoints require valid API key
        String[] endpoints = {
                "/internal/ticker/1",
                "/internal/ticker",
                "/internal/ticker/symbol/BTC/latest",
                "/internal/ticker/symbol/BTC"
        };

        for (String endpoint : endpoints) {
            mockMvc.perform(get(endpoint)
                            .header("X-API-KEY", VALID_API_KEY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        // Verify that UserRepository was called for each endpoint
        verify(userRepository, times(endpoints.length))
                .findByApiKeyAndActive(VALID_API_KEY, true);
    }
}