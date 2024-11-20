package com.example.order.order;

import com.example.order.order.dto.OrderMatchResult;
import com.example.order.trade.entity.TradeLog;
import com.example.order.trade.service.TradeService;
import com.example.order.wallet.WalletService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderMatchingService {
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WalletService walletService;
    private final TradeService tradeService;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper = new ObjectMapper();
    private final String BUY_ORDER_KEY = "BUY_ORDER";
    private final String SELL_ORDER_KEY = "SELL_ORDER";

    /* 주문이 들어옴 → 주문 체결 가능한지 확인 → 주문 체결 or OrderBook에 저장 */

    @KafkaListener(topics = "ORDER", groupId = "order-group", containerFactory = "kafkaListenerContainerFactory")
    public void orderListener(String message, Acknowledgment acknowledgment) {
        System.out.println("ORDER received: " + message);

        try {
            Map<String, Object> data = objectMapper.readValue(message, new TypeReference<Map<String, Object>>() {
            });
            Number id = (Number) data.get("userId");
            Number price = (Number) data.get("price");
            Number amount = (Number) data.get("amount");
            String tradeType = (String) data.get("tradeType");
            String stringPrice = price.toString();
            String stringAmount = amount.toString();
            Long userId = id.longValue();
            String symbol = (String) data.get("symbol");

            tryMatchOrder(stringPrice, stringAmount, userId, tradeType, symbol);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // status 0
    @Async
    public void tryMatchOrder(
            String price,
            String amount,
            Long userId,
            String orderType,
            String symbol
    ) {

        String oppositeOrderType = (orderType.equals(BUY_ORDER_KEY)) ? SELL_ORDER_KEY : BUY_ORDER_KEY;
        String orderKey = symbol + "_" + oppositeOrderType;

        double remainingAmount = Double.parseDouble(amount);

        OrderMatchResult result = new OrderMatchResult(remainingAmount, false);

        while(remainingAmount > 0) {
            Set<ZSetOperations.TypedTuple<Object>> oppositeOrders = redisTemplate
                    .opsForZSet()
                    .rangeByScoreWithScores(
                            orderKey,
                            Double.parseDouble(price),
                            Double.parseDouble(price)
                    );

            if (oppositeOrders != null && !oppositeOrders.isEmpty()) {
                // 가장 최근 주문 가져오기
                ZSetOperations.TypedTuple<Object> latestOppositeOrder = oppositeOrders.iterator().next();

                String oppositeOrderId = (String) latestOppositeOrder.getValue();
                String priceStr = (String) stringRedisTemplate.opsForHash().get(oppositeOrderId, "price");

//            log.info(orderKey + " orderId: " + oppositeOrderId);

                // 가격이 매칭되는지 확인
                if ((orderType.equals(BUY_ORDER_KEY) && Double.parseDouble(price) >= Double.parseDouble(priceStr)) ||
                        (orderType.equals(SELL_ORDER_KEY) && Double.parseDouble(priceStr) >= Double.parseDouble(price))) {

                     remainingAmount = processOrderMatch(
                            latestOppositeOrder,
                            remainingAmount,
                            Double.parseDouble(priceStr),
                            orderType,
                            userId,
                            symbol
                    );

                     result.setTradeStatus(true);

                    if(remainingAmount > 0) {
                        continue;
                    }
                }

            }

            if(result.getIsTradeStatus()) {
                sendAlarmMessage(userId, symbol, String.valueOf(Double.parseDouble(amount) - Double.parseDouble(String.valueOf(remainingAmount))), orderType);
            }

            // 매칭되지 않으면 새로운 주문 추가
//        log.info("매칭 결과 없음 " + orderType + " ORDER 추가 " + orderType + " Price: " + price + ", " + orderType + " Amount: " + amount);
            addOrder(price, String.valueOf(remainingAmount), orderType, userId, symbol);

            break;
        }
    }

    private double processOrderMatch(
            ZSetOperations.TypedTuple<Object> order,
            double requestAmount,
            double price,
            String orderType,
            Long userId,
            String symbol
    ) {
        String orderId = (String) order.getValue();
        double availableAmount = Double.parseDouble((String) stringRedisTemplate.opsForHash().get(orderId, "amount"));

        String timestampStr = (String) stringRedisTemplate.opsForHash().get(orderId, "timestamp");
        Double timestamp = Double.valueOf(timestampStr);

        String oppositeUserIdStr = (String) stringRedisTemplate.opsForHash().get(orderId, "userId");
        Long oppositeUserId = Long.valueOf(oppositeUserIdStr);
        String oppositeOrderType = (orderType.equals(BUY_ORDER_KEY)) ? SELL_ORDER_KEY : BUY_ORDER_KEY;

        if (availableAmount >= requestAmount) {
            stringRedisTemplate.opsForHash().put(orderId, "amount", String.valueOf(availableAmount - requestAmount));
//            log.info(availableAmount + "개 중 " + requestAmount + "개 처리됨 => " + (availableAmount - requestAmount) + "개 남음");

            processWalletUpdateAndSaveTradeLog(userId, oppositeUserId, price, availableAmount, orderType, symbol, orderId, timestamp);

            // 알림 전송
            sendAlarmMessage(userId, symbol, String.valueOf(requestAmount), orderType);
            sendAlarmMessage(oppositeUserId, symbol, String.valueOf(requestAmount), oppositeOrderType);

            return 0;
        } else {

            String orderKey = symbol + "_" + oppositeOrderType;

            redisTemplate.opsForZSet().remove(orderKey, orderId);

//            log.info(requestAmount + "개 중 " + availableAmount + "개 처리됨 => 주문 삭제 완료");

            processWalletUpdateAndSaveTradeLog(userId, oppositeUserId, price, availableAmount, orderType, symbol, orderId, timestamp);

            // 알림
            sendAlarmMessage(oppositeUserId, symbol, String.valueOf(availableAmount), oppositeOrderType);

//            log.info(restAmount + "개 추가 " + (orderType.equals(BUY_ORDER_KEY) ? "BUY" : "SELL") + " 매칭 시작");
            return requestAmount - availableAmount;
        }
    }

    public void addOrder(String stringPrice, String stringAmount, String orderKey, Long userId, String symbol) {

        long timestamp = System.currentTimeMillis();
        double doublePrice = Double.parseDouble(stringPrice);
        double score = doublePrice;
        String orderId = UUID.randomUUID().toString();
        String saveOrderKey = symbol + "_" + orderKey;

        redisTemplate.opsForZSet().add(saveOrderKey, orderId, score);
        redisTemplate.opsForHash().put(orderId, "price", stringPrice);
        redisTemplate.opsForHash().put(orderId, "amount", stringAmount);
        redisTemplate.opsForHash().put(orderId, "timestamp", String.valueOf(timestamp));
        redisTemplate.opsForHash().put(orderId, "userId", String.valueOf(userId));
    }

    public void sendAlarmMessage(Long userId, String symbol, String amount, String orderType) {
        kafkaTemplate.send("alarm-topic-" + userId,
                symbol + "가 " + amount + "개 " +
                        (orderType.equals(BUY_ORDER_KEY) ? "매수 " : "매도 ") + "체결되었습니다.");
//        log.info(userId + "의 " + symbol + "가 " + amount + "개 " +
//                (orderType.equals(BUY_ORDER_KEY) ? "매수 " : "매도 ") + "체결되었습니다.");
    }

    public void processWalletUpdateAndSaveTradeLog(
            Long userId,
            Long oppositeUserId,
            double price,
            double amount,
            String orderKey,
            String symbol,
            String orderId,
            Number timestamp
    ) {
        // Wallet 업데이트
        CompletableFuture<Void> walletUpdateFuture = CompletableFuture.runAsync(() -> {
            try {
                walletService.updateWallet(userId, oppositeUserId, price, amount, orderKey, symbol);
            } catch (Exception e) {
                tradeService.saveLog(userId, oppositeUserId, price, amount, orderKey, symbol, orderId, timestamp, "REJECTED");
                log.error("Wallet 업데이트 실패: userId={}, oppositeUserId={}, error={}", userId, oppositeUserId, e.getMessage());
                throw new RuntimeException("Wallet 업데이트 실패", e);
            }
        });

        // 로그 저장 비동기 처리
        CompletableFuture<Void> logSaveFuture = CompletableFuture.runAsync(() -> {
            try {
                tradeService.saveLog(userId, oppositeUserId, price, amount, orderKey, symbol, orderId, timestamp, "MATCHED");
            } catch (Exception e) {
                // 로그 저장 실패 시 Wallet 업데이트 롤백
                walletService.rollbackWalletUpdate(userId, oppositeUserId, price, amount, orderKey, symbol);
                throw e;
            }
        });

        try {
            CompletableFuture.allOf(walletUpdateFuture, logSaveFuture).join();
        } catch (Exception e) {
            log.error("전체 작업에서 예외 발생: {}", e.getMessage());
        }
    }
}
