package org.example.common.webclient.service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoWebService {

    private final AmazonDynamoDB amazonDynamoDB;
    private final String tableName = "crypto"; // DynamoDB 테이블 이름

    /**
     * DynamoDB에서 암호화폐 가격을 조회하는 메서드
     *
     * @param coin 조회할 코인 이름 (e.g., "BTCUSDT")
     * @param date 조회할 날짜 (e.g., "2024-11-08")
     * @param time 조회할 시간 (e.g., "13:45:30")
     * @return 조회된 암호화폐 가격 (long 타입)
     */
    public Long getCryptoValueAsLong(String coin, String date, String time) {
        // 날짜와 시간을 Unix 타임스탬프로 변환
        long datetime = convertToUnixTimestamp(date, time);

        // DynamoDB 객체 생성 및 테이블 참조
        DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);
        Table table = dynamoDB.getTable(tableName);

        // 조회할 항목의 파티션 키와 정렬 키 설정
        GetItemSpec spec = new GetItemSpec()
                .withPrimaryKey("crypto_id", coin, "datetime", datetime);

        // 항목 조회
        Item item = table.getItem(spec);

        if (item == null) {
            log.error("No data found for " + coin + ", " + date + ":" + time);
            throw new RuntimeException("No data found for coin: " + coin + " at datetime: " + date + ":" + time);
        }

        // 결과 검증 및 반환
        try {
            Long price = item.getLong("price");
            log.info("Retrieved price for crypto_id: {} and datetime: {}: {}", coin, 8374917, price);
            return price;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number format for price in DynamoDB for coin: " + coin);
        }
    }

    private long convertToUnixTimestamp(String date, String time) {
        // 시간 형식 확인 및 DateTimeFormatter 설정
        DateTimeFormatter formatter = time.length() == 5
                ? DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                : DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        // date와 time 결합 후 파싱
        String dateTimeString = date + "T" + time;
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, formatter);

        return localDateTime.toEpochSecond(ZoneOffset.UTC);
    }
}