package org.example.common.dataTest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

public class JdbcBulkInsert {
    private static final String URL = "jdbc:mysql://localhost:3306/seven";
    private static final String USER = "root";
    private static final String PASSWORD = "gjsl12399!";

    public static void main(String[] args) {
//        int tradeRecords = 2000000;          // trade 테이블의 총 레코드 수
        int walletHistoryRecords = 400000;   // wallethistory 테이블의 총 레코드 수

//        updateDatesForTables("trade", tradeRecords);
        updateDatesForTables("wallethistory", walletHistoryRecords);
    }


    public static void updateDatesForTables(String tableName, int totalRecords) {
        String updateQuery = "UPDATE " + tableName + " SET created_at = ?, modified_at = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            for (int i = 1; i <= totalRecords; i++) {
                LocalDateTime randomDateTime = getRandomDateTime();

                pstmt.setObject(1, randomDateTime);
                pstmt.setObject(2, randomDateTime);
                pstmt.setInt(3, i); // 각 레코드의 id에 맞게 수정

                pstmt.addBatch();

                if (i % 1000 == 0) { // 배치 단위로 실행
                    pstmt.executeBatch();
                    pstmt.clearBatch();
                }
            }
            pstmt.executeBatch(); // 남은 배치 실행
            System.out.println("Updated dates for " + tableName + " table.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static LocalDateTime getRandomDateTime() {
        LocalDateTime startDate = LocalDateTime.of(2024, Month.SEPTEMBER, 30, 23, 59);
        LocalDateTime endDate = LocalDateTime.of(2024, Month.OCTOBER, 31, 23, 59);

        long startEpoch = startDate.toEpochSecond(java.time.ZoneOffset.UTC);
        long endEpoch = endDate.toEpochSecond(java.time.ZoneOffset.UTC);

        long randomEpoch = ThreadLocalRandom.current().nextLong(startEpoch, endEpoch);
        return LocalDateTime.ofEpochSecond(randomEpoch, 0, java.time.ZoneOffset.UTC);
    }
}
