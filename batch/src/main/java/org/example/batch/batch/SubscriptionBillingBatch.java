/*
package org.example.batch.batch;

import lombok.RequiredArgsConstructor;
import org.example.batch.partitioner.UserPartitioner;
import org.example.batch.processor.subscriptionBillingProccessor.BtcSubscriptionBillingProccessor;
import org.example.batch.processor.subscriptionBillingProccessor.EthSubscriptionBillingProccessor;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;
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

    // 구독정보를 조작하기위한 job
    */
/*@Bean
    public Job secondJob() {
        return new JobBuilder("secondJob", jobRepository)
                .start(firstBillingStep())
                .next(secondBillingStep())
                .build();
    }*//*


    // User 데이터를 읽고 처리 후 subscription을 변경하는 단계를 정의, 청크 크기는 10으로 설정

    @Bean
    public Step firstBillingStep() {//btc 정보 계산용
        return new StepBuilder("firstBillingStep", jobRepository)
                .<User, User>chunk(10000, platformTransactionManager)
                .reader(beforeBillingReader()) // User 데이터를 읽어옴'
                .processor(btcSubscriptionBillingProccessor) // User 데이터를 subscription으로 변환
                .writer(afterBillingWriter()) // 변환된 Wallet 데이터를 저장
                .build();
    }

    @Bean
    public Step secondBillingStep() {//eth 정보 계산용
        return new StepBuilder("secondBillingStep", jobRepository)
                .<User, User>chunk(10000, platformTransactionManager)
                .reader(beforeBillingReader()) // User 데이터를 읽어옴
                .processor(ethSubscriptionBillingProccessor) // User 데이터를 Ranking으로 변환
                .writer(afterBillingWriter()) // 변환된 User 데이터를 저장
                .build();
    }

    // User 데이터를 읽기 위한 설정을 정의
    */
/*@Bean
    public RepositoryItemReader<User> beforeBillingReader() {
        return new RepositoryItemReaderBuilder<User>()
                .name("beforeBillingReader")
                .pageSize(10000) // 한 번에 10개의 User 데이터를 읽어옴
                .methodName("findAllJoinSubscriptsJoinWallet") // UserRepository의 메서드 이름
                .repository(userRepository)
                .sorts(Map.of("id", Sort.Direction.ASC)) // User 데이터를 ID 기준으로 오름차순 정렬
                .build();
    }*//*

    */
/*@Bean
    public RepositoryItemReader<User> beforeBillingReader() {
        return new RepositoryItemReaderBuilder<User>()
                .name("beforeBillingReader")
                .pageSize(10000)
                .methodName("findAllByIdBetween")
                .repository(userRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                // 인수는 ExecutionContext에서 가져와야 하므로
                .arguments("#start", "#end", PageRequest.of(0, 10000)) // 인수를 ExecutionContext의 start, end로 설정
                .build();
    }*//*




    @Bean
    public RepositoryItemReader<User> beforeBillingReader() {
        return new RepositoryItemReaderBuilder<User>()
                .name("beforeBillingReader")
                .pageSize(10000)
                .methodName("findAllByIdBet")
                .repository(userRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .arguments("#executionContext.start", "#executionContext.end", PageRequest.of(0, 10000))

                .build();
    }

    */
/*@Bean
    public RepositoryItemReader<User> userItemReader() {
        RepositoryItemReader<User> reader = new RepositoryItemReader<>();
        reader.setRepository(userRepository);
        reader.setMethodName("findAllByIdBetween");

        // 매개변수 설정 (start, end, Pageable)
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start", start); // 동적으로 할당
        parameters.put("end", end);     // 동적으로 할당
        parameters.put("pageable", pageable);

        reader.setParameters(parameters);
        return reader;
    }*//*



    // 처리된 Ranking 데이터를 데이터베이스에 저장하기 위한 설정을 정의
    @Bean
    public RepositoryItemWriter<User> afterBillingWriter() {
        return new RepositoryItemWriterBuilder<User>()
                .repository(userRepository)
                .methodName("save") // userRepository save 메서드를 사용하여 데이터 저장
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() { // 병렬처리
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(4);
        return asyncTaskExecutor;
    }

    // 첫 번째 파티션 핸들러
    @Bean
    public PartitionHandler firstPartitionHandler() {
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setTaskExecutor(taskExecutor());
        partitionHandler.setGridSize(4); // 파티션 수 설정
        partitionHandler.setStep(firstBillingStep());
        return partitionHandler;
    }

    // 두 번째 파티션 핸들러
    @Bean
    public PartitionHandler secondPartitionHandler() {
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setTaskExecutor(new SimpleAsyncTaskExecutor());
        partitionHandler.setGridSize(4); // 파티션 수 설정
        partitionHandler.setStep(secondBillingStep());
        return partitionHandler;
    }

    // 첫 번째 마스터 스텝
    @Bean
    public Step firstMasterStep() {
        return new StepBuilder("firstMasterStep", jobRepository)
                .partitioner("firstBillingStep", partitioner())
                .step(firstBillingStep())
                .taskExecutor(taskExecutor())
                .partitionHandler(firstPartitionHandler())
                .build();
    }

    // 두 번째 마스터 스텝
    @Bean
    public Step secondMasterStep() {
        return new StepBuilder("secondMasterStep", jobRepository)
                .partitioner("secondBillingStep", partitioner())
                .step(secondBillingStep())
                .taskExecutor(taskExecutor())
                .partitionHandler(secondPartitionHandler())
                .build();
    }

    // 최종 잡에 마스터 스텝 추가
    @Bean
    public Job secondJob() {
        return new JobBuilder("secondJob", jobRepository)
                .start(firstMasterStep())
                .next(secondMasterStep())
                .build();
    }

    // Partitioner 빈
    @Bean
    public Partitioner partitioner() {
        return new UserPartitioner();
    }

}
*/
package org.example.batch.batch;

