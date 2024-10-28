package com.sparta.ranking.service;

import com.sparta.ranking.config.CountConfig;

import com.sparta.ranking.entity.Ranked;
import com.sparta.ranking.entity.Ranking;
import org.example.common.trade.entity.Trade;
import org.example.common.trade.enums.TradeFor;
import org.example.common.user.entity.User;
import org.example.common.wallet.entity.Wallet;
import org.example.common.wallet.entity.WalletHistory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;


@Component
public class RankingCalculationService {

    // 특정 암호화폐의 수익률을 계산하여 반환하는 메서드
    public double calculateYield(User user, String cryptoSymbol) throws RuntimeException {
        // 다른 사용자를 위한 거래 가격을 계산
        double otherPriceForCrypto = calculateOtherPrice(user.getTradeList(), cryptoSymbol);

        // 한 달 전 마지막 지갑 기록과 현재 지갑 기록을 가져옴
        WalletHistory lastMonthWallet = findClosestLastMonthWallet(user.getWalletHistoryList(), cryptoSymbol);
        WalletHistory nowWallet = findClosestThisMonthWallet(user.getWalletHistoryList(), cryptoSymbol);

        /*// 한 달 전 또는 현재 지갑 기록이 없는 경우 예외를 발생시킴
        if (lastMonthWallet == null || nowWallet == null) {
            throw new RuntimeException(String.format("한달전 %s 지갑의 기록이 없습니다.", cryptoSymbol));
        }*/
        if(lastMonthWallet == null){
            Wallet wallet = user.getWalletList().stream()
                    .filter(w -> w.getCryptoSymbol().equals(cryptoSymbol))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("지갑이 없습니다."));
            lastMonthWallet= new WalletHistory(wallet);
        }
        if(nowWallet == null){
            nowWallet=lastMonthWallet;
        }

        // 수익률을 계산하여 반환
        return calculateYieldPercentage(lastMonthWallet, nowWallet, otherPriceForCrypto);
    }

    public void setRank(Ranking ranking, String crtproSymbol){
        if(ranking.getCryptoSymbol().equals(crtproSymbol)&&ranking.getRanked().equals(Ranked.ON)){
        ranking.update(CountConfig.count);
        CountConfig.count++;

        }
    }

    // 다른 사용자를 위한 거래 금액을 계산하는 메서드
    private double calculateOtherPrice(List<Trade> tradeList, String cryptoSymbol) {
        // 전월의 첫 번째 날 계산 (예: 오늘이 10월 25일이면, 9월 1일을 계산)
        //LocalDate startDate = LocalDate.now().withDayOfMonth(1).minusMonths(1);
        LocalDate startDate = LocalDate.now();
        // 현재 달의 첫 번째 날 계산 (예: 10월 1일)
        //LocalDate endDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now().plusDays(1);
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

   /* private WalletHistory findLatestWalletInPreviousMonth(List<WalletHistory> walletHistoryList, String cryptoSymbol) {
        // 전월의 첫 번째 날 계산 (예: 오늘이 10월 25일이면, 9월 1일을 계산)
        LocalDate startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        // 현재 달의 첫 번째 날 계산 (예: 10월 1일)
        LocalDate endDate = LocalDate.now().withDayOfMonth(1);

        return walletHistoryList.stream()
                .filter(w -> w.getCryptoSymbol().equals(cryptoSymbol))
                .filter(w -> {
                    LocalDate modifiedDate = w.getModifiedAt().toLocalDate();
                    // 전월 첫째 날 이상, 당월 첫째 날 미만의 데이터를 필터링
                    return (modifiedDate.isEqual(startDate) || modifiedDate.isAfter(startDate))
                            && modifiedDate.isBefore(endDate);
                })
                .max(Comparator.comparing(WalletHistory::getModifiedAt))
                .orElse(null);
    }*/
    private WalletHistory findClosestThisMonthWallet(List<WalletHistory> walletHistoryList, String cryptoSymbol) {
        // 이번 달의 첫 번째 날
        //LocalDate firstDayOfCurrentMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate firstDayOfCurrentMonth = LocalDate.now().plusDays(1);

        return walletHistoryList.stream()
                .filter(w -> w.getCryptoSymbol().equals(cryptoSymbol))
                .filter(w -> w.getModifiedAt().toLocalDate().isBefore(firstDayOfCurrentMonth))
                .max(Comparator.comparing(WalletHistory::getModifiedAt)) // 이번달1일 이전의 가장 최신의 지갑찾기
                .orElse(null);
    }
    private WalletHistory findClosestLastMonthWallet(List<WalletHistory> walletHistoryList, String cryptoSymbol) {
        // 저번 달의 첫 번째 날
        //LocalDate firstDayOfCurrentMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate firstDayOfCurrentMonth = LocalDate.now();

        return walletHistoryList.stream()
                .filter(w -> w.getCryptoSymbol().equals(cryptoSymbol))
                /*.filter(w -> w.getModifiedAt().toLocalDate().isBefore(firstDayOfCurrentMonth))
                .max(Comparator.comparing(WalletHistory::getModifiedAt)) // 저번달1일 이전의 가장 최신의 지갑찾기*/
                .filter(w -> w.getModifiedAt().toLocalDate().equals(firstDayOfCurrentMonth))
                .min(Comparator.comparing(WalletHistory::getModifiedAt))
                .orElse(null);
    }

    // 두 지갑 기록을 비교하여 수익률을 계산하는 메서드
    private double calculateYieldPercentage(WalletHistory lastMonthWallet, WalletHistory nowWallet, double otherPrice) {
        double lastTotal = lastMonthWallet.getCash() + lastMonthWallet.getAmount() * lastMonthWallet.getCryptoPrice();
        double nowTotal = nowWallet.getCash() + nowWallet.getAmount() * nowWallet.getCryptoPrice();

        return ((nowTotal - lastTotal - otherPrice) / lastTotal) * 100;
    }

}

