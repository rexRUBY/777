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
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_name", columnList = "name"),
        @Index(name = "idx_user_id", columnList = "id")
})
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

    @Column
    private boolean processed=false;

    @Column(nullable = false, name = "user_status")
    private boolean userStatus = true; // 유저 상태 (true: 활성, false: 탈퇴)

    @OneToMany(mappedBy = "followingUser", cascade = CascadeType.ALL, orphanRemoval = true)
    // 나를 팔로우 하는사람들과의 팔로우목록, 내가 신청받는것
    // @BatchSize(size = 20)
    @Fetch(FetchMode.SUBSELECT)
    private List<Subscriptions> subscriptionsBeingFollowed = new ArrayList<>();

    @OneToMany(mappedBy = "followerUser", cascade = CascadeType.ALL, orphanRemoval = true)
    // 내가 팔로우 하는사람들과의 팔로우 목록, 내가 신청한것
    // @BatchSize(size = 10)
    @Fetch(FetchMode.SUBSELECT)
    private List<Subscriptions> subscriptionsIFollow = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    // @BatchSize(size = 10)
    @Fetch(FetchMode.SUBSELECT)
    private List<WalletHistory> walletHistoryList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    // @BatchSize(size = 10)
    @Fetch(FetchMode.SUBSELECT)
    private List<Trade> tradeList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    // @BatchSize(size = 10)
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 2)
    private List<Wallet> walletList = new ArrayList<>();

    // 생성자: 필수 필드만 포함
    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public User(long userId) {
        this.id=userId;
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

    public void changeProcess(){
        this.processed=true;
    }

    public void updateUser(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }
}