import lombok.RequiredArgsConstructor;
import org.example.batch.partitioner.UserPartitioner;
import org.example.batch.processor.subscriptionBillingProccessor.BtcSubscriptionBillingProccessor;
import org.example.batch.processor.subscriptionBillingProccessor.EthSubscriptionBillingProccessor;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SubscriptionBillingBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final UserRepository userRepository;
    private final BtcSubscriptionBillingProccessor btcSubscriptionBillingProcessor;
    private final EthSubscriptionBillingProccessor ethSubscriptionBillingProcessor;

    private static final long TOTAL_USERS = 10000; // 전체 사용자 수

    // 구독정보를 조작하기위한 job
    @Bean
    public Job secondJob() {
        return new JobBuilder("secondJob", jobRepository)
                .incrementer(new RunIdIncrementer()) // Job이 여러 번 실행될 수 있도록 설정
                .start(firstMasterStep())
                .next(secondMasterStep())
                .build();
    }

    // 첫 번째 마스터 단계
    @Bean
    public Step firstMasterStep() {
        return new StepBuilder("firstMasterStep", jobRepository)
                .partitioner("firstBillingStep", userPartitioner()) // 파티셔너리 지정
                .step(firstBillingStep()) // 실제 처리할 Step 지정
                .partitionHandler(partitionHandler()) // 파티셔닝 처리기 설정
                .build();
    }

    // 두 번째 마스터 단계
    @Bean
    public Step secondMasterStep() {
        return new StepBuilder("secondMasterStep", jobRepository)
                .partitioner("secondBillingStep", userPartitioner()) // 파티셔너리 지정
                .step(secondBillingStep())
                .partitionHandler(partitionHandler())
                .build();
    }

    // User 데이터를 읽고 처리 후 subscription을 변경하는 단계를 정의
    @Bean
    public Step firstBillingStep() {
        return new StepBuilder("firstBillingStep", jobRepository)
                .<User, User>chunk(1000, platformTransactionManager)
                .reader(beforeBillingReader()) // User 데이터를 읽어옴
                .processor(btcSubscriptionBillingProcessor) // User 데이터를 subscription으로 변환
                .writer(afterBillingWriter()) // 변환된 Wallet 데이터를 저장
                .build();
    }

    @Bean
    public Step secondBillingStep() {
        return new StepBuilder("secondBillingStep", jobRepository)
                .<User, User>chunk(1000, platformTransactionManager)
                .reader(beforeBillingReader()) // User 데이터를 읽어옴
                .processor(ethSubscriptionBillingProcessor) // User 데이터를 Ranking으로 변환
                .writer(afterBillingWriter()) // 변환된 User 데이터를 저장
                .build();
    }

    // User 데이터를 읽기 위한 설정을 정의
    @Bean
    public RepositoryItemReader<User> beforeBillingReader() {
        return new RepositoryItemReaderBuilder<User>()
                .name("beforeBillingReader")
                .pageSize(1000) // 한 번에 10개의 User 데이터를 읽어옴
                .methodName("findAllJoinSubscriptsJoinWallet") // UserRepository의 메서드 이름
                .repository(userRepository)
                .sorts(Map.of("id", Sort.Direction.ASC)) // User 데이터를 ID 기준으로 오름차순 정렬
                .build();
    }

    // 처리된 Ranking 데이터를 데이터베이스에 저장하기 위한 설정을 정의
    @Bean
    public RepositoryItemWriter<User> afterBillingWriter() {
        return new RepositoryItemWriterBuilder<User>()
                .repository(userRepository)
                .methodName("save") // userRepository save 메서드를 사용하여 데이터 저장
                .build();
    }

    // UserPartitioner 빈 정의
    @Bean
    public Partitioner userPartitioner() {
        return new UserPartitioner(TOTAL_USERS); // 전체 사용자 수 전달
    }

    // PartitionHandler 설정

    @Bean
    public PartitionHandler partitionHandler() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(15); // 원하는 스레드 수에 맞게 조정
        taskExecutor.setMaxPoolSize(35);
        taskExecutor.setThreadNamePrefix("partition-thread-"); // 스레드 이름 접두사 설정
        taskExecutor.afterPropertiesSet();

        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setTaskExecutor(taskExecutor);
        partitionHandler.setStep(firstBillingStep()); // Step 설정
        partitionHandler.setGridSize(25); // 원하는 파티션 수

        return partitionHandler;
    }



}
