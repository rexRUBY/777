package org.example.batch.schedule;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.Date;

@Configuration
public class BillingSchedule {
    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    public BillingSchedule(JobLauncher jobLauncher, JobRegistry jobRegistry) {
        this.jobLauncher = jobLauncher;
        this.jobRegistry = jobRegistry;
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul") // 매일 01시에 실행
    public void runJobs() throws Exception {
        System.out.println("Job schedule start");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String date = dateFormat.format(new Date());

        // JobParameters 설정
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", date)
                .toJobParameters();

        // 첫 번째 Job 실행
        JobExecution firstJobExecution = jobLauncher.run(jobRegistry.getJob("secondJob"), jobParameters);
        if (firstJobExecution.getStatus() == BatchStatus.COMPLETED) {
            // 두 번째 Job 실행
            JobExecution secondJobExecution = jobLauncher.run(jobRegistry.getJob("thirdJob"), jobParameters);
        }
    }
}
