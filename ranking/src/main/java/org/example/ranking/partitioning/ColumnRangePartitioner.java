package org.example.ranking.partitioning;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class ColumnRangePartitioner implements Partitioner {

    private final String column;
    private final long minValue;
    private final long maxValue;
    private final int gridSize;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitionMap = new HashMap<>();

        // gridSize가 유효한지 확인하여 0으로 나누는 오류 방지
        if (gridSize <= 0 || minValue > maxValue) {
            throw new IllegalArgumentException("잘못된 gridSize 또는 범위 값입니다.");
        }

        long targetSize = (maxValue - minValue + 1) / gridSize;
        long start = minValue;

        for (int i = 0; i < gridSize; i++) {
            ExecutionContext context = new ExecutionContext();
            partitionMap.put("partition" + i, context);

            long end = (i == gridSize - 1) ? maxValue : start + targetSize - 1;

            context.putLong("minValue", start);
            context.putLong("maxValue", end);
            context.putString("column", column);

            start += targetSize; // 다음 파티션의 시작점 설정
        }

        return partitionMap;
    }
}
