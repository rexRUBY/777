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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    //1분에 한번씩 코인의 가격을 비교
    @GetMapping("/first")
    public String first(@RequestParam("price") Long price,
                        @RequestParam("cryptoSymbol") String cryptoSymbol,
                        @RequestParam("time")String time) throws JobExecutionException {

        // 'time' 파라미터를 받아서 LocalDateTime으로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss"); // 예: 20241130 154500
        LocalDateTime parsedTime = LocalDateTime.parse(time, formatter);

        // cryptoSymbol 값이 "BTC"인지 "ETH"인지 체크
        if ("BTC".equals(cryptoSymbol)) {
            // BTC일 때 실행할 코드
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("price", price)
                    .addString("cryptoSymbol", "BTC")  // Job 1은 BTC 데이터만 처리
                    .addLocalDateTime("time",parsedTime)
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
                    .addLocalDateTime("time",parsedTime)
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
                         @RequestParam("cryptoSymbol") String cryptoSymbol,
                         @RequestParam("time")String time) throws JobExecutionException {
        // 'time' 파라미터를 받아서 LocalDateTime으로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss"); // 예: 20241130 154500
        LocalDateTime parsedTime = LocalDateTime.parse(time, formatter);
        // cryptoSymbol 값이 "BTC"인지 "ETH"인지 체크
        if ("BTC".equals(cryptoSymbol)) {
            // BTC일 때 실행할 코드
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("price", price)
                    .addString("cryptoSymbol", "BTC")  // Job 1은 BTC 데이터만 처리
                    .addLocalDateTime("time",parsedTime)
                    .toJobParameters();

            // 비동기적으로 Job 1 실행 (병렬 실행)
            taskExecutor.execute(() -> {
                try {
                    jobLauncher.run(jobRegistry.getJob("checkDateJob"), jobParameters);
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
                    .addLocalDateTime("time",parsedTime)
                    .toJobParameters();

            // 비동기적으로 Job 2 실행 (병렬 실행)
            taskExecutor.execute(() -> {
                try {
                    jobLauncher.run(jobRegistry.getJob("checkDateJob"), jobParameters);
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
