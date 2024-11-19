package org.example.ranking.processor.rankingProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.ranking.entity.Ranking;
import org.example.common.common.log.LogExecution;
import org.example.common.ranking.repository.RankingRepository;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.example.ranking.service.RankingCalculationService;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class RankingProcessor implements ItemProcessor<User, List<Ranking>>, StepExecutionListener {

    private final UserRepository userRepository;
    private final RankingCalculationService rankingCalculationService;
    private ExecutionContext executionContext;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.executionContext = stepExecution.getExecutionContext();
    }

    @Override
    @LogExecution
    public List<Ranking> process(User user) throws Exception {
        LocalDate time = LocalDate.now();
        String userEmail = user.getEmail();

        List<Ranking> rankings = new ArrayList<>();
        String btcKey = user.getEmail() + "_btc" + time;
        // BTC 처리
        if (!executionContext.containsKey(btcKey)) {
            double btcYield = rankingCalculationService.calculateYield(user, "BTC");
            rankings.add(new Ranking(userEmail, "BTC", btcYield));
            executionContext.put(btcKey, true);
        }

        String ethKey = user.getEmail() + "_eth" + time;
        // ETH 처리
        if (!executionContext.containsKey(ethKey)) {
            double ethYield = rankingCalculationService.calculateYield(user, "ETH");
            rankings.add(new Ranking(userEmail, "ETH", ethYield));
            executionContext.put(ethKey, true); // 중복 체크용
        }

        user.changeProcess();
        userRepository.save(user);

        return rankings; // 두 개의 Ranking 객체를 포함한 리스트 반환
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void processDailyRanking() {
        List<User> users = userRepository.findAll();

        users.forEach(user -> {
            user.getWalletList().forEach(wallet -> {
                String cryptoSymbol = wallet.getCryptoSymbol();
                try {
                    rankingCalculationService.calculateYield(user, cryptoSymbol);
                } catch (Exception e) {
                    System.err.println("Error calculating yield for user " + user.getEmail() + " and crypto " + cryptoSymbol);
                }
            });
        });
    }
}
