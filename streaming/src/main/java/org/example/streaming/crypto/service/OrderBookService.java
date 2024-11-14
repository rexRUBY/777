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

        Set<ZSetOperations.TypedTuple<Object>> buyOrders = redisTemplate.opsForZSet().rangeByScoreWithScores(buyKey, 0, Double.MAX_VALUE);
        Set<ZSetOperations.TypedTuple<Object>> sellOrders = redisTemplate.opsForZSet().rangeByScoreWithScores(sellKey, 0, Double.MAX_VALUE);

        Map<String, List<List<Object>>> result = new HashMap<>();

        List<List<Object>> buyLevels = processOrders(buyOrders);
        List<List<Object>> sellLevels = processOrders(sellOrders);

        result.put("BUY", buyLevels);
        result.put("SELL", sellLevels);

        return result;

    }

    private List<List<Object>> processOrders(Set<ZSetOperations.TypedTuple<Object>> orders) {
        List<List<Object>> levels = new ArrayList<>();
        double totalAmount = 0;
        double totalSize = 0;

        for (ZSetOperations.TypedTuple<Object> order : orders) {
            String orderId = (String) order.getValue();
            double price = order.getScore();
            double size = Double.parseDouble((String) redisTemplate.opsForHash().get(orderId, "amount"));

            totalSize += size;
            totalAmount += price * size;

            double depth = totalAmount / totalSize;

            List<Object> level = new ArrayList<>();
            level.add(price);
            level.add(size);
            level.add(totalAmount);
            level.add(depth);

            levels.add(level);
        }

        return levels;
    }
}
