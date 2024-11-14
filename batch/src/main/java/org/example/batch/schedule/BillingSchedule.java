package org.example.batch.schedule;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

@Configuration
public class BillingSchedule {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;
    private final TaskExecutor taskExecutor;

    public BillingSchedule(JobLauncher jobLauncher, JobRegistry jobRegistry,
                           @Qualifier("defaultTaskExecutor") TaskExecutor taskExecutor) {
        this.jobLauncher = jobLauncher;
        this.jobRegistry = jobRegistry;
        this.taskExecutor = taskExecutor;
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul") //01시에 실행
    public void runJobs() throws Exception {
        System.out.println("Job schedule start");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String date = dateFormat.format(new Date());

        // JobParameters 설정
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", date)
                .toJobParameters();

        // 첫 번째 Job 실행
        executeJob("secondJob", jobParameters)
                .thenCompose(firstJobExecution -> {
                    if (firstJobExecution.getStatus() == BatchStatus.COMPLETED) {
                        // 두 번째 Job 실행
                        return executeJob("thirdJob", jobParameters);
                    } else {
                        System.out.println("First job failed.");
                        return CompletableFuture.completedFuture(null);  // 실패 시 바로 종료
                    }
                })
                .thenAccept(secondJobExecution -> {
                    if (secondJobExecution != null && secondJobExecution.getStatus() == BatchStatus.COMPLETED) {
                        System.out.println("Third job completed successfully.");
                    } else {
                        System.out.println("Third job failed.");
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    private CompletableFuture<JobExecution> executeJob(String jobName, JobParameters jobParameters) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return jobLauncher.run(jobRegistry.getJob(jobName), jobParameters);
            } catch (Exception e) {
                throw new RuntimeException("Job execution failed", e);
            }
        }, taskExecutor);
    }
}
