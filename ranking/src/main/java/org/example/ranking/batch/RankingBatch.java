package org.example.ranking.batch;

import lombok.RequiredArgsConstructor;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.example.ranking.entity.Ranking;
import org.example.ranking.partitioning.ColumnRangePartitioner;
import org.example.ranking.processor.rankingProcessor.RankingProcessor;
import org.example.ranking.repository.RankingRepository;
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
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
//                .start(firstStep())
                .start(firstRateStep) // RankingRateBatch 의 firstRateStep
                .next(secondRateStep) // RankingRateBatch 의 secondRateStep
                .build();
    }
    @Bean
    public Step firstStep() {
        return new StepBuilder("firstStep", jobRepository)
                .partitioner("firstStep", partitioner()) // 파티셔너 적용
                .step(firstRankingStep())
                .gridSize(10) // 파티션 수
//                .taskExecutor(taskExecutor())
                .build();
    }

    // User 데이터를 읽고 처리 후 Ranking으로 저장하는 단계를 정의, 청크 크기는 10으로 설정
    @Bean
    public Step firstRankingStep() {//btc 정보 계산용
        return new StepBuilder("firstRankingStep", jobRepository)
                .<User, List<Ranking>>chunk(5000, platformTransactionManager)
                .reader(beforeReader()) // User 데이터를 읽어옴
                .processor(rankingProcessor) // User 데이터를 Ranking으로 변환
                .writer(afterWriter()) // 변환된 Ranking 데이터를 저장
//                .taskExecutor(taskExecutor())// 멀티스레드 적용
                .build();
    }


    // User 데이터를 읽기 위한 설정을 정의
    @Bean
    public RepositoryItemReader<User> beforeReader() {
        return new RepositoryItemReaderBuilder<User>()
                .name("beforeReader")
                .pageSize(5000) // 한 번에 10개의 User 데이터를 읽어옴
                .methodName("findAllByProcessedFalseJoinTradeJoinWallet") // UserRepository의 메서드 이름
                .repository(userRepository)
                .sorts(Map.of("id", Sort.Direction.ASC)) // User 데이터를 ID 기준으로 오름차순 정렬
                .build();
    }

    @Bean
    public ListRankingWriter afterWriter() {
        return new ListRankingWriter(rankingRepository); // RankingRepository를 전달
    }

    // ThreadPoolTaskExecutor 설정
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4); // 기본 스레드 수
        executor.setMaxPoolSize(8); // 최대 스레드 수
        executor.setQueueCapacity(300); // 큐 용량
        executor.setThreadNamePrefix("RankingRateBatch-");
        executor.initialize();
        return executor;
    }
    @Bean
    public ColumnRangePartitioner partitioner() {
        Long minId = userRepository.findMinId(); // 최소 ID 조회
        Long maxId = userRepository.findMaxId(); // 최대 ID 조회
        return new ColumnRangePartitioner("id", minId, maxId, 10);
    }




}
