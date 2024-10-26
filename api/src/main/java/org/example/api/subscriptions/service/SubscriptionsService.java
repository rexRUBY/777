package org.example.api.subscriptions.service;


import lombok.RequiredArgsConstructor;
import org.example.common.auth.dto.request.UnFollowResponse;
import org.example.common.common.dto.AuthUser;
import org.example.common.common.exception.InvalidRequestException;
import org.example.common.crypto.entity.Crypto;
import org.example.common.crypto.repository.CryptoRepository;
import org.example.common.subscriptions.dto.*;
import org.example.common.subscriptions.entity.Billing;
import org.example.common.subscriptions.entity.Subscriptions;
import org.example.common.subscriptions.repository.BillingRepository;
import org.example.common.subscriptions.repository.SubscriptionsRepository;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.example.common.wallet.entity.Wallet;
import org.example.common.wallet.repository.WalletRepository;
import org.example.common.webclient.Util.DateTimeUtil;
import org.example.common.webclient.service.CryptoWebService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionsService {

    private final SubscriptionsRepository subscriptionsRepository;
    private final UserRepository userRepository;
    private final CryptoRepository cryptoRepository;
    private final WalletRepository walletRepository;
    private final CryptoWebService cryptoWebService;
    private final BillingRepository billingRepository;

    @Transactional
    public FollowingResponse subscribe(AuthUser authUser, FollowingRequest followingRequest) {

        Crypto crypto = cryptoRepository.findById(followingRequest.getCryptoId())
                .orElseThrow(() -> new InvalidRequestException("없는 코인입니다."));

        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new InvalidRequestException("없는 유저입니다."));

        if(user.getId().equals(followingRequest.getFollowingUserId())){
            throw new InvalidRequestException("you can't subscribe yourself");
        }

        User followingUser = userRepository.findById(followingRequest.getFollowingUserId())
                .orElseThrow(() -> new InvalidRequestException("없는 유저입니다."));

        Wallet userWallet = walletRepository.findByUserIdAndCryptoSymbol(user.getId(),crypto.getSymbol());

        if(userWallet.getAmount()< followingRequest.getCryptoAmount()){
            throw new InvalidRequestException("you don't have such amount of coin");
        }

        userWallet.minusCoin(followingRequest.getCryptoAmount());

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
    @Transactional
    public UnFollowResponse unFollowing(AuthUser authUser,long subscriptionsId) {
        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new InvalidRequestException("없는 유저입니다."));
        Subscriptions subscriptions = subscriptionsRepository.findById(subscriptionsId)
                .orElseThrow(() -> new InvalidRequestException("no such subscriptionId"));
        User followingUser = userRepository.findById(subscriptions.getFollowingUser().getId())
                .orElseThrow(() -> new InvalidRequestException("없는 유저입니다."));
        Wallet userWallet = walletRepository.findByUserIdAndCryptoSymbol(user.getId(),subscriptions.getCrypto().getSymbol());
        Wallet followingUserWallet = walletRepository.findByUserIdAndCryptoSymbol(followingUser.getId(),subscriptions.getCrypto().getSymbol());
        if(!subscriptions.getFollowerUser().equals(user)){
            throw new InvalidRequestException("that is not you're subscriptions");
        }
        Long price = cryptoWebService.getCryptoValueAsLong(subscriptions.getCrypto().getSymbol(), DateTimeUtil.getCurrentDate(),DateTimeUtil.getCurrentTime());
        Long totalPrice = (long)(subscriptions.getCryptoAmount()*price);
        userWallet.updateCash(totalPrice*0.9,price);
        followingUserWallet.updateCash(totalPrice*0.1,price);
        subscriptions.checkout(price);
        Billing billing = Billing.of(subscriptions);
        billingRepository.save(billing);
        subscriptionsRepository.delete(subscriptions);

        return new UnFollowResponse(followingUser.getEmail(), subscriptions.getCrypto().getSymbol(),subscriptions.getCryptoAmount(),totalPrice);
    }
}
