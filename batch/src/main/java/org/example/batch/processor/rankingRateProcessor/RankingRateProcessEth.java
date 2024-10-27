package org.example.batch.processor.rankingRateProcessor;

import lombok.extern.slf4j.Slf4j;
import org.example.batch.entity.Ranking;
import org.example.batch.repository.RankingRepository;
import org.example.batch.service.RankingCalculationService;
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
        LocalDateTime time = LocalDateTime.now();
        String userEmail = ranking.getUserEmail();
        log.info(userEmail);
        // eth 랭킹 처리
        String ethKey2 = ranking.getUserEmail() + "_eth" + time + "_ranked";
        if (executionContext.containsKey(ethKey2) &&
                rankingRepository.existsByUserEmailAndCryptoSymbolAndCreatedAtAndUserRankNotNull(userEmail, "ETH", time)) {
            throw new IllegalStateException("duplicated");
        }
//      rank.update()
        rankingCalculationService.setRank(ranking, "ETH");
        executionContext.put(ethKey2, true); // 중복 체크용
        return ranking;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }
}
