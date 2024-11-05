package org.example.batch.batch;

import lombok.RequiredArgsConstructor;
import org.example.batch.partitioner.ColumnRangePartitioner;
import org.example.batch.processor.subscriptionBillingProcessor.SubscriptionBillingProcessor;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SubscriptionBillingBatch{

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final UserRepository userRepository;
    private final SubscriptionBillingProcessor subscriptionBillingProcessor;

    @Bean
    public Job secondJob() {
        return new JobBuilder("secondJob", jobRepository)
                .start(billingStep())
                .build();
    }
    @Bean
    public Step billingStep() {
        return new StepBuilder("billingStep", jobRepository)
                .partitioner("billingStep", subPartitioner()) // 파티셔너 적용
                .step(firstBillingStep())
                .gridSize(10) // 파티션 수
                .build();
    }

    @Bean
    public Step firstBillingStep() {//btc 정보 계산용
        return new StepBuilder("firstBillingStep", jobRepository)
                .<User,User >chunk(5000, platformTransactionManager)
                .reader(beforeBillingReader()) // User 데이터를 읽어옴
                .processor(subscriptionBillingProcessor) // User 데이터를 Ranking으로 변환
                .writer(afterBillingWriter()) // 변환된 Ranking 데이터를 저장
                .build();
    }
    @Bean
    public RepositoryItemReader<User> beforeBillingReader() {
        return new RepositoryItemReaderBuilder<User>()
                .name("beforeBillingReader")
                .pageSize(5000) // 한 번에 10개의 User 데이터를 읽어옴
                .methodName("findAllJoinSubscriptsJoinWallet") // UserRepository의 메서드 이름
                .repository(userRepository)
                .sorts(Map.of("id", Sort.Direction.ASC)) // User 데이터를 ID 기준으로 오름차순 정렬
                .build();
    }
    @Bean
    public RepositoryItemWriter<User> afterBillingWriter() {
        return new RepositoryItemWriterBuilder<User>()
                .repository(userRepository)
                .methodName("save") // userRepository save 메서드를 사용하여 데이터 저장
                .build();
    }
    @Bean
    public ColumnRangePartitioner subPartitioner() {
        Long minId = userRepository.findMinId(); // 최소 ID 조회
        Long maxId = userRepository.findMaxId(); // 최대 ID 조회
        return new ColumnRangePartitioner("id", minId, maxId, 10);
    }

}
