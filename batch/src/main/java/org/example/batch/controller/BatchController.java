package org.example.batch.controller;

import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@ResponseBody
public class BatchController {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;
    private final TaskExecutor taskExecutor;
    public BatchController(JobLauncher jobLauncher, JobRegistry jobRegistry,
                           @Qualifier("defaultTaskExecutor") TaskExecutor taskExecutor) {
        this.jobLauncher = jobLauncher;
        this.jobRegistry = jobRegistry;
        this.taskExecutor = taskExecutor;
    }


    @GetMapping("/test")
    public String test() {
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
            jobLauncher.run(jobRegistry.getJob("checkJob"), jobParameters);
            return "Job executed successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Job execution failed!";
        }
    }
    //1분에 한번씩 코인의 가격을 비교
    @GetMapping("/first")
    public String first(@RequestParam("price") Long price,
                           @RequestParam("cryptoSymbol") String cryptoSymbol) throws JobExecutionException {

        // cryptoSymbol 값이 "BTC"인지 "ETH"인지 체크
        if ("BTC".equals(cryptoSymbol)) {
            // BTC일 때 실행할 코드
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("price", price)
                    .addString("cryptoSymbol", "BTC")  // Job 1은 BTC 데이터만 처리
                    .toJobParameters();

            // 비동기적으로 Job 1 실행 (병렬 실행)
            taskExecutor.execute(() -> {
                try {
                    jobLauncher.run(jobRegistry.getJob("checkJob"), jobParameters);
                } catch (JobExecutionException e) {
                    e.printStackTrace();
                }
            });

            return "Job 1 (BTC) started";
        } else if ("ETH".equals(cryptoSymbol)) {
            // ETH일 때 실행할 코드
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("price", price)
                    .addString("cryptoSymbol", "ETH")  // Job 2는 ETH 데이터만 처리
                    .toJobParameters();

            // 비동기적으로 Job 2 실행 (병렬 실행)
            taskExecutor.execute(() -> {
                try {
                    jobLauncher.run(jobRegistry.getJob("checkJob"), jobParameters);
                } catch (JobExecutionException e) {
                    e.printStackTrace();
                }
            });

            return "Job 2 (ETH) started";
        } else {
            // cryptoSymbol이 BTC도 ETH도 아닌 경우
            return "Invalid cryptoSymbol. Please provide either 'BTC' or 'ETH'.";
        }
    }

    //하루에 한번 날짜 체크하고 정산작업진행
    @GetMapping("/second")
    public String second(@RequestParam("price") Long price,
                           @RequestParam("cryptoSymbol") String cryptoSymbol) throws JobExecutionException {

        // cryptoSymbol 값이 "BTC"인지 "ETH"인지 체크
        if ("BTC".equals(cryptoSymbol)) {
            // BTC일 때 실행할 코드
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("price", price)
                    .addString("cryptoSymbol", "BTC")  // Job 1은 BTC 데이터만 처리
                    .toJobParameters();

            // 비동기적으로 Job 1 실행 (병렬 실행)
            taskExecutor.execute(() -> {
                try {
                    jobLauncher.run(jobRegistry.getJob("job1"), jobParameters);
                } catch (JobExecutionException e) {
                    e.printStackTrace();
                }
            });

            return "Job 1 (BTC) started";
        } else if ("ETH".equals(cryptoSymbol)) {
            // ETH일 때 실행할 코드
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("price", price)
                    .addString("cryptoSymbol", "ETH")  // Job 2는 ETH 데이터만 처리
                    .toJobParameters();

            // 비동기적으로 Job 2 실행 (병렬 실행)
            taskExecutor.execute(() -> {
                try {
                    jobLauncher.run(jobRegistry.getJob("job2"), jobParameters);
                } catch (JobExecutionException e) {
                    e.printStackTrace();
                }
            });

            return "Job 2 (ETH) started";
        } else {
            // cryptoSymbol이 BTC도 ETH도 아닌 경우
            return "Invalid cryptoSymbol. Please provide either 'BTC' or 'ETH'.";
        }
    }

}
