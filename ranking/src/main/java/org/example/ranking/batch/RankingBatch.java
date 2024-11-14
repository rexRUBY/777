package org.example.ranking.batch;

import lombok.RequiredArgsConstructor;
import org.example.common.ranking.entity.Ranking;
import org.example.common.ranking.repository.RankingRepository;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.example.ranking.partitioning.ColumnRangePartitioner;
import org.example.ranking.processor.rankingProcessor.RankingProcessor;
import org.example.ranking.writer.ListRankingWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class RankingBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final UserRepository userRepository;
    private final RankingProcessor rankingProcessor;
    private final RankingRepository rankingRepository;

    // 순위 배치 프로세스를 위한 메인 작업(Job)을 정의
    @Bean
    public Job firstJob(Step firstRateStep, Step secondRateStep) {
        return new JobBuilder("firstJob", jobRepository)
                .start(firstStep())
                .next(firstRateStep) // RankingRateBatch 의 firstRateStep
                .next(secondRateStep) // RankingRateBatch 의 secondRateStep
                .build();
    }

    @Bean
    public Step firstStep() {
        return new StepBuilder("firstStep", jobRepository)
                .partitioner("firstStep", partitioner())
                .step(firstRankingStep())
                .gridSize(10)
                .build();
    }

    // User 데이터를 읽고 처리 후 Ranking으로 저장하는 단계를 정의
    @Bean
    public Step firstRankingStep() {
        return new StepBuilder("firstRankingStep", jobRepository)
                .<User, List<Ranking>>chunk(5000, platformTransactionManager)
                .reader(beforeReader())
                .processor(rankingProcessor)
                .writer(afterWriter())
                .build();
    }

    // User 데이터를 읽기 위한 설정을 정의
    @Bean
    public RepositoryItemReader<User> beforeReader() {
        return new RepositoryItemReaderBuilder<User>()
                .name("beforeReader")
                .pageSize(5000)
                .methodName("findAllByProcessedFalseJoinTradeJoinWallet") // UserRepository의 메서드 이름
                .repository(userRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ListRankingWriter afterWriter() {
        return new ListRankingWriter(rankingRepository); // RankingRepository를 전달
    }

    @Bean
    public ColumnRangePartitioner partitioner() {
        Long minId = userRepository.findMinId(); // 최소 ID 조회
        Long maxId = userRepository.findMaxId(); // 최대 ID 조회
        return new ColumnRangePartitioner("id", minId, maxId, 10);
    }
}
