package org.example.batch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;


    @GetMapping("/first")
    public String firstApi(@RequestParam("value") String value) throws JobExecutionException {

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", value)
                .toJobParameters();

        // 첫 번째 Job 실행
        JobExecution firstJobExecution = jobLauncher.run(jobRegistry.getJob("checkJob"), jobParameters);
//        JobExecution firstJobExecution = jobLauncher.run(jobRegistry.getJob("secondJob"), jobParameters);
//        if (firstJobExecution.getStatus() == BatchStatus.COMPLETED) {
//            // 두 번째 Job 실행
//            JobExecution secondJobExecution = jobLauncher.run(jobRegistry.getJob("thirdJob"), jobParameters);
//        }


        return "ok";
    }
}
