package org.example.common.trade.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class TradeListResponseDto {
    private List<TradeResponsePageDto> dataList;
    private int totalPages;
    private long totalElements;

    public TradeListResponseDto(List<TradeResponsePageDto> dataList, int totalPages, long totalElements) {
        this.dataList = dataList;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }
}
