package org.example.monitoring.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomPrometheusConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> configureMetrics() {
        return registry -> registry.config().commonTags("application", "YourAppName");
    }
}