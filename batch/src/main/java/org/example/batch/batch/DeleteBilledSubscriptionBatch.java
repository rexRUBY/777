package org.example.batch.batch;

import lombok.RequiredArgsConstructor;
import org.example.batch.partitioner.ColumnRangePartitioner;
import org.example.batch.processor.deleteSubscriptionsBilledProcessor.DeleteSubscriptionsBilledProcessor;
import org.example.common.subscriptions.entity.Billing;
import org.example.common.subscriptions.entity.Subscriptions;
import org.example.common.subscriptions.repository.BillingRepository;
import org.example.common.subscriptions.repository.SubscriptionsRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DeleteBilledSubscriptionBatch {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final DeleteSubscriptionsBilledProcessor deleteSubscriptionsBilledProcessor;
    private final SubscriptionsRepository subscriptionsRepository;
    private final BillingRepository billingRepository;

    //구독정보를 삭제하고 Billing을 추가하기위한 job
    @Bean
    public Job thirdJob() {
        return new JobBuilder("thirdJob", jobRepository)
                .start(checkDeleteStep())
                .build();
    }

    @Bean
    public Step checkDeleteStep() {
        return new StepBuilder("checkDeleteStep", jobRepository)
                .partitioner("checkDeleteStep", deletePartitioner()) // 파티셔너 적용
                .step(firstDeleteStep())
                .gridSize(10) // 파티션 수
                .build();
    }

    // 구독 데이터를 읽고 처리 후 Billing에 저장하고 Subscriptions에서 삭제하는 단계를 정의, 청크 크기는 10으로 설정
    @Bean
    public Step firstDeleteStep() {
        return new StepBuilder("firstDeleteStep", jobRepository)
                .<Subscriptions, Subscriptions>chunk(5000, platformTransactionManager)
                .reader(beforeDeleteReader()) // 구독 데이터를 읽어옴
                .processor(deleteSubscriptionsBilledProcessor) // 구독 데이터를 처리
                .writer(afterDeleteWriter()) // 처리된 구독 데이터를 Billing에 저장하고 삭제
                .build();
    }

    // 구독 데이터를 읽기 위한 설정을 정의
    @Bean
    public RepositoryItemReader<Subscriptions> beforeDeleteReader() {
        return new RepositoryItemReaderBuilder<Subscriptions>()
                .name("beforeDeleteReader") // 리더의 이름 설정
                .pageSize(5000) // 한 번에 10개의 구독 데이터를 읽어옴
                .methodName("findAllBySubscribe") // subscriptionsRepository의 메서드 이름
                .repository(subscriptionsRepository)
                .sorts(Map.of("id", Sort.Direction.ASC)) // subscriptions 데이터를 ID 기준으로 오름차순 정렬
                .build();
    }

    @Bean
    public ItemWriter<Subscriptions> afterDeleteWriter() {
        return subscriptions -> {
            for (Subscriptions subscription : subscriptions) {
                // Billing 객체 생성
                Billing billing = Billing.of(subscription); // Billing 생성 메서드 호출
                billingRepository.save(billing); // BillingRepository에 저장

                // SubscriptionsRepository에서 구독 데이터 삭제
                subscriptionsRepository.delete(subscription);
            }
        };
    }

    @Bean
    public ColumnRangePartitioner deletePartitioner() {
        Long minId = subscriptionsRepository.findMinId(); // 최소 ID 조회
        Long maxId = subscriptionsRepository.findMaxId(); // 최대 ID 조회
        return new ColumnRangePartitioner("id", minId, maxId, 10);
    }
}
