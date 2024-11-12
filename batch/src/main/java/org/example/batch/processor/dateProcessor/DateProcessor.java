package org.example.batch.processor.dateProcessor;

import lombok.extern.slf4j.Slf4j;
import org.example.batch.service.SubscriptionBillingService;
import org.example.common.subscriptions.entity.Subscriptions;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@StepScope
public class DateProcessor implements ItemProcessor<Subscriptions, Subscriptions>, StepExecutionListener {

    private final SubscriptionBillingService subscriptionBillingService;
    // StepExecution에서 사용할 ExecutionContext
    private ExecutionContext executionContext;

    public DateProcessor(SubscriptionBillingService subscriptionBillingService) {
        this.subscriptionBillingService = subscriptionBillingService;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.executionContext = stepExecution.getExecutionContext();
    }

    @Override
    public Subscriptions process(Subscriptions subscriptions) throws Exception {
        log.info("process start subscriptiopn billing");
        LocalDate time = LocalDate.now();
        String id = String.valueOf(subscriptions.getId());
        Long price = (Long) executionContext.get("price");
        String cryptoSymbol = executionContext.getString("cryptoSymbol");

        log.info(id);
        //  랭킹 처리
        String btcBillKey = id + "_billed" + time;
        if (!executionContext.containsKey(btcBillKey)) {
            subscriptionBillingService.dateCheck(subscriptions, cryptoSymbol, price);
            executionContext.put(btcBillKey, true); // 중복 체크용
        }
        return subscriptions;
    }
}
