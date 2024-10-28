package org.example.ranking.proccessor.rankingRateProcessor;


import lombok.extern.slf4j.Slf4j;
import org.example.ranking.entity.Ranking;
import org.example.ranking.repository.RankingRepository;
import org.example.ranking.service.RankingCalculationService;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@StepScope
public class RankingRateProcessBtc implements ItemProcessor<Ranking, Ranking>, StepExecutionListener {

    private final RankingRepository rankingRepository;
    private final RankingCalculationService rankingCalculationService;

    // StepExecution에서 사용할 ExecutionContext
    private ExecutionContext executionContext;

    public RankingRateProcessBtc(RankingRepository rankingRepository, RankingCalculationService rankingCalculationService) {
        this.rankingRepository = rankingRepository;
        this.rankingCalculationService = rankingCalculationService;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.executionContext = stepExecution.getExecutionContext();

    }

    @Override
    public Ranking process(Ranking ranking) throws Exception {
        log.info("process start rate btc");
        LocalDateTime time = LocalDateTime.now();
        String userEmail = ranking.getUserEmail();
        log.info(userEmail);
        // BTC 랭킹 처리
        String btcKey2 = ranking.getUserEmail() + "_btc" + time + "_ranked";
        if (executionContext.containsKey(btcKey2) &&
                rankingRepository.existsByUserEmailAndCryptoSymbolAndCreatedAtAndUserRankNotNull(userEmail, "BTC", time)) {
            throw new IllegalStateException("duplicated");
        }
//      rank.update()
        rankingCalculationService.setRank(ranking, "BTC");
        executionContext.put(btcKey2, true); // 중복 체크용

        return ranking;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }
}
