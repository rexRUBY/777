package org.example.common.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.common.common.entity.Timestamped;
import org.example.common.subscriptions.entity.Subscriptions;
import org.example.common.trade.entity.Trade;
import org.example.common.wallet.entity.Wallet;
import org.example.common.wallet.entity.WalletHistory;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 256, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false,name = "user_status")
    private boolean userStatus = true; // 유저 상태 (true: 활성, false: 탈퇴)

    @OneToMany(mappedBy = "followingUser", cascade = CascadeType.ALL, orphanRemoval = true)//나를 팔로우 하는사람들과의 팔로우목록 // 내가 신청받는것
    @BatchSize(size = 20)
    private List<Subscriptions> subscriptionsBeingFollowed = new ArrayList<>();

    @OneToMany(mappedBy = "followerUser", cascade = CascadeType.ALL, orphanRemoval = true)//내가 팔로우 하는사람들과의 팔로우 목록 // 내가 신청한것
    @BatchSize(size = 20)
    private List<Subscriptions> subscriptionsIFollow = new ArrayList<>();

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private List<WalletHistory> walletHistoryList = new ArrayList<>();

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private List<Trade> tradeList = new ArrayList<>();

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Wallet> walletList = new ArrayList<>();

    // 생성자: 필수 필드만 포함
    private User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    // 정적 팩토리 메서드
    public static User of(String email, String password, String name) {
        return new User(email, password, name);
    }

    // 비밀번호 변경 기능
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    // 유저 탈퇴 처리
    public void withdrawUser() {
        this.userStatus = false;
    }
}