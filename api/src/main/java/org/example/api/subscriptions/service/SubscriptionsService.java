package org.example.api.subscriptions.service;


import lombok.RequiredArgsConstructor;
import org.example.common.common.dto.AuthUser;
import org.example.common.common.exception.InvalidRequestException;
import org.example.common.crypto.entity.Crypto;
import org.example.common.crypto.repository.CryptoRepository;
import org.example.common.subscriptions.dto.*;
import org.example.common.subscriptions.entity.Subscriptions;
import org.example.common.subscriptions.repository.SubscriptionsRepository;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionsService {

    private final SubscriptionsRepository subscriptionsRepository;
    private final UserRepository userRepository;
    private final CryptoRepository cryptoRepository;

    @Transactional
    public FollowingResponse subscribe(AuthUser authUser, FollowingRequest followingRequest) {

        Crypto crypto = cryptoRepository.findById(followingRequest.getCryptoId())
                .orElseThrow(() -> new InvalidRequestException("없는 코인입니다."));

        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new InvalidRequestException("없는 유저입니다."));

        User followingUser = userRepository.findById(followingRequest.getFollowingUserId())
                .orElseThrow(() -> new InvalidRequestException("없는 유저입니다."));

        Subscriptions subscriptions = Subscriptions.of(followingUser, user, crypto, followingRequest.getCryptoAmount());

        subscriptionsRepository.save(subscriptions);
        
        return new FollowingResponse(subscriptions.getFollowingUser().getName(), subscriptions.getCrypto().getSymbol());
    }

    public FollowingListResponse getFollowing(AuthUser authUser) {
        return new FollowingListResponse(
                subscriptionsRepository.findAllByFollowerUserId(authUser.getId())
                        .stream()
                        .map(f -> new FollowingResponse(f.getFollowingUser().getName(), f.getCrypto().getSymbol()))
                        .toList());
    }

    public FollowerListResponse getFollower(AuthUser authUser) {
        return new FollowerListResponse(
                subscriptionsRepository.findAllByFollowingUserId(authUser.getId())
                        .stream()
                        .map(f -> new FollowerResponse(f.getFollowerUser().getName(), f.getCrypto().getSymbol()))
                        .toList());
    }
}
