package org.example.ranking.processor.rankingRateProcessor;

import lombok.extern.slf4j.Slf4j;
import org.example.common.ranking.entity.Ranking;
import org.example.ranking.service.RankingCalculationService;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@StepScope
public class RankingRateProcessEth implements ItemProcessor<Ranking, Ranking>, StepExecutionListener {

    private final RankingCalculationService rankingCalculationService;

    // StepExecution에서 사용할 ExecutionContext
    private ExecutionContext executionContext;

    public RankingRateProcessEth(RankingCalculationService rankingCalculationService) {
        this.rankingCalculationService = rankingCalculationService;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.executionContext = stepExecution.getExecutionContext();

    }

    @Override
    public Ranking process(Ranking ranking) throws Exception {
        log.info("process start rate eth");
        LocalDate time= LocalDate.now();
        String userEmail = ranking.getUserEmail();
        log.info(userEmail);
        // eth 랭킹 처리
        String ethKey2 = ranking.getUserEmail() + "_eth" + time+"_ranked";
        if (!executionContext.containsKey(ethKey2) ) {
            rankingCalculationService.setRank(ranking, "ETH");
            executionContext.put(ethKey2, true);
        }
        return ranking;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }
}
