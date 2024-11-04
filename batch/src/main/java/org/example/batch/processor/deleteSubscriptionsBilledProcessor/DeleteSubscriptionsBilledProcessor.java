package org.example.batch.processor.deleteSubscriptionsBilledProcessor;

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
public class DeleteSubscriptionsBilledProcessor implements ItemProcessor<Subscriptions, Subscriptions >, StepExecutionListener {

    @Override
    public Subscriptions  process(Subscriptions subscriptions) throws Exception {
        log.info("process start subscriptiopn billing");

        if(subscriptions.getSubscribe().equals(Subscribe.ON)){
            throw new IllegalStateException("it is not billed");
        }

        return subscriptions ;
    }
}
