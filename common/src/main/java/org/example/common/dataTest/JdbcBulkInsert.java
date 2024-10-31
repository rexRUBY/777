//package org.example.common.dataTest;
//
//import java.sql.*;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ThreadLocalRandom;
//
//public class JdbcBulkInsert {
//    private static final String DB_URL = "jdbc:mysql://localhost:3306/seven1";
//    private static final String USER = "root";
//    private static final String PASSWORD = "Gcgc6899@";
//    private static final int USER_COUNT = 10000;
//    private static final int BATCH_SIZE = 1000;
//    private static final int THREAD_COUNT = 2;
//
//    public static void main(String[] args) {
//        insertUsersWithWallets();
//        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
//        executor.submit(JdbcBulkInsert::insertWalletHistory);
//        executor.submit(JdbcBulkInsert::insertSubscriptions);
//        executor.submit(JdbcBulkInsert::insertTrades);
//        executor.shutdown();
//    }
//
//    private static void insertUsersWithWallets() {
//        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
//            String userSql = "INSERT INTO users (name, password, email, user_status) VALUES (?, ?, ?, true)";
//            String walletSql = "INSERT INTO wallets (user_id, amount, crypto_symbol, crypto_price, cash) VALUES (?, ?, ?, ?, ?)";
//            conn.setAutoCommit(false);
//            try (PreparedStatement userPstmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
//                 PreparedStatement walletPstmt = conn.prepareStatement(walletSql)) {
//
//                for (int i = 1; i <= USER_COUNT; i++) {
//                    // User 데이터 삽입
//                    userPstmt.setString(1, "user" + i);
//                    userPstmt.setString(2, "password" + i);
//                    userPstmt.setString(3, "user" + i + "@example.com");
//                    userPstmt.addBatch();
//
//                    if (i % BATCH_SIZE == 0) {
//                        userPstmt.executeBatch();
//                        try (ResultSet generatedKeys = userPstmt.getGeneratedKeys()) {
//                            while (generatedKeys.next()) {
//                                long userId = generatedKeys.getLong(1);
//
//                                // Wallet 데이터 삽입 (각 사용자마다 BTC와 ETH)
//                                for (String symbol : new String[]{"BTC", "ETH"}) {
//                                    walletPstmt.setLong(1, userId);
//                                    walletPstmt.setDouble(2, Math.random() * 10);
//                                    walletPstmt.setString(3, symbol);
//                                    walletPstmt.setLong(4, symbol.equals("BTC") ? 30000L : 2000L);
//                                    walletPstmt.setLong(5, 1000000000L);
//                                    walletPstmt.addBatch();
//                                }
//                            }
//                        }
//                        walletPstmt.executeBatch();
//                        conn.commit();
//                    }
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void insertWalletHistory() {
//        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
//            String sql = "INSERT INTO wallethistory (user_id, amount, crypto_symbol, crypto_price, cash, created_at, modified_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
//            conn.setAutoCommit(false);
//            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//                for (int i = 1; i <= USER_COUNT; i++) {
//                    for (int j = 0; j < 5; j++) {
//                        pstmt.setLong(1, i);
//                        pstmt.setDouble(2, Math.random() * 10);
//                        String symbol = Math.random() > 0.5 ? "BTC" : "ETH";
//                        pstmt.setString(3, symbol);
//                        pstmt.setLong(4, symbol.equals("BTC") ? 30000L : 2000L);
//                        pstmt.setLong(5, (long) (Math.random() * 2000000));
//                        String date = getRandomDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//                        pstmt.setString(6, date);
//                        pstmt.setString(7, date);
//                        pstmt.addBatch();
//                    }
//                    if (i % BATCH_SIZE == 0) pstmt.executeBatch();
//                }
//                pstmt.executeBatch();
//                conn.commit();
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void insertSubscriptions() {
//        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
//            String sql = "INSERT INTO subscriptions (following_user_id, follower_user_id, crypto_id, crypto_amount, now_price, subscribe, created_at, modified_at) VALUES (?, ?, ?, ?, ?, 'ON', ?, ?)";
//            conn.setAutoCommit(false);
//            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//                for (int i = 1; i <= USER_COUNT; i++) {
//                    long followingUserId = (long) (Math.random() * USER_COUNT) + 1;
//                    long followerUserId = (long) (Math.random() * USER_COUNT) + 1;
//                    if (followerUserId == followingUserId) followerUserId = (followerUserId % USER_COUNT) + 1;
//                    long cryptoId = Math.random() > 0.5 ? 1 : 2;
//                    double cryptoAmount = Math.random() * 10;
//                    long nowPrice = cryptoId == 1 ? 30000L : 2000L;
//
//                    String date = getRandomDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//                    pstmt.setLong(1, followingUserId);
//                    pstmt.setLong(2, followerUserId);
//                    pstmt.setLong(3, cryptoId);
//                    pstmt.setDouble(4, cryptoAmount);
//                    pstmt.setLong(5, nowPrice);
//                    pstmt.setString(6, date);
//                    pstmt.setString(7, date);
//                    pstmt.addBatch();
//
//                    if (i % BATCH_SIZE == 0) pstmt.executeBatch();
//                }
//                pstmt.executeBatch();
//                conn.commit();
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void insertTrades() {
//        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
//            String sql = "INSERT INTO trade (user_id, crypto_id, trade_type, trade_for, amount, price, total_price, money_from, created_at, modified_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//            conn.setAutoCommit(false);
//            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//                for (int i = 1; i <= USER_COUNT; i++) {
//                    long userId = (long) (Math.random() * USER_COUNT) + 1;
//                    long cryptoId = Math.random() > 0.5 ? 1 : 2;
//                    String tradeFor = Math.random() > 0.5 ? "SELF" : "OTHER";
//                    String tradeType = "SELF".equals(tradeFor) ? (Math.random() > 0.5 ? "BUY" : "SELL") : "SELL";
//                    long moneyFrom = tradeFor.equals("SELF") ? userId : (long) (Math.random() * USER_COUNT) + 1;
//                    if (moneyFrom == userId) moneyFrom = (moneyFrom % USER_COUNT) + 1;
//
//                    double amount = Math.random() * 10;
//                    long price = cryptoId == 1 ? 30000L : 2000L;
//                    long totalPrice = (long) (price * amount);
//
//                    String date = getRandomDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//                    pstmt.setLong(1, userId);
//                    pstmt.setLong(2, cryptoId);
//                    pstmt.setString(3, tradeType);
//                    pstmt.setString(4, tradeFor);
//                    pstmt.setDouble(5, amount);
//                    pstmt.setLong(6, price);
//                    pstmt.setLong(7, totalPrice);
//                    pstmt.setLong(8, moneyFrom);
//                    pstmt.setString(9, date);
//                    pstmt.setString(10, date);
//                    pstmt.addBatch();
//
//                    if (i % BATCH_SIZE == 0) pstmt.executeBatch();
//                }
//                pstmt.executeBatch();
//                conn.commit();
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static LocalDate getRandomDate() {
//        LocalDate startDate = LocalDate.of(2024, 9, 1);
//        LocalDate endDate = LocalDate.of(2024, 10, 30);
//        long startEpochDay = startDate.toEpochDay();
//        long endEpochDay = endDate.toEpochDay();
//        long randomEpochDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay + 1);
//        return LocalDate.ofEpochDay(randomEpochDay);
//    }
//}
