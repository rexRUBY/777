package org.example.monitoring.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class UserMetricsService {

    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Double> userYields = new ConcurrentHashMap<>();

    public UserMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void updateYield(String userEmail, String cryptoSymbol, double newYield) {
        String metricKey = userEmail + ":" + cryptoSymbol;
        userYields.put(metricKey, newYield);

        Gauge.builder("user_yield_rate", userYields, map -> map.getOrDefault(metricKey, 0.0))
                .description("Yield rate of users")
                .tag("userEmail", userEmail)
                .tag("cryptoSymbol", cryptoSymbol)
                .register(meterRegistry);
    }
}
