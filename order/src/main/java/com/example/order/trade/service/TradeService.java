package com.example.order.trade.service;

import com.example.order.trade.entity.TradeLog;
import com.example.order.trade.repository.TradeLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeLogRepository tradeLogRepository;

    @Async
    public void saveLog(
            Long userId,
            Long oppositeUserId,
            double price,
            double requestAmount,
            String orderType,
            String symbol,
            String orderId,
            Number timestamp,
            String status
    ) {
        TradeLog tradeLog = new TradeLog(
                orderId, userId, oppositeUserId, price, requestAmount, orderType, symbol, timestamp, status
        );
        tradeLogRepository.save(tradeLog);
    }
}
