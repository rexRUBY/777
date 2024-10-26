package org.example.batch.processor.deleteSubscriptionsBilledProccessor;

import lombok.extern.slf4j.Slf4j;
import org.example.common.subscriptions.entity.Subscribe;
import org.example.common.subscriptions.entity.Subscriptions;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
@Slf4j
@Component
@StepScope
public class DeleteSubscriptionsBilledProccessor implements ItemProcessor<Subscriptions, Subscriptions >, StepExecutionListener {

    @Override
    public Subscriptions  process(Subscriptions subscriptions) throws Exception {
        log.info("process start subscriptiopn billing");

        if(!subscriptions.getSubscribe().equals(Subscribe.OFF)){
            throw new IllegalStateException("it is not billed");
        }

        return subscriptions ;
    }
}
