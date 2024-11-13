package org.example.batch.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.batch.partitioner.ColumnRangePartitioner;
import org.example.batch.processor.dateProcessor.DateProcessor;
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

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CheckDateBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final SubscriptionsRepository subscriptionsRepository;
    private final DateProcessor dateProcessor;

    //구독한 날과 현재 날짜를 비교하여 한달이 되면 처리해주는 Job
    @Bean
    public Job checkDateJob() {
        return new JobBuilder("checkDateJob", jobRepository)
                .start(checkDateStep())
                .build();
    }

    @Bean
    public Step checkDateStep() {
        return new StepBuilder("checkDateStep", jobRepository)
                .partitioner("checkDateStep", datePartitioner())
                .step(firstCheckingDateStep())
                .gridSize(10)
                .build();
    }

    @Bean
    public Step firstCheckingDateStep() {
        return new StepBuilder("firstCheckingDateStep", jobRepository)
                .<Subscriptions, Subscriptions>chunk(5000, platformTransactionManager)
                .reader(beforeCheckDateReader(null, null))
                .processor(dateProcessor)
                .writer(afterCheckDateWriter())
                .taskExecutor(dateTaskExecutor())
                .listener(stepDateExecutionListener())
                .build();
    }
    //param으로 받아온 time과 cryptoSymbol 정보를 stepExecutionContext에서 꺼내서 파라미터로 사용
    @Bean
    @StepScope
    public RepositoryItemReader<Subscriptions> beforeCheckDateReader(
            @Value("#{stepExecutionContext['time']}") LocalDateTime time,
            @Value("#{stepExecutionContext['cryptoSymbol']}") String cryptoSymbol) {
        return new RepositoryItemReaderBuilder<Subscriptions>()
                .name("beforeReader")
                .pageSize(5000)
                .methodName("findTime") //subscriptionsRepository에 jpql로 설정한 메서드
                .repository(subscriptionsRepository)
                .arguments(time, cryptoSymbol) //파라미터설정
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public RepositoryItemWriter<Subscriptions> afterCheckDateWriter() {
        return new RepositoryItemWriterBuilder<Subscriptions>()
                .repository(subscriptionsRepository)
                .methodName("save")
                .build();
    }

    @Bean
    public ColumnRangePartitioner datePartitioner() { //파티셔닝 범위 설정을 위해 subscriptionsRepository의 처음과 끝 id의 값을 받아옴
        Long minId = subscriptionsRepository.findMinId();
        Long maxId = subscriptionsRepository.findMaxId();
        return new ColumnRangePartitioner("id", minId, maxId, 10);
    }

    //병렬처리를 위해 taskExecutor설정
    @Bean
    public TaskExecutor dateTaskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(10);
        return taskExecutor;
    }

    @Bean
    public StepExecutionListener stepDateExecutionListener() {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                log.info("Starting step: " + stepExecution.getStepName());

                LocalDateTime time = LocalDateTime.now().minusMonths(1);
                // 컨트롤러에서 전달받은 파라미터값을 ExecutionContext에 저장
                String cryptoSymbol = stepExecution.getJobExecution().getJobParameters().getString("cryptoSymbol");
                Long price = stepExecution.getJobExecution().getJobParameters().getLong("price");

                // ExecutionContext에 값을 저장
                stepExecution.getExecutionContext().put("cryptoSymbol", cryptoSymbol);
                stepExecution.getExecutionContext().put("price", price);
                stepExecution.getExecutionContext().put("time", time);
            }

            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                log.info("Step " + stepExecution.getStepName() + " completed with status: " + stepExecution.getExitStatus());
                return ExitStatus.COMPLETED;
            }
        };
    }
}

