/*
package org.example.batch.partitioner;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class UserPartitioner implements Partitioner {

    private static final Logger logger = LoggerFactory.getLogger(UserPartitioner.class);
    private static final long TOTAL_USERS = 10000000; // 총 사용자 수

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitionMap = new HashMap<>();
        long partitionSize = TOTAL_USERS / gridSize;

        for (int i = 0; i < gridSize; i++) {
            long start = i * partitionSize + 1; // 시작 ID
            long end = (i == gridSize - 1) ? TOTAL_USERS : (i + 1) * partitionSize; // 끝 ID

            ExecutionContext context = new ExecutionContext();
            context.putLong("start", start);
            context.putLong("end", end);
            partitionMap.put("partition" + i, context);

            // 파티션 범위를 로깅
            logger.info("Partition {} -> start: {}, end: {}", i, start, end);
        }
        return partitionMap;
    }
}
*/
package org.example.batch.partitioner;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

public class UserPartitioner implements Partitioner {
    private final long totalUsers; // 전체 사용자 수

    public UserPartitioner(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitionMap = new HashMap<>();
        long partitionSize = totalUsers / gridSize;

        for (int i = 0; i < gridSize; i++) {
            long start = i * partitionSize + 1; // 시작 ID
            long end = (i == gridSize - 1) ? totalUsers : (i + 1) * partitionSize; // 끝 ID

            ExecutionContext context = new ExecutionContext();
            context.putLong("start", start);
            context.putLong("end", end);
            partitionMap.put("partition" + i, context);
        }
        return partitionMap;
    }
}
