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
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SubscriptionBillingBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final UserRepository userRepository;
    private final SubscriptionBillingProcessor subscriptionBillingProcessor;

    //체크된 모든 subscriptions들을 정산해주는 Job
    @Bean
    public Job secondJob() {
        return new JobBuilder("secondJob", jobRepository)
                .start(billingStep())
                .build();
    }

    @Bean
    public Step billingStep() {
        return new StepBuilder("billingStep", jobRepository)
                .partitioner("billingStep", subPartitioner())
                .step(firstBillingStep())
                .gridSize(10)
                .build();
    }

    @Bean
    public Step firstBillingStep() {//btc 정보 계산용
        return new StepBuilder("firstBillingStep", jobRepository)
                .<User, User>chunk(5000, platformTransactionManager)
                .reader(beforeBillingReader())
                .processor(subscriptionBillingProcessor)
                .writer(afterBillingWriter())
                .taskExecutor(subTaskExecutor())
                .build();
    }

    @Bean
    public RepositoryItemReader<User> beforeBillingReader() {
        return new RepositoryItemReaderBuilder<User>()
                .name("beforeBillingReader")
                .pageSize(5000)
                .methodName("findAllJoinSubscriptsJoinWallet")
                .repository(userRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public RepositoryItemWriter<User> afterBillingWriter() {
        return new RepositoryItemWriterBuilder<User>()
                .repository(userRepository)
                .methodName("save")
                .build();
    }

    @Bean
    public TaskExecutor subTaskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(10);
        return taskExecutor;
    }

    @Bean
    public ColumnRangePartitioner subPartitioner() {
        Long minId = userRepository.findMinId(); // 최소 ID 조회
        Long maxId = userRepository.findMaxId(); // 최대 ID 조회
        return new ColumnRangePartitioner("id", minId, maxId, 10);
    }

}
