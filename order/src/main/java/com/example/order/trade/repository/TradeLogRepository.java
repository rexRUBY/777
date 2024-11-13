package com.example.order.trade.repository;

import com.example.order.trade.entity.TradeLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TradeLogRepository extends MongoRepository<TradeLog, String> {
}