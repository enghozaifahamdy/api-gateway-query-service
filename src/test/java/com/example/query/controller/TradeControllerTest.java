package com.example.query.controller;

import com.example.query.entity.UserEntity;
import com.example.query.model.TradeDto;
import com.example.query.repository.UserRepository;
import com.example.query.service.TradeService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(TradeController.class)
class TradeControllerTest {
    private static final String VALID_API_KEY = "EQmdzY5Cvvg1QZuwyR1t9AVVuqZj5YD5d7pv8nrh";
    private static final String INVALID_API_KEY = "12345";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradeService tradeService;


    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private TradeDto tradeDto;
    private UserEntity mockUser;
    @BeforeEach
    void setUp() {
        tradeDto = new TradeDto();
        tradeDto.setTradeId(12345L);
        tradeDto.setSymbol("BTCUSDT");
        tradeDto.setEventType("trade");
        tradeDto.setPrice("50000.00");
        tradeDto.setQuantity("0.001");
        tradeDto.setTradeTime(System.currentTimeMillis());
        tradeDto.setIsBuyerMarketMaker(true);
        tradeDto.setEventTimestamp(System.currentTimeMillis());

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
    void shouldGetTradeByTradeId() throws Exception {
        // Given
        Long tradeId = 12345L;
        when(tradeService.getTradeByTradeId(tradeId)).thenReturn(tradeDto);

        // When & Then
        mockMvc.perform(get("/internal/trade/{tradeId}", tradeId)
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tradeId", is(12345)))
                .andExpect(jsonPath("$.symbol", is("BTCUSDT")))
                .andExpect(jsonPath("$.eventType", is("trade")))
                .andExpect(jsonPath("$.price", is("50000.00")))
                .andExpect(jsonPath("$.quantity", is("0.001")))
                .andExpect(jsonPath("$.isBuyerMarketMaker", is(true)));

        verify(tradeService).getTradeByTradeId(tradeId);
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);

    }

    @Test
    @WithMockUser
    void shouldSaveTrade() throws Exception {
        // Given
        TradeDto inputDto = new TradeDto();
        inputDto.setSymbol("ETHUSDT");
        inputDto.setEventType("trade");
        inputDto.setPrice("3000.00");
        inputDto.setQuantity("0.5");

        TradeDto savedDto = new TradeDto();
        savedDto.setTradeId(67890L);
        savedDto.setSymbol("ETHUSDT");
        savedDto.setEventType("trade");
        savedDto.setPrice("3000.00");
        savedDto.setQuantity("0.5");

        when(tradeService.saveTrade(any(TradeDto.class))).thenReturn(savedDto);

        // When & Then
        mockMvc.perform(post("/internal/trade")
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tradeId", is(67890)))
                .andExpect(jsonPath("$.symbol", is("ETHUSDT")))
                .andExpect(jsonPath("$.price", is("3000.00")))
                .andExpect(jsonPath("$.quantity", is("0.5")));

        verify(tradeService).saveTrade(any(TradeDto.class));
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);

    }

    @Test
    @WithMockUser
    void shouldDeleteTrade() throws Exception {
        // Given
        Long tradeId = 12345L;
        doNothing().when(tradeService).deleteByTradeId(tradeId);

        // When & Then
        mockMvc.perform(delete("/internal/trade/{tradeId}", tradeId)
                        .header("X-API-KEY", VALID_API_KEY)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(tradeService).deleteByTradeId(tradeId);
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);

    }

    @Test
    @WithMockUser
    void shouldUpdateTrade() throws Exception {
        // Given
        TradeDto updateDto = new TradeDto();
        updateDto.setTradeId(12345L);
        updateDto.setSymbol("BTCUSDT");
        updateDto.setPrice("51000.00");
        updateDto.setQuantity("0.002");

        when(tradeService.updateTrade(any(TradeDto.class))).thenReturn(updateDto);

        // When & Then
        mockMvc.perform(put("/internal/trade")
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tradeId", is(12345)))
                .andExpect(jsonPath("$.price", is("51000.00")))
                .andExpect(jsonPath("$.quantity", is("0.002")));

        verify(tradeService).updateTrade(any(TradeDto.class));
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);

    }

    @Test
    @WithMockUser
    void shouldGetAllTradesWithDefaultPagination() throws Exception {
        // Given
        List<TradeDto> trades = Arrays.asList(tradeDto);
        PageImpl<TradeDto> page = new PageImpl<>(trades, PageRequest.of(0, 20), 1);

        when(tradeService.getAllTrades(0, 20, "tradeId", "asc")).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/internal/trade")
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].tradeId", is(12345)))
                .andExpect(jsonPath("$.content[0].symbol", is("BTCUSDT")))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.size", is(20)))
                .andExpect(jsonPath("$.number", is(0)));

        verify(tradeService).getAllTrades(0, 20, "tradeId", "asc");
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);

    }

    @Test
    @WithMockUser
    void shouldGetAllTradesWithCustomPagination() throws Exception {
        // Given
        List<TradeDto> trades = Arrays.asList(tradeDto);
        PageImpl<TradeDto> page = new PageImpl<>(trades, PageRequest.of(2, 5), 25);

        when(tradeService.getAllTrades(2, 5, "price", "desc")).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/internal/trade")
                        .header("X-API-KEY", VALID_API_KEY)
                        .param("page", "2")
                        .param("size", "5")
                        .param("sortBy", "price")
                        .param("sortDirection", "desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(25)))
                .andExpect(jsonPath("$.size", is(5)))
                .andExpect(jsonPath("$.number", is(2)));

        verify(tradeService).getAllTrades(2, 5, "price", "desc");
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);

    }

    @Test
    @WithMockUser
    void shouldGetLatestTradeBySymbol() throws Exception {
        // Given
        String symbol = "BTCUSDT";
        when(tradeService.getLatestTradeBySymbol(symbol)).thenReturn(tradeDto);

        // When & Then
        mockMvc.perform(get("/internal/trade/symbol/{symbol}/latest", symbol)
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.symbol", is("BTCUSDT")))
                .andExpect(jsonPath("$.price", is("50000.00")));

        verify(tradeService).getLatestTradeBySymbol(symbol);
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);

    }

    @Test
    @WithMockUser
    void shouldGetTradesBySymbol() throws Exception {
        // Given
        String symbol = "BTCUSDT";
        List<TradeDto> trades = Arrays.asList(tradeDto);
        PageImpl<TradeDto> page = new PageImpl<>(trades, PageRequest.of(0, 20), 1);

        when(tradeService.getTradesBySymbol(symbol, 0, 20, "tradeId", "asc")).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/internal/trade/symbol/{symbol}", symbol)
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].symbol", is("BTCUSDT")))
                .andExpect(jsonPath("$.content[0].tradeId", is(12345)));

        verify(tradeService).getTradesBySymbol(symbol, 0, 20, "tradeId", "asc");
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);

    }

    @Test
    @WithMockUser
    void shouldGetTradesBySymbolWithCustomPagination() throws Exception {
        // Given
        String symbol = "ETHUSDT";
        List<TradeDto> trades = Arrays.asList(tradeDto);
        PageImpl<TradeDto> page = new PageImpl<>(trades, PageRequest.of(1, 10), 15);

        when(tradeService.getTradesBySymbol(symbol, 1, 10, "quantity", "desc")).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/internal/trade/symbol/{symbol}", symbol)
                        .header("X-API-KEY", VALID_API_KEY)
                        .param("page", "1")
                        .param("size", "10")
                        .param("sortBy", "quantity")
                        .param("sortDirection", "desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(15)))
                .andExpect(jsonPath("$.size", is(10)));

        verify(tradeService).getTradesBySymbol(symbol, 1, 10, "quantity", "desc");
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);

    }

    @Test
    void shouldReturnUnauthorizedWhenNoApiKey() throws Exception {
        // When & Then
        mockMvc.perform(get("/internal/trade/12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(tradeService, never()).getTradeByTradeId(any());
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);

    }

    @Test
    @WithMockUser
    void shouldHandleInvalidTradeIdFormat() throws Exception {
        // When & Then
        mockMvc.perform(get("/internal/trade/{tradeId}", "invalid-id")
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(tradeService, never()).getTradeByTradeId(any());
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);

    }

    @Test
    @WithMockUser
    void shouldHandleMissingRequestBodyForUpdate() throws Exception {
        // When & Then
        mockMvc.perform(put("/internal/trade")
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(tradeService, never()).updateTrade(any());
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);

    }

    @Test
    @WithMockUser
    void shouldHandleBuyerMarketMakerFalse() throws Exception {
        // Given
        TradeDto inputDto = new TradeDto();
        inputDto.setSymbol("ADAUSDT");
        inputDto.setEventType("trade");
        inputDto.setPrice("1.50");
        inputDto.setQuantity("100.0");
        inputDto.setIsBuyerMarketMaker(false);

        TradeDto savedDto = new TradeDto();
        savedDto.setTradeId(99999L);
        savedDto.setSymbol("ADAUSDT");
        savedDto.setIsBuyerMarketMaker(false);

        when(tradeService.saveTrade(any(TradeDto.class))).thenReturn(savedDto);

        // When & Then
        mockMvc.perform(post("/internal/trade")
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isBuyerMarketMaker", is(false)));

        verify(tradeService).saveTrade(any(TradeDto.class));
        verify(userRepository).findByApiKeyAndActive(VALID_API_KEY, true);

    }
}
