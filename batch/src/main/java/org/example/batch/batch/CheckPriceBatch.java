package org.example.batch.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.batch.partitioner.ColumnRangePartitioner;
import org.example.batch.processor.checkProcessor.PriceProcessor;
import org.example.common.common.log.LogExecution;
import org.example.common.subscriptions.entity.Subscriptions;
import org.example.common.subscriptions.repository.SubscriptionsRepository;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CheckPriceBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final SubscriptionsRepository subscriptionsRepository;
    private final PriceProcessor priceProcessor;

    // 구독당시 코인의 가격과 현재 가격을 비교하여 상승률이 5%가 되는 것을 체크하기 위한 Job
    @Bean
    @LogExecution
    public Job checkJob() {
        return new JobBuilder("checkJob", jobRepository)
                .start(checkStep())
                .build();
    }

    @Bean
    @LogExecution
    public Step checkStep() {
        return new StepBuilder("checkStep", jobRepository)
                .partitioner("checkStep", pricePartitioner())
                .step(firstCheckingStep())
                .gridSize(10)
                .build();
    }

    @Bean
    public Step firstCheckingStep() { // btc 정보 계산용
        return new StepBuilder("firstCheckingStep", jobRepository)
                .<Subscriptions, Subscriptions>chunk(5000, platformTransactionManager)
                .reader(beforeCheckReader(null, null)) //기본 파라미터값 null로 처리
                .processor(priceProcessor)
                .writer(afterCheckWriter())
                .taskExecutor(priceTaskExecutor())
                .listener(stepExecutionListener())
                .build();
    }

    // param으로 받아온 값들을 stepExecutionContext에서 꺼내서 파라미터로 사용
    @Bean
    @StepScope
    public RepositoryItemReader<Subscriptions> beforeCheckReader(
            @Value("#{stepExecutionContext['price']}") Long price,
            @Value("#{stepExecutionContext['cryptoSymbol']}") String cryptoSymbol) {
        return new RepositoryItemReaderBuilder<Subscriptions>()
                .name("beforeCheckReader")
                .pageSize(5000)
                .methodName("findPriceByCrypto") //subscriptionsRepository에 jpql로 메서드 정의
                .repository(subscriptionsRepository)
                .arguments(price, cryptoSymbol)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public RepositoryItemWriter<Subscriptions> afterCheckWriter() {
        return new RepositoryItemWriterBuilder<Subscriptions>()
                .repository(subscriptionsRepository)
                .methodName("save")
                .build();
    }

    @Bean
    public ColumnRangePartitioner pricePartitioner() { // partitioning 범위 설정을 위해 subscriptionsRepository의 처음과 끝 id값을 받아옴
        Long minId = subscriptionsRepository.findMinId();
        Long maxId = subscriptionsRepository.findMaxId();
        return new ColumnRangePartitioner("id", minId, maxId, 10);
    }

    @Bean
    public TaskExecutor priceTaskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(10);
        return taskExecutor;
    }

    @Bean
    public StepExecutionListener stepExecutionListener() {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                log.info("Starting step: " + stepExecution.getStepName());

                // 컨트롤러에서 전달받은 파라미터값을 ExecutionContext에 저장
                String cryptoSymbol = stepExecution.getJobExecution().getJobParameters().getString("cryptoSymbol");
                Long price = stepExecution.getJobExecution().getJobParameters().getLong("price");

                // ExecutionContext에 값을 저장
                stepExecution.getExecutionContext().put("cryptoSymbol", cryptoSymbol);
                stepExecution.getExecutionContext().put("price", price);
            }

            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                log.info("Step " + stepExecution.getStepName() + " completed with status: " + stepExecution.getExitStatus());
                return ExitStatus.COMPLETED;
            }
        };
    }
}


