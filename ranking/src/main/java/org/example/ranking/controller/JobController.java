package org.example.ranking.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/api/job")
public class JobController {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    public JobController(JobLauncher jobLauncher, JobRegistry jobRegistry) {
        this.jobLauncher = jobLauncher;
        this.jobRegistry = jobRegistry;
    }

    @GetMapping("/runFirstJob")
    public String runFirstJob() {
        try {
            System.out.println("Job execution via API started");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            String date = dateFormat.format(new Date());

            // JobParameters 설정
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("date", date)
                    .addLong("run.id", System.currentTimeMillis()) // 각 실행마다 고유한 ID 추가
                    .toJobParameters();

            // 첫 번째 Job 실행
            jobLauncher.run(jobRegistry.getJob("firstJob"), jobParameters);
            return "Job executed successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Job execution failed!";
        }
    }
}
