package org.example.ranking.service;

import lombok.extern.slf4j.Slf4j;
import org.example.common.common.log.LogExecution;
import org.example.common.ranking.entity.Ranked;
import org.example.common.ranking.entity.Ranking;
import org.example.common.trade.entity.Trade;
import org.example.common.trade.enums.TradeFor;
import org.example.common.user.entity.User;
import org.example.common.wallet.entity.Wallet;
import org.example.common.wallet.entity.WalletHistory;
import org.example.ranking.config.CountConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class RankingCalculationService {

    private final RestTemplate restTemplate;

    public RankingCalculationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @LogExecution
    // 특정 암호화폐의 수익률을 계산하여 반환하는 메서드
    public double calculateYield(User user, String cryptoSymbol) throws RuntimeException {
        // 다른 사용자를 위한 거래 가격을 계산
        double otherPriceForCrypto = calculateOtherPrice(user.getTradeList(), cryptoSymbol);

        // 한 달 전 마지막 지갑 기록과 현재 지갑 기록을 가져옴
        WalletHistory lastMonthWallet = findClosestLastMonthWallet(user.getWalletHistoryList(), cryptoSymbol);
        WalletHistory nowWallet = findClosestThisMonthWallet(user.getWalletHistoryList(), cryptoSymbol);

        if(lastMonthWallet == null){
            Wallet wallet = user.getWalletList().stream()
                    .filter(w -> w.getCryptoSymbol().equals(cryptoSymbol))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("지갑이 없습니다."));
            lastMonthWallet=new WalletHistory(wallet);
        }
        if(nowWallet == null){
            nowWallet=lastMonthWallet;
        }

        // 수익률을 계산하여 반환
        double yield = calculateYieldPercentage(lastMonthWallet, nowWallet, otherPriceForCrypto);

        // 모니터링 서버로 수익률 전송
        sendYieldToMonitoringServer(user.getEmail(), cryptoSymbol, yield);

        return yield;
    }

    private void sendYieldToMonitoringServer(String userEmail, String cryptoSymbol, double yield) {
        String url = "http://localhost:8086/api/metrics/yield?userEmail=" + userEmail + "&cryptoSymbol=" + cryptoSymbol + "&yield=" + yield;
        restTemplate.postForEntity(url, null, Void.class);
    }

    @LogExecution
    public void setRank(Ranking ranking, String crtproSymbol){
        if(ranking.getCryptoSymbol().equals(crtproSymbol)&&ranking.getRanked().equals(Ranked.ON)){
        ranking.update(CountConfig.count);
        CountConfig.count++;

        }
    }

    // 다른 사용자를 위한 거래 금액을 계산하는 메서드
    private double calculateOtherPrice(List<Trade> tradeList, String cryptoSymbol) {
        // 전월의 첫 번째 날 계산 (예: 오늘이 10월 25일이면, 9월 1일을 계산)
        LocalDate startDate = LocalDate.now().withDayOfMonth(1).minusMonths(1);
        // 현재 달의 첫 번째 날 계산 (예: 10월 1일)
        LocalDate endDate = LocalDate.now().withDayOfMonth(1);
        return tradeList.stream()
                .filter(t -> t.getCrypto().getSymbol().equals(cryptoSymbol) && t.getTradeFor().equals(TradeFor.OTHER))
                .filter(w -> {
                    LocalDate modifiedDate = w.getModifiedAt().toLocalDate();
                    // 전월 첫째 날 이상, 당월 첫째 날 미만의 데이터를 필터링
                    return (modifiedDate.isEqual(startDate) || modifiedDate.isAfter(startDate))
                            && modifiedDate.isBefore(endDate);
                })
                .mapToDouble(t -> t.getTotalPrice() * 0.1)
                .sum();
    }

    private WalletHistory findClosestThisMonthWallet(List<WalletHistory> walletHistoryList, String cryptoSymbol) {
        // 이번 달의 첫 번째 날
        LocalDate firstDayOfCurrentMonth = LocalDate.now().withDayOfMonth(1);

        return walletHistoryList.stream()
                .filter(w -> w.getModifiedAt() != null &&w.getCryptoSymbol().equals(cryptoSymbol))
                .filter(w -> w.getModifiedAt().toLocalDate().isBefore(firstDayOfCurrentMonth))
                .max(Comparator.comparing(WalletHistory::getModifiedAt)) // 이번달1일 이전의 가장 최신의 지갑찾기
                .orElse(null);
    }
    private WalletHistory findClosestLastMonthWallet(List<WalletHistory> walletHistoryList, String cryptoSymbol) {
        // 저번 달의 첫 번째 날
        LocalDate firstDayOfCurrentMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        return walletHistoryList.stream()
                .filter(w -> w.getCryptoSymbol() != null && w.getCryptoSymbol().equals(cryptoSymbol))
                .filter(w -> w.getModifiedAt() != null && w.getModifiedAt().toLocalDate().isBefore(firstDayOfCurrentMonth))
                .max(Comparator.comparing(WalletHistory::getModifiedAt)) // 저번달1일 이전의 가장 최신의 지갑찾기
                .orElse(null);
    }

    // 두 지갑 기록을 비교하여 수익률을 계산하는 메서드
    private double calculateYieldPercentage(WalletHistory lastMonthWallet, WalletHistory nowWallet, double otherPrice) {
        double lastTotal = (double)lastMonthWallet.getCash() + lastMonthWallet.getAmount() * (double)lastMonthWallet.getCryptoPrice();
        double nowTotal = (double)nowWallet.getCash() + nowWallet.getAmount() * (double)nowWallet.getCryptoPrice();

        return ((nowTotal - lastTotal - otherPrice) / lastTotal) * 100;
    }

}

