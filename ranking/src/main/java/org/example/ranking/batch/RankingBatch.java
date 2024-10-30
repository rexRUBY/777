package org.example.ranking.batch;

import lombok.RequiredArgsConstructor;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.example.ranking.entity.Ranking;
import org.example.ranking.processor.rankingProcessor.RankingProcessorBtc;
import org.example.ranking.processor.rankingProcessor.RankingProcessorEth;
import org.example.ranking.repository.RankingRepository;
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
public class RankingBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final UserRepository userRepository;
    private final RankingProcessorBtc rankingProcessorBtc;
    private final RankingProcessorEth rankingProcessorEth;
    private final RankingRepository rankingRepository;

    // 순위 배치 프로세스를 위한 메인 작업(Job)을 정의
    @Bean
    public Job firstJob(Step firstRateStep, Step secondRateStep) {
        return new JobBuilder("firstJob", jobRepository)
                .start(firstStep())
                .next(secondStep())
                .next(firstRateStep) // RankingRateBatch 의 firstRateStep
                .next(secondRateStep) // RankingRateBatch 의 secondRateStep
                .build();
    }

    // User 데이터를 읽고 처리 후 Ranking으로 저장하는 단계를 정의, 청크 크기는 10으로 설정
    @Bean
    public Step firstStep() {//btc 정보 계산용
        return new StepBuilder("firstStep", jobRepository)
                .<User, Ranking>chunk(10, platformTransactionManager)
                .reader(beforeReader()) // User 데이터를 읽어옴
                .processor(rankingProcessorBtc) // User 데이터를 Ranking으로 변환
                .writer(afterWriter()) // 변환된 Ranking 데이터를 저장
                .build();
    }

    @Bean
    public Step secondStep() {//eth 정보 계산용
        return new StepBuilder("secondStep", jobRepository)
                .<User, Ranking>chunk(10, platformTransactionManager)
                .reader(beforeReader()) // User 데이터를 읽어옴
                .processor(rankingProcessorEth) // User 데이터를 Ranking으로 변환
                .writer(afterWriter()) // 변환된 Ranking 데이터를 저장
                .build();
    }

    // User 데이터를 읽기 위한 설정을 정의
    @Bean
    public RepositoryItemReader<User> beforeReader() {
        return new RepositoryItemReaderBuilder<User>()
                .name("beforeReader")
                .pageSize(10) // 한 번에 10개의 User 데이터를 읽어옴
                .methodName("findAllJoinTradeJoinWallet") // UserRepository의 메서드 이름
                .repository(userRepository)
                .sorts(Map.of("id", Sort.Direction.ASC)) // User 데이터를 ID 기준으로 오름차순 정렬
                .build();
    }

    // 처리된 Ranking 데이터를 데이터베이스에 저장하기 위한 설정을 정의
    @Bean
    public RepositoryItemWriter<Ranking> afterWriter() {
        return new RepositoryItemWriterBuilder<Ranking>()
                .repository(rankingRepository)
                .methodName("save") // RankingRepository의 save 메서드를 사용하여 데이터 저장
                .build();
    }


}
