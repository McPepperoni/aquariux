package com.aquariux.trading.api;

import com.aquariux.trading.domain.TradeTransaction;
import com.aquariux.trading.repository.TradeTransactionRepository;
import com.aquariux.trading.trade.TradeRequest;
import com.aquariux.trading.trade.TradeResponse;
import com.aquariux.trading.trade.TradeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
class TradeController {

    private static final String DEMO_USERNAME = "demo";

    private final TradeService tradeService;
    private final TradeTransactionRepository tradeTransactionRepository;

    @PostMapping
    TradeResponse execute(@Valid @RequestBody TradeRequest request) {
        return tradeService.execute(request);
    }

    @GetMapping
    List<TradeResponse> history() {
        return tradeTransactionRepository.findByUserUsernameOrderByCreatedAtDesc(DEMO_USERNAME).stream()
                .map(this::toResponse)
                .toList();
    }

    private TradeResponse toResponse(TradeTransaction trade) {
        return TradeResponse.builder()
                .id(trade.getId())
                .pair(trade.getPair())
                .side(trade.getSide())
                .quantity(trade.getQuantity())
                .price(trade.getPrice())
                .quoteAmount(trade.getQuoteAmount())
                .baseCurrency(trade.getBaseCurrency())
                .quoteCurrency(trade.getQuoteCurrency())
                .createdAt(trade.getCreatedAt())
                .build();
    }
}
