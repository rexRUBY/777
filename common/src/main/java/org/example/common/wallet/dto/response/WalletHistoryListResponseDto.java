package org.example.common.wallet.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class WalletHistoryListResponseDto {
    private List<WalletHistoryPageResponseDto> dataList;
    private int totalPages;
    private long totalElements;

    public WalletHistoryListResponseDto(List<WalletHistoryPageResponseDto> dataList, int totalPages, long totalElements) {
        this.dataList = dataList;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }
}
