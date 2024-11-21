package org.example.monitoring.controller;

import org.example.monitoring.service.CoinMetricsService;
import org.example.monitoring.service.UserMetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {
    private final CoinMetricsService coinMetricsService;
    private final UserMetricsService userMetricsService;

    public MetricsController(CoinMetricsService coinMetricsService, UserMetricsService userMetricsService) {
        this.coinMetricsService = coinMetricsService;
        this.userMetricsService = userMetricsService;
    }

    @PostMapping("/trade")
    public ResponseEntity<Void> recordTradeMetrics(@RequestParam String coinType,
                                                   @RequestParam double price,
                                                   @RequestParam double amount) {
        coinMetricsService.recordTransaction(coinType, amount);
        coinMetricsService.updatePrice(coinType, price);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/yield")
    public ResponseEntity<Void> recordYield(@RequestParam String userEmail,
                                            @RequestParam String cryptoSymbol,
                                            @RequestParam double yield) {
        userMetricsService.updateYield(userEmail, cryptoSymbol, yield);
        return ResponseEntity.ok().build();
    }
}
