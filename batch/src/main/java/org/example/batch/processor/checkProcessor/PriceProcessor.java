package org.example.batch.processor.checkProcessor;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class PriceProcessor implements ItemProcessor<Subscriptions, Subscriptions>, StepExecutionListener {

    private final SubscriptionBillingService subscriptionBillingService;

    private ExecutionContext executionContext;


    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.executionContext = stepExecution.getExecutionContext();
    }

    @Override
    public Subscriptions process(Subscriptions subscriptions) throws Exception {
        Long price = (Long) executionContext.get("price");

        String Key = subscriptions.getId() + "_" + subscriptions.getCrypto().getSymbol();

        if (!executionContext.containsKey(Key)) {
            subscriptionBillingService.priceCheck(subscriptions, subscriptions.getCrypto().getSymbol(), price);
            executionContext.put(Key, true);
        }

        return subscriptions;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }
}
