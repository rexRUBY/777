package org.example.ranking.proccessor.rankingProcessor;

import lombok.extern.slf4j.Slf4j;
import org.example.common.user.entity.User;
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
public class RankingProcessorBtc implements ItemProcessor<User, Ranking>, StepExecutionListener {

    private final RankingRepository rankingRepository;
    private final RankingCalculationService rankingCalculationService;

    // StepExecution에서 사용할 ExecutionContext
    private ExecutionContext executionContext;

    public RankingProcessorBtc(RankingRepository rankingRepository, RankingCalculationService rankingCalculationService) {
        this.rankingRepository = rankingRepository;
        this.rankingCalculationService = rankingCalculationService;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.executionContext = stepExecution.getExecutionContext();
    }

    @Override
    public Ranking process(User user) throws Exception {
        log.info("process start btc");
        LocalDateTime time = LocalDateTime.now();
        String userEmail = user.getEmail();
        log.info(userEmail);
        // BTC 랭킹 처리
        String btcKey = user.getEmail() + "_btc" + time;
        if (executionContext.containsKey(btcKey) &&
                rankingRepository.existsByUserEmailAndCryptoSymbolAndCreatedAt(userEmail, "BTC", time)) {
            throw new IllegalStateException("duplicated");
        }

        double btcYield = rankingCalculationService.calculateYield(user, "BTC");
        executionContext.put(btcKey, true); // 중복 체크용
        return new Ranking(userEmail, "BTC", btcYield);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }
}
