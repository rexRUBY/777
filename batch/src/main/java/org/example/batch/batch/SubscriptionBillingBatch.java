package org.example.batch.batch;

import lombok.RequiredArgsConstructor;
import org.example.batch.processor.subscriptionBillingProccessor.BtcSubscriptionBillingProccessor;
import org.example.batch.processor.subscriptionBillingProccessor.EthSubscriptionBillingProccessor;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.example.common.wallet.entity.Wallet;
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

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SubscriptionBillingBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final UserRepository userRepository;
    private final BtcSubscriptionBillingProccessor btcSubscriptionBillingProccessor;
    private final EthSubscriptionBillingProccessor ethSubscriptionBillingProccessor;

    //구독정보를 초작하기위한 job
    @Bean
    public Job secondJob(Step firstDeleteStep) {
        return new JobBuilder("secondJob", jobRepository)
                .start(firstBillingStep())
                .next(secondBillingStep())
                .next(firstDeleteStep)
                .build();
    }

    // User 데이터를 읽고 처리 후 subscription을 변경하는 단계를 정의, 청크 크기는 10으로 설정
    @Bean
    public Step firstBillingStep() {//btc 정보 계산용
        return new StepBuilder("firstBillingStep", jobRepository)
                .<User, Wallet>chunk(10, platformTransactionManager)
                .reader(beforeBillingReader()) // User 데이터를 읽어옴
                .processor(btcSubscriptionBillingProccessor) // User 데이터를 subscription으로 변환
                .build();
    }

    @Bean
    public Step secondBillingStep() {//eth 정보 계산용
        return new StepBuilder("secondBillingStep", jobRepository)
                .<User, Wallet>chunk(10, platformTransactionManager)
                .reader(beforeBillingReader()) // User 데이터를 읽어옴
                .processor(ethSubscriptionBillingProccessor) // User 데이터를 Ranking으로 변환
                .build();
    }

    // User 데이터를 읽기 위한 설정을 정의
    @Bean
    public RepositoryItemReader<User> beforeBillingReader() {
        return new RepositoryItemReaderBuilder<User>()
                .name("beforeBillingReader")
                .pageSize(10) // 한 번에 10개의 User 데이터를 읽어옴
                .methodName("findAllJoinSubscriptsJoinWallet") // UserRepository의 메서드 이름
                .repository(userRepository)
                .sorts(Map.of("id", Sort.Direction.ASC)) // User 데이터를 ID 기준으로 오름차순 정렬
                .build();
    }
}
