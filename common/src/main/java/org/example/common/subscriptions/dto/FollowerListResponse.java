package org.example.common.subscriptions.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class FollowerListResponse {
    private final List<FollowerResponse> subscriptions;
    private long totalElements;
    private int totalPages;

    public FollowerListResponse(List<FollowerResponse> followers, long totalElements, int totalPages) {
        this.subscriptions = followers;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }
}
