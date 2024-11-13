package org.example.streaming.crypto.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderBookService {

    private final RedisTemplate<String, Object> redisTemplate;

    public Map<String, List<List<Object>>> getOrderBookData(String symbol) {

        String buyKey = symbol + "BUY_ORDER";
        String sellKey = symbol + "SELL_ORDER";

        Set<ZSetOperations.TypedTuple<Object>> buyOrders = redisTemplate.opsForZSet().reverseRangeWithScores(buyKey, 0, Long.MAX_VALUE);
        Set<ZSetOperations.TypedTuple<Object>> sellOrders = redisTemplate.opsForZSet().reverseRangeWithScores(sellKey, 0, Long.MAX_VALUE);

        Map<String, List<List<Object>>> result = new HashMap<>();

        List<List<Object>> buyLevels = processOrders(buyOrders);
        List<List<Object>> sellLevels = processOrders(sellOrders);

        result.put("BUY", buyLevels);
        result.put("SELL", sellLevels);

        return result;
    }

    private List<List<Object>> processOrders(Set<ZSetOperations.TypedTuple<Object>> orders) {
        Map<Double, Double> priceLevelSizeMap = new LinkedHashMap<>();
        Map<Double, Double> priceLevelAmountMap = new LinkedHashMap<>();
        double totalAmount = 0;
        double totalSize = 0;
        int priceCount = 0;

        for (ZSetOperations.TypedTuple<Object> order : orders) {
            double price = order.getScore();
            String orderId = (String) order.getValue();
            double size = Double.parseDouble((String) redisTemplate.opsForHash().get(orderId, "amount"));

            priceLevelSizeMap.put(price, priceLevelSizeMap.getOrDefault(price, 0.0) + size);
            priceLevelAmountMap.put(price, priceLevelAmountMap.getOrDefault(price, 0.0) + (price * size));

            if (!priceLevelSizeMap.containsKey(price)) {
                priceCount++;
                if (priceCount >= 20) break;
            }
        }

        List<List<Object>> levels = new ArrayList<>();

        for (Map.Entry<Double, Double> entry : priceLevelSizeMap.entrySet()) {
            double price = entry.getKey();
            double size = entry.getValue();
            totalSize += size;
            totalAmount += priceLevelAmountMap.get(price);

            double depth = totalAmount / totalSize;

            List<Object> level = new ArrayList<>();
            level.add(price);          // 가격
            level.add(size);           // 총량
            level.add(totalAmount);    // 총액
            level.add(depth);          // Depth

            levels.add(level);

            if (levels.size() >= 20) break;
        }

        return levels;
    }
}
