package org.example.batch.batch;

import lombok.RequiredArgsConstructor;
import org.example.batch.partitioner.ColumnRangePartitioner;
import org.example.batch.processor.checkProcessor.PriceProcessor;
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

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class CheckPriceBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final SubscriptionsRepository subscriptionsRepository;
    private final PriceProcessor priceProcessor;
    private final CryptoWebService cryptoWebService;

    @Bean
    public Job checkJob() {
        return new JobBuilder("checkJob", jobRepository)
                .start(checkStep())
                .build();
    }
    @Bean
    public Step checkStep() {
        return new StepBuilder("checkStep", jobRepository)
                .partitioner("checkStep", pricePartitioner()) // 파티셔너 적용
                .step(firstCheckingStep())
                .gridSize(10) // 파티션 수
                .build();
    }

    @Bean
    public Step firstCheckingStep() {//btc 정보 계산용
        return new StepBuilder("firstCheckingStep", jobRepository)
                .<Subscriptions,Subscriptions >chunk(5000, platformTransactionManager)
                .reader(beforeCheckReader(null,null)) // User 데이터를 읽어옴
                .processor(priceProcessor) // User 데이터를 Ranking으로 변환
                .writer(afterCheckWriter()) // 변환된 Ranking 데이터를 저장
                .listener(stepExecutionListener())
                .build();
    }
    @Bean
    @StepScope
    public RepositoryItemReader<Subscriptions> beforeCheckReader(
            @Value("#{stepExecutionContext['btcPrice']}") Long btcPrice,
            @Value("#{stepExecutionContext['ethPrice']}")Long ethPrice) {
        return new RepositoryItemReaderBuilder<Subscriptions>()
                .name("beforeCheckBtcReader")
                .pageSize(5000) // 한 번에 10개의 User 데이터를 읽어옴
                .methodName("findAllPrice") // UserRepository의 메서드 이름
                .repository(subscriptionsRepository)
                .arguments(btcPrice,ethPrice)
                .sorts(Map.of("id", Sort.Direction.ASC)) // User 데이터를 ID 기준으로 오름차순 정렬
                .build();
    }
    @Bean
    public RepositoryItemWriter<Subscriptions> afterCheckWriter() {
        return new RepositoryItemWriterBuilder<Subscriptions>()
                .repository(subscriptionsRepository)
                .methodName("save") // userRepository save 메서드를 사용하여 데이터 저장
                .build();
    }
    @Bean
    public ColumnRangePartitioner pricePartitioner() {
        Long minId = subscriptionsRepository.findMinId(); // 최소 ID 조회
        Long maxId = subscriptionsRepository.findMaxId(); // 최대 ID 조회
        return new ColumnRangePartitioner("id", minId, maxId, 10);
    }

    @Bean
    public StepExecutionListener stepExecutionListener() {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                Long btcPrice = cryptoWebService.getCryptoValueAsLong("BTC", DateTimeUtil.getCurrentDate(), DateTimeUtil.getCurrentTime());
                Long ethPrice = cryptoWebService.getCryptoValueAsLong("ETH", DateTimeUtil.getCurrentDate(), DateTimeUtil.getCurrentTime());

                // ExecutionContext에 가격 정보를 저장
                stepExecution.getExecutionContext().put("btcPrice", btcPrice);
                stepExecution.getExecutionContext().put("ethPrice", ethPrice);
            }

            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                return ExitStatus.COMPLETED;
            }
        };
    }
}
