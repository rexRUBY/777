package com.example.order.order;

import com.example.order.trade.service.TradeService;
import com.example.order.wallet.WalletService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class OrderMatchingService {
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    private final WalletService walletService;
    private final TradeService tradeService;

    private ObjectMapper objectMapper = new ObjectMapper();
    private final String BUY_ORDER_KEY = "BUY_ORDER";
    private final String SELL_ORDER_KEY = "SELL_ORDER";

    public OrderMatchingService(
            StringRedisTemplate stringRedisTemplate,
            RedisTemplate<String, Object> redisTemplate,
            RedissonClient redissonClient,
            WalletService walletService,
            TradeService tradeService
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
        this.walletService = walletService;
        this.tradeService = tradeService;
    }

    /* 주문이 들어옴 → 주문 체결 가능한지 확인 → 주문 체결 or OrderBook에 저장 */

    @KafkaListener(topics = "ORDER", groupId = "order-group")
    public void orderListener(String message) {
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    public void tryMatchOrder(
            String price,
            String amount,
            Long userId,
            String orderType,
            String symbol
    ) {
        String lockKey = symbol + orderType + "OrderLock:" + price;
        RLock lock = redissonClient.getLock(lockKey);

        lock.lock();

        try {
            String oppositeOrderType = (orderType.equals(BUY_ORDER_KEY)) ? SELL_ORDER_KEY : BUY_ORDER_KEY;
            String orderKey = symbol + oppositeOrderType;
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

                log.info(orderKey + " orderId: " + oppositeOrderId);

                // 가격이 매칭되는지 확인
                if ((orderType.equals(BUY_ORDER_KEY) && Double.parseDouble(price) >= Double.parseDouble(priceStr)) ||
                        (orderType.equals(SELL_ORDER_KEY) && Double.parseDouble(priceStr) >= Double.parseDouble(price))) {
                    processOrderMatch(latestOppositeOrder, amount, Double.parseDouble(priceStr), orderType, userId, symbol);
                    return;
                }
            }

            // 매칭되지 않으면 새로운 주문 추가
            log.info("매칭 결과 없음 " + orderType + " ORDER 추가 " + orderType + " Price: " + price + ", " + orderType + " Amount: " + amount);
            addOrder(price, amount, orderType, userId, symbol);
        } finally {
            lock.unlock();
        }
    }

    private void processOrderMatch(
            ZSetOperations.TypedTuple<Object> order,
            String amount,
            double price,
            String orderType,
            Long userId,
            String symbol
    ) {
        String orderId = (String) order.getValue();
        double availableAmount = Double.parseDouble((String) stringRedisTemplate.opsForHash().get(orderId, "amount"));
        double requestAmount = Double.parseDouble(amount);
        String timestampStr = (String) stringRedisTemplate.opsForHash().get(orderId, "timestamp");
        Double timestamp = Double.valueOf(timestampStr);

        log.info("requestAmount: " + requestAmount);
        log.info("availableAmount: " + availableAmount);

        String oppositeUserIdStr = (String) stringRedisTemplate.opsForHash().get(orderId, "userId");
        Long oppositeUserId = Long.valueOf(oppositeUserIdStr);

        if (availableAmount >= requestAmount) {
            stringRedisTemplate.opsForHash().put(orderId, "amount", String.valueOf(availableAmount - requestAmount));
            log.info(availableAmount + "개 중 " + requestAmount + "개 처리됨 => " + (availableAmount - requestAmount) + "개 남음");

            // 유저 지갑 업데이트
            walletService.updateWallet(userId, oppositeUserId, price, requestAmount, orderType, symbol);

            // 로그 생성
            tradeService.saveLog(userId, oppositeUserId, price, requestAmount, orderType, symbol, orderId, timestamp);
        } else {

            String oppositeOrderType = (orderType.equals(BUY_ORDER_KEY)) ? SELL_ORDER_KEY : BUY_ORDER_KEY;
            String orderKey = symbol + oppositeOrderType;

            log.info("orderType: " + orderType);
            log.info("orderKey: " + orderKey);

            redisTemplate.opsForZSet().remove(orderKey, orderId);

            log.info(requestAmount + "개 중 " + availableAmount + "개 처리됨 => 주문 삭제 완료");

            // 유저 지갑 업데이트
            walletService.updateWallet(userId, oppositeUserId, price, requestAmount, orderType, symbol);

            // 로그 생성
            tradeService.saveLog(userId, oppositeUserId, price, requestAmount, orderType, symbol, orderId, timestamp);

            String restAmount = String.valueOf(requestAmount - availableAmount);
            log.info(restAmount + "개 추가 " + (orderType.equals(BUY_ORDER_KEY) ? "BUY" : "SELL") + " 매칭 시작");

            tryMatchOrder(String.valueOf(price), restAmount, userId, orderType, symbol);
        }
    }

    public void addOrder(String stringPrice, String stringAmount, String orderKey, Long userId, String symbol) {

        long timestamp = System.currentTimeMillis();
        double doublePrice = Double.parseDouble(stringPrice);
        double score = doublePrice;
        String orderId = UUID.randomUUID().toString();
        String saveOrderKey = symbol + orderKey;

        redisTemplate.opsForZSet().add(saveOrderKey, orderId, score);
        redisTemplate.opsForHash().put(orderId, "price", stringPrice);
        redisTemplate.opsForHash().put(orderId, "amount", stringAmount);
        redisTemplate.opsForHash().put(orderId, "timestamp", String.valueOf(timestamp));
        redisTemplate.opsForHash().put(orderId, "userId", String.valueOf(userId));
    }
}
