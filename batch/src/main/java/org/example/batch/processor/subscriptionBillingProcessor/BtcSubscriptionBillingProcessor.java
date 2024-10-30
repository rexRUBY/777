package org.example.batch.processor.subscriptionBillingProcessor;

import lombok.extern.slf4j.Slf4j;
import org.example.batch.service.SubscriptionBillingService;
import org.example.common.user.entity.User;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@StepScope
public class BtcSubscriptionBillingProcessor implements ItemProcessor<User, User>, StepExecutionListener {

    private final SubscriptionBillingService subscriptionBillingService;
    // StepExecution에서 사용할 ExecutionContext
    private ExecutionContext executionContext;

    public BtcSubscriptionBillingProcessor(SubscriptionBillingService subscriptionBillingService) {
        this.subscriptionBillingService = subscriptionBillingService;
    }


    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.executionContext = stepExecution.getExecutionContext();
    }

    @Override
    public User process(User user) throws Exception {
        log.info("process start subscriptiopn billing");
        LocalDateTime time = LocalDateTime.now();
        String userEmail = user.getEmail();
        log.info(userEmail);
        // eth 랭킹 처리
        String btcBillKey = userEmail + "_billed_btc" + time;
        if (executionContext.containsKey(btcBillKey)) {
            throw new IllegalStateException("duplicated");
        }
//      rank.update()
        subscriptionBillingService.billCheck(user, "BTC");
        executionContext.put(btcBillKey, true); // 중복 체크용
        return user;
    }


}
