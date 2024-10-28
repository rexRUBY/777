package com.sparta.ranking.proccessor.rankingProcessor;


import com.sparta.ranking.entity.Ranking;
import com.sparta.ranking.repository.RankingRepository;
import com.sparta.ranking.service.RankingCalculationService;
import org.example.common.user.entity.User;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@StepScope
public class RankingProcessorEth implements ItemProcessor<User, Ranking>, StepExecutionListener {

    private final RankingRepository rankingRepository;
    private final RankingCalculationService rankingCalculationService;

    // StepExecution에서 사용할 ExecutionContext
    private ExecutionContext executionContext;

    public RankingProcessorEth(RankingRepository rankingRepository, RankingCalculationService rankingCalculationService) {
        this.rankingRepository = rankingRepository;
        this.rankingCalculationService = rankingCalculationService;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.executionContext = stepExecution.getExecutionContext();
    }

    @Override
    public Ranking process(User user) throws Exception {
        LocalDateTime time = LocalDateTime.now();
        String userEmail = user.getEmail();
        String ethKey = user.getEmail() + "_eth" + time;
        if (executionContext.containsKey(ethKey)&&
                rankingRepository.existsByUserEmailAndCryptoSymbolAndCreatedAt(userEmail, "ETH",time)) {
            throw new IllegalStateException("duplicated");
        }
        double ethYield = rankingCalculationService.calculateYield(user, "ETH");
        executionContext.put(ethKey,true);

        return new Ranking(userEmail,"ETH",ethYield);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }
}
