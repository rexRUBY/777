package org.example.common.dataTest;

import org.example.common.user.entity.User;
import org.example.common.trade.entity.Trade;
import org.example.common.wallet.entity.WalletHistory;
import org.example.common.wallet.entity.Wallet;
import org.example.common.crypto.entity.Crypto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JdbcBulkInsert {

    private static final int BATCH_SIZE = 10000; // 배치 사이즈 설정
    private static final int THREAD_COUNT = 4; // 스레드 수 설정
    private static final int USER_COUNT = 1000000; // 사용자 수 설정
    private static final long INITIAL_CASH = 1000000000L; // 초기 캐시 금액

    public static void main(String[] args) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("your-persistence-unit");
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        for (int i = 0; i < USER_COUNT; i++) {
            final int userId = i + 1; // 사용자 ID

            executorService.submit(() -> {
                EntityManager entityManager = entityManagerFactory.createEntityManager();
                try {
                    User user = createUser(userId);
                    List<Wallet> wallets = createWallets(user);
                    List<Trade> trades = createTrades(user);
                    List<WalletHistory> walletHistories = createWalletHistories(user);

                    saveUserData(entityManager, user, wallets, trades, walletHistories);
                } catch (Exception e) {
                    e.printStackTrace(); // 예외 처리
                } finally {
                    entityManager.close(); // EntityManager는 사용 후 닫기
                }
            });
        }

        executorService.shutdown();
        while (!executorService.isTerminated()) {
            // 모든 스레드가 종료될 때까지 대기
        }

        entityManagerFactory.close(); // EntityManagerFactory는 모든 작업 후 닫기
    }

    private static User createUser(int userId) {
        String email = "user" + userId + "@example.com";
        String password = "password" + userId;
        String name = "User " + userId;
        return User.of(email, password, name);
    }

    private static List<Wallet> createWallets(User user) {
        List<Wallet> wallets = new ArrayList<>();
        wallets.add(new Wallet(user, 0.0, "BTC", INITIAL_CASH, INITIAL_CASH));
        wallets.add(new Wallet(user, 0.0, "ETH", INITIAL_CASH, INITIAL_CASH));
        user.getWalletList().addAll(wallets);
        return wallets;
    }

    private static List<Trade> createTrades(User user) {
        List<Trade> trades = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 100; i++) { // 예시로 100개의 거래를 생성
            String tradeFor = random.nextBoolean() ? "SELF" : "OTHER"; // 50% 확률로 SELF 또는 OTHER
            String tradeType = tradeFor.equals("OTHER") ? "SELL" : (random.nextBoolean() ? "BUY" : "SELL");
            double amount = random.nextDouble() * 100; // 랜덤 거래 수량
            long price = Math.abs(random.nextLong() % 10000); // 랜덤 가격 (음수 처리)
            long totalPrice = price * (long) amount; // 총 가격
            long moneyFrom = Math.abs(random.nextLong() % 100000); // 랜덤 자금 출처 (음수 처리)

            Trade trade = new Trade(user, getCryptoById(1), tradeType, tradeFor, amount, price, totalPrice, moneyFrom);
            trades.add(trade);
        }

        user.getTradeList().addAll(trades);
        return trades;
    }

    private static List<WalletHistory> createWalletHistories(User user) {
        List<WalletHistory> walletHistories = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) { // 예시로 10개의 지갑 기록을 생성
            double amount = random.nextDouble() * 1000; // 랜덤 금액
            String cryptoSymbol = random.nextBoolean() ? "BTC" : "ETH"; // 랜덤 암호화폐 기호
            long cryptoPrice = Math.abs(random.nextLong() % 10000); // 랜덤 가격 (음수 처리)
            long cash = Math.abs(random.nextLong() % 100000); // 랜덤 캐시 (음수 처리)

            WalletHistory walletHistory = new WalletHistory(amount, cryptoSymbol, cryptoPrice, cash, user);
            walletHistories.add(walletHistory);
        }
        user.getWalletHistoryList().addAll(walletHistories);
        return walletHistories;
    }

    private static Crypto getCryptoById(int cryptoId) {
        return new Crypto((long) cryptoId, cryptoId == 1 ? "BTC" : "ETH"); // ID에 따라 Crypto 객체 반환
    }

    private static void saveUserData(EntityManager entityManager, User user, List<Wallet> wallets, List<Trade> trades, List<WalletHistory> walletHistories) {
        entityManager.getTransaction().begin();
        entityManager.persist(user);

        for (int i = 0; i < wallets.size(); i++) {
            entityManager.persist(wallets.get(i));
            if (i > 0 && (i + 1) % BATCH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        for (int i = 0; i < trades.size(); i++) {
            entityManager.persist(trades.get(i));
            if (i > 0 && (i + 1) % BATCH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        for (int i = 0; i < walletHistories.size(); i++) {
            entityManager.persist(walletHistories.get(i));
            if (i > 0 && (i + 1) % BATCH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.getTransaction().commit();
    }
}
