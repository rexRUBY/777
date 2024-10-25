package org.example.batch.schedule;

import org.example.batch.config.JobCompletionNotificationListener;
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
public class FirstSchedule {
    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;
    private final JobCompletionNotificationListener jobCompletionNotificationListener;

    public FirstSchedule(JobLauncher jobLauncher, JobRegistry jobRegistry, JobCompletionNotificationListener jobCompletionNotificationListener) {
        this.jobLauncher = jobLauncher;
        this.jobRegistry = jobRegistry;
        this.jobCompletionNotificationListener = jobCompletionNotificationListener;
    }

    @Scheduled(cron = "* * * * * *", zone = "Asia/Seoul")
    public void runFirstJob() throws Exception {

        System.out.println("first schedule start");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        String date = dateFormat.format(new Date());

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", date)
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(jobRegistry.getJob("firstJob"), jobParameters);

        jobCompletionNotificationListener.afterJob(jobExecution);
    }
}