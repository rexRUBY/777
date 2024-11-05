package org.example.batch.processor.dateProcessor;

import lombok.extern.slf4j.Slf4j;
import org.example.batch.service.SubscriptionBillingService;
import org.example.common.subscriptions.entity.Subscriptions;
import org.example.common.user.entity.User;
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
        Long ethPrice = (Long) executionContext.get("ethPrice");
        Long btcPrice = (Long) executionContext.get("btcPrice");

        log.info(id);
        // eth 랭킹 처리
        String btcBillKey = id + "_billed_btc" + time;
        if (!executionContext.containsKey(btcBillKey)) {
            subscriptionBillingService.dateCheck(subscriptions, "BTC",btcPrice);
            executionContext.put(btcBillKey, true); // 중복 체크용
        }

        // eth 랭킹 처리
        String ethBillKey = id + "_billed_eth" + time;
        if (!executionContext.containsKey(ethBillKey)) {
            subscriptionBillingService.dateCheck(subscriptions, "ETH",ethPrice);
            executionContext.put(ethBillKey, true); // 중복 체크용
        }
        return subscriptions;
    }

}
