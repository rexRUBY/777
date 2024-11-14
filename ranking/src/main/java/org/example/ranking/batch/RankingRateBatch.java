package org.example.ranking.batch;

import lombok.RequiredArgsConstructor;
import org.example.common.ranking.entity.Ranking;
import org.example.common.ranking.repository.RankingRepository;
import org.example.common.user.repository.UserRepository;
import org.example.ranking.config.CountConfig;
import org.example.ranking.partitioning.ColumnRangePartitioner;
import org.example.ranking.processor.rankingRateProcessor.RankingRateProcessBtc;
import org.example.ranking.processor.rankingRateProcessor.RankingRateProcessEth;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
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
public class RankingRateBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final RankingRateProcessBtc rankingRateProcessBtc;
    private final RankingRateProcessEth rankingRateProcessEth;
    private final UserRepository userRepository;
    private final RankingRepository rankingRepository;

    @Bean
    public Step firstRateStep() {
        return new StepBuilder("firstRateStep", jobRepository)
                .partitioner("firstRateStep", ratePartitioner())
                .step(firstRatingStep())
                .gridSize(10)
                .build();
    }

    @Bean
    public Step secondRateStep() {
        return new StepBuilder("secondRateStep", jobRepository)
                .partitioner("secondRateStep", ratePartitioner())
                .step(secondRatingStep())
                .gridSize(10)
                .build();
    }

    @Bean
    public Step firstRatingStep() {//btc 정보 계산용
        return new StepBuilder("firstRatingStep", jobRepository)
                .<Ranking, Ranking>chunk(5000, platformTransactionManager)
                .reader(beforeBtcRateReader())
                .processor(rankingRateProcessBtc)
                .writer(afterRateWriter())
                .listener(stepExecutionListener())
                .build();
    }

    @Bean
    public Step secondRatingStep() {//eth 정보 계산용
        return new StepBuilder("secondRatingStep", jobRepository)
                .<Ranking, Ranking>chunk(5000, platformTransactionManager)
                .reader(beforeEthRateReader())
                .processor(rankingRateProcessEth)
                .writer(afterRateWriter())
                .listener(stepExecutionListener())
                .build();
    }

    @Bean
    public RepositoryItemReader<Ranking> beforeBtcRateReader() {
        return new RepositoryItemReaderBuilder<Ranking>()
                .name("beforeRateReader")
                .pageSize(5000)
                .methodName("findAllByBtcSelectedFields")
                .repository(rankingRepository)
                .sorts(Map.of("yield", Sort.Direction.DESC))
                .build();
    }

    @Bean
    public RepositoryItemReader<Ranking> beforeEthRateReader() {
        return new RepositoryItemReaderBuilder<Ranking>()
                .name("beforeRateReader")
                .pageSize(5000)
                .methodName("findAllByEthSelectedFields")
                .repository(rankingRepository)
                .sorts(Map.of("yield", Sort.Direction.DESC))
                .build();
    }

    // 처리된 Ranking 데이터를 데이터베이스에 저장하기 위한 설정을 정의
    @Bean
    public RepositoryItemWriter<Ranking> afterRateWriter() {
        return new RepositoryItemWriterBuilder<Ranking>()
                .repository(rankingRepository)
                .methodName("save")
                .build();
    }

    @Bean
    public StepExecutionListener stepExecutionListener() {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                CountConfig.setCount(1L); // 스텝 시작 시 count를 초기화
            }

            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                return ExitStatus.COMPLETED;
            }
        };
    }

    @Bean
    public ColumnRangePartitioner ratePartitioner() {
        Long minId = userRepository.findMinId(); // 최소 ID 조회
        Long maxId = 2 * userRepository.findMaxId(); // 최대 ID 조회
        return new ColumnRangePartitioner("id", minId, maxId, 10);
    }
}
