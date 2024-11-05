package org.example.batch.batch;

import lombok.RequiredArgsConstructor;
import org.example.batch.partitioner.ColumnRangePartitioner;
import org.example.batch.processor.dateProcessor.DateProcessor;
import org.example.common.subscriptions.entity.Subscriptions;
import org.example.common.subscriptions.repository.SubscriptionsRepository;
import org.example.common.webclient.service.CryptoWebService;
import org.example.common.webclient.util.DateTimeUtil;
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
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class CheckDateBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final SubscriptionsRepository subscriptionsRepository;
    private final CryptoWebService cryptoWebService;
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
                .<Subscriptions,Subscriptions >chunk(5000, platformTransactionManager)
                .reader(beforeCheckDateReader(null)) // User 데이터를 읽어옴
                .processor(dateProcessor) // User 데이터를 Ranking으로 변환
                .writer(afterCheckDateWriter()) // 변환된 Ranking 데이터를 저장
                .listener(stepDateExecutionListener())
                .build();
    }
    @Bean
    @StepScope
    public RepositoryItemReader<Subscriptions> beforeCheckDateReader(
            @Value("#{stepExecutionContext['time']}")LocalDateTime time) {
        return new RepositoryItemReaderBuilder<Subscriptions>()
                .name("beforeReader")
                .pageSize(5000) // 한 번에 10개의 User 데이터를 읽어옴
                .methodName("findAllPrice") //
                .repository(subscriptionsRepository)
                .arguments(time)
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
    public StepExecutionListener stepDateExecutionListener() {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                LocalDateTime time = LocalDateTime.now().minusMonths(1);
                Long btcPrice = cryptoWebService.getCryptoValueAsLong("BTC", DateTimeUtil.getCurrentDate(), DateTimeUtil.getCurrentTime());
                Long ethPrice = cryptoWebService.getCryptoValueAsLong("ETH", DateTimeUtil.getCurrentDate(), DateTimeUtil.getCurrentTime());

                // ExecutionContext에 가격 정보를 저장
                stepExecution.getExecutionContext().put("btcPrice", btcPrice);
                stepExecution.getExecutionContext().put("ethPrice", ethPrice);
                // ExecutionContext에 날짜 정보를 저장
                stepExecution.getExecutionContext().put("time", time);}

            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                return ExitStatus.COMPLETED;
            }
        };
    }
}
