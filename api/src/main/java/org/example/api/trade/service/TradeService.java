package org.example.api.trade.service;


import lombok.RequiredArgsConstructor;
import org.example.common.common.dto.AuthUser;
import org.example.common.common.exception.InvalidRequestException;
import org.example.common.crypto.entity.Crypto;
import org.example.common.crypto.repository.CryptoRepository;
import org.example.common.subscriptions.entity.Billing;
import org.example.common.subscriptions.entity.Subscriptions;
import org.example.common.subscriptions.repository.BillingRepository;
import org.example.common.subscriptions.repository.SubscriptionsRepository;
import org.example.common.trade.dto.request.TradeRequestDto;
import org.example.common.trade.dto.response.TradeResponseDto;
import org.example.common.trade.entity.Trade;
import org.example.common.trade.enums.TradeFor;
import org.example.common.trade.enums.TradeType;
import org.example.common.trade.repository.TradeRepository;
import org.example.common.user.entity.User;
import org.example.common.user.repository.UserRepository;
import org.example.common.wallet.entity.Wallet;
import org.example.common.wallet.entity.WalletHistory;
import org.example.common.wallet.repository.WalletHistoryRepository;
import org.example.common.wallet.repository.WalletRepository;
import org.example.common.webclient.util.DateTimeUtil;
import org.example.common.webclient.service.CryptoWebService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeService {
    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;
    private final CryptoRepository cryptoRepository;
    private final WalletRepository walletRepository;
    private final BillingRepository billingRepository;
    private final WalletHistoryRepository walletHistoryRepository;
    private final SubscriptionsRepository subscriptionsRepository;
    private final CryptoWebService cryptoWebService;


    @Transactional
    public TradeResponseDto postTrade(AuthUser authUser, long cryptoId, TradeRequestDto tradeRequestDto) {
        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new InvalidRequestException("no such user"));
        Crypto crypto = cryptoRepository.findById(cryptoId).orElseThrow(()->new NullPointerException("no such crypto"));

        Long price = cryptoWebService.getCryptoValueAsLong(crypto.getSymbol(),DateTimeUtil.getCurrentDate(),DateTimeUtil.getCurrentTime());

        Wallet wallet = walletRepository.findByUserIdAndCryptoSymbol(user.getId(),crypto.getSymbol());
        if(tradeRequestDto.getTradeType().equals(TradeType.Authority.BUY)){
            if(wallet.getCash() < price*tradeRequestDto.getAmount()) {
                throw new InvalidRequestException("no such money");
            }
            wallet.update(wallet.getAmount() + tradeRequestDto.getAmount(),
                    wallet.getCash() - (long)(price * tradeRequestDto.getAmount()),price);
            WalletHistory walletHistory = new WalletHistory(wallet);
            walletHistoryRepository.save(walletHistory);
        }else if(tradeRequestDto.getTradeType().equals(TradeType.Authority.SELL)){
            if(wallet.getAmount() < tradeRequestDto.getAmount()){
                throw new InvalidRequestException("no such amount");
            }
            wallet.update((wallet.getAmount() - tradeRequestDto.getAmount()),
                    wallet.getCash() + (long)(price * tradeRequestDto.getAmount()),price);
            WalletHistory walletHistory = new WalletHistory(wallet);
            walletHistoryRepository.save(walletHistory);
        }
        Trade trade = new Trade(user,crypto,tradeRequestDto.getTradeType(),tradeRequestDto.getTradeFor(),tradeRequestDto.getAmount(),price,(long)(price * tradeRequestDto.getAmount()),user.getId());
        tradeRepository.save(trade);
        return new TradeResponseDto(crypto.getSymbol(),tradeRequestDto.getAmount(),tradeRequestDto.getTradeType(),(long)(price * tradeRequestDto.getAmount()));
    }

    @Transactional
    public TradeResponseDto postSubscriptionsTrade(AuthUser authUser, long cryptoId, long subscritionsId, TradeRequestDto tradeRequestDto) {

        //authuser user subscription followinguser 일치하는지확인
        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new InvalidRequestException("no such user"));

        Crypto crypto = cryptoRepository.findById(cryptoId)
                .orElseThrow(()->new NullPointerException("no such crypto"));

        Subscriptions subscriptions = subscriptionsRepository.findById(subscritionsId)
                .orElseThrow(() -> new InvalidRequestException("no such subscriptions"));

        if (!subscriptions.getFollowingUser().getId().equals(user.getId())) {
            throw new InvalidRequestException("different user");
        }
        if(!subscriptions.getCrypto().getId().equals(cryptoId)){
            throw new InvalidRequestException("different crypto");
        }

        //subscription 찾아와서 코인 종류 뽑고 tradeRequest에서 코인갯수체크(갯수보다많으면 throw)/ type=sell로 통일 으로
        Long price = cryptoWebService.getCryptoValueAsLong(crypto.getSymbol(),DateTimeUtil.getCurrentDate(),DateTimeUtil.getCurrentTime());
        Long totalPrice = (long)(price * tradeRequestDto.getAmount());

        Wallet userWallet = walletRepository.findByUserIdAndCryptoSymbol(user.getId(),crypto.getSymbol());
        Wallet followerWallet = walletRepository.findByUserIdAndCryptoSymbol(subscriptions.getFollowerUser().getId(),crypto.getSymbol());

        if (tradeRequestDto.getAmount().equals(subscriptions.getCryptoAmount())) {
            Trade trade = new Trade(user,crypto, TradeType.Authority.SELL, TradeFor.Authority.OTHER, subscriptions.getCryptoAmount(),price,(long)(price * tradeRequestDto.getAmount()),subscriptions.getFollowerUser().getId());
            userWallet.updateCash(totalPrice*0.1,price);
            followerWallet.updateCash(totalPrice*0.9,price);
            tradeRepository.save(trade);
            subscriptions.checkout(price);
            Billing billing = Billing.of(subscriptions);
            billingRepository.save(billing);
            subscriptionsRepository.delete(subscriptions);

            WalletHistory walletHistory = new WalletHistory(userWallet);
            WalletHistory walletHistory1 = new WalletHistory(followerWallet);

            walletHistoryRepository.save(walletHistory);
            walletHistoryRepository.save(walletHistory1);
        }
        else {
            throw new InvalidRequestException("write same amount");
        }

        return new TradeResponseDto(crypto.getSymbol(),tradeRequestDto.getAmount(), TradeType.Authority.SELL,price);
    }

    public List<TradeResponseDto> getTradeList(AuthUser authUser, long cryptoId) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(()->new NullPointerException("no such user"));
        Crypto crypto = cryptoRepository.findById(cryptoId).orElseThrow(()->new NullPointerException("no such crypto"));
        List<Trade> tradeList = tradeRepository.findAllByCryptoAndUser(crypto,user);

        return tradeList.stream().map(Trade->new TradeResponseDto(Trade.getCrypto().getSymbol(),Trade.getAmount(),String.valueOf(Trade.getTradeType()),Trade.getPrice())).toList();
    }


    public List<TradeResponseDto> getAllTradeList(AuthUser authUser) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(()->new NullPointerException("no such user"));
        List<Trade> tradeList = tradeRepository.findAllByUser(user);

        return tradeList.stream().map(Trade->new TradeResponseDto(Trade.getCrypto().getSymbol(),Trade.getAmount(),String.valueOf(Trade.getTradeType()),Trade.getPrice())).toList();
    }
}
