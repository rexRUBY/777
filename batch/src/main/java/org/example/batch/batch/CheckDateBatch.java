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

    @Bean
    public Job checkDateJob() {
        return new JobBuilder("checkDateJob", jobRepository)
                .start(checkDateStep())
                .build();
    }

    @Bean
    public Step checkDateStep() {
        return new StepBuilder("checkDateStep", jobRepository)
                .partitioner("checkDateStep", datePartitioner()) // 파티셔너 적용
                .step(firstCheckingDateStep())
                .gridSize(10) // 파티션 수
                .build();
    }

    @Bean
    public Step firstCheckingDateStep() {//btc 정보 계산용
        return new StepBuilder("firstCheckingDateStep", jobRepository)
                .<Subscriptions, Subscriptions>chunk(5000, platformTransactionManager)
                .reader(beforeCheckDateReader(null, null)) // User 데이터를 읽어옴
                .processor(dateProcessor) // User 데이터를 Ranking으로 변환
                .writer(afterCheckDateWriter()) // 변환된 Ranking 데이터를 저장
                .taskExecutor(dateTaskExecutor()) // TaskExecutor 설정
                .listener(stepDateExecutionListener())
                .build();
    }

    @Bean
    @StepScope
    public RepositoryItemReader<Subscriptions> beforeCheckDateReader(
            @Value("#{stepExecutionContext['time']}") LocalDateTime time,
            @Value("#{stepExecutionContext['cryptoSymbol']}") String cryptoSymbol) {
        return new RepositoryItemReaderBuilder<Subscriptions>()
                .name("beforeReader")
                .pageSize(5000) // 한 번에 10개의 User 데이터를 읽어옴
                .methodName("findTime") //
                .repository(subscriptionsRepository)
                .arguments(time, cryptoSymbol)
                .sorts(Map.of("id", Sort.Direction.ASC)) // User 데이터를 ID 기준으로 오름차순 정렬
                .build();
    }

    @Bean
    public RepositoryItemWriter<Subscriptions> afterCheckDateWriter() {
        return new RepositoryItemWriterBuilder<Subscriptions>()
                .repository(subscriptionsRepository)
                .methodName("save") // userRepository save 메서드를 사용하여 데이터 저장
                .build();
    }

    @Bean
    public ColumnRangePartitioner datePartitioner() {
        Long minId = subscriptionsRepository.findMinId(); // 최소 ID 조회
        Long maxId = subscriptionsRepository.findMaxId(); // 최대 ID 조회
        return new ColumnRangePartitioner("id", minId, maxId, 10);
    }
    @Bean
    public TaskExecutor dateTaskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(10); // 최대 10개의 스레드로 병렬 처리
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

