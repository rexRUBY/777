package org.example.monitoring.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class CoinMetricsService {

    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Counter> transactionVolumeCounters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Double> coinPrices = new ConcurrentHashMap<>();

    public CoinMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordTransaction(String coinType, double amount) {
        Counter counter = transactionVolumeCounters.computeIfAbsent(coinType, key ->
                Counter.builder("coin_transaction_volume")
                        .description("Total volume of transactions for a specific coin")
                        .tag("coinType", coinType)
                        .register(meterRegistry)
        );
        counter.increment(amount);
    }

    public void updatePrice(String coinType, double price) {
        coinPrices.put(coinType, price);
        Gauge.builder("coin_price", coinPrices, map -> map.getOrDefault(coinType, 0.0))
                .description("Current price of a specific coin")
                .tag("coinType", coinType)
                .register(meterRegistry);
    }
}
