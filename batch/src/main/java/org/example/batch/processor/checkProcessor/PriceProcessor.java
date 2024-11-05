package org.example.batch.processor.checkProcessor;

import lombok.extern.slf4j.Slf4j;
import org.example.batch.service.SubscriptionBillingService;
import org.example.common.subscriptions.entity.Subscriptions;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
public class PriceProcessor implements ItemProcessor<Subscriptions, Subscriptions>, StepExecutionListener {

    private final SubscriptionBillingService subscriptionBillingService;

    private ExecutionContext executionContext;

    public PriceProcessor(SubscriptionBillingService subscriptionBillingService) {
        this.subscriptionBillingService = subscriptionBillingService;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.executionContext = stepExecution.getExecutionContext();
    }

    @Override
    public Subscriptions process(Subscriptions subscriptions) throws Exception {
        Long btcPrice = (Long) executionContext.get("btcPrice");
        Long ethPrice = (Long) executionContext.get("ethPrice");

        String btcKey = subscriptions.getId() + "_btc";
        // BTC 처리
        if (!executionContext.containsKey(btcKey)) {
            subscriptionBillingService.priceCheck(subscriptions, "BTC",btcPrice);
            executionContext.put(btcKey, true);
        }
        String ethKey = subscriptions.getId() + "_eth";
        // BTC 처리
        if (!executionContext.containsKey(ethKey)) {
            subscriptionBillingService.priceCheck(subscriptions, "ETH",ethPrice);
            executionContext.put(ethKey, true);
        }

        return subscriptions;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }
}

