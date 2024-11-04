package org.example.ranking.processor.rankingRateProcessor;


import lombok.extern.slf4j.Slf4j;
import org.example.common.ranking.entity.Ranking;
import org.example.common.ranking.repository.RankingRepository;
import org.example.ranking.service.RankingCalculationService;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@StepScope
public class RankingRateProcessEth implements ItemProcessor<Ranking, Ranking>, StepExecutionListener {

    private final RankingRepository rankingRepository;
    private final RankingCalculationService rankingCalculationService;

    // StepExecution에서 사용할 ExecutionContext
    private ExecutionContext executionContext;

    public RankingRateProcessEth(RankingRepository rankingRepository, RankingCalculationService rankingCalculationService) {
        this.rankingRepository = rankingRepository;
        this.rankingCalculationService = rankingCalculationService;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.executionContext = stepExecution.getExecutionContext();

    }

    @Override
    public Ranking process(Ranking ranking) throws Exception {
        log.info("process start rate eth");
        LocalDateTime time= LocalDateTime.now().minusDays(1);
        LocalDateTime time2= LocalDateTime.now();
        String userEmail = ranking.getUserEmail();
        log.info(userEmail);
        // eth 랭킹 처리
        String ethKey2 = ranking.getUserEmail() + "_eth" + time+"_ranked";
        if (!executionContext.containsKey(ethKey2) ) {
            rankingCalculationService.setRank(ranking, "ETH");
            executionContext.put(ethKey2, true);
        }
//      rank.update()

//        !rankingRepository.existsByUserEmailAndCryptoSymbolAndUserRankNotNullAndCreatedAtBetween(userEmail, "ETH",time,time2)
        // 중복 체크용
        return ranking;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }
}
