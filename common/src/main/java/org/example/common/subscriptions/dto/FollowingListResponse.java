package org.example.common.subscriptions.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class FollowingListResponse {
    private List<FollowingResponse> subscriptions;
    private int totalPages;
    private long totalElements;

    public FollowingListResponse(List<FollowingResponse> followingResponses, int totalPages, long totalElements) {
        this.subscriptions = followingResponses;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }
}
