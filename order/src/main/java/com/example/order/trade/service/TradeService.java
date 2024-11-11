package com.example.order.trade.service;

import com.example.order.trade.entity.TradeLog;
import com.example.order.trade.repository.TradeLogRepository;
import org.springframework.stereotype.Service;

@Service
public class TradeService {

    private final TradeLogRepository tradeLogRepository;

    public TradeService(TradeLogRepository tradeLogRepository) {
        this.tradeLogRepository = tradeLogRepository;
    }

    public void saveLog(
            Long userId,
            Long oppositeUserId,
            double price,
            double requestAmount,
            String orderType,
            String symbol,
            String orderId,
            Number timestamp
    ) {
        TradeLog tradeLog = new TradeLog(
                orderId, userId, oppositeUserId, price, requestAmount, orderType, symbol, timestamp, "MATCHED"
        );
        tradeLogRepository.save(tradeLog);
    }
}
