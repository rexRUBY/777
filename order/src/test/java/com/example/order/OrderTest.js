import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '30s', target: 1000 }, // 100 명의 가상 사용자로 30초 동안 테스트
        { duration: '1m', target: 1000 },  // 1분 동안 100 명의 가상 사용자 유지
        { duration: '30s', target: 0 },   // 30초 동안 사용자를 감소시킴
    ],
};

// 랜덤한 값 생성 함수
function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

// tradeType을 랜덤으로 선택하는 함수
function getRandomTradeType() {
    const tradeTypes = ['BUY_ORDER', 'SELL_ORDER']; // 두 가지 tradeType 배열
    const randomIndex = getRandomInt(0, tradeTypes.length - 1); // 0 또는 1을 랜덤으로 선택
    return tradeTypes[randomIndex];
}

export default function () {
    const url = 'http://localhost:8082/test'; // API URL
    const totalRequests = 5; // 총 요청 수

    for (let i = 0; i < totalRequests; i++) {
        const userId = getRandomInt(12, 21); // 랜덤 userId 생성
        const tradeType = getRandomTradeType();
        const price = getRandomInt(80, 100); // 랜덤 price 생성
        const amount = getRandomInt(10, 20); // 랜덤 amount 생성
        const symbol = 'LPJ'; // 거래하는 심볼

        const payload = JSON.stringify({
            price,
            amount,
            userId,
            tradeType,
            symbol,
        });

        const params = {
            headers: {
                'Content-Type': 'application/json',
            },
        };

        // POST 요청 보내기
        const res = http.post(url, payload, params);

        // 응답 체크
        check(res, {
            'is status 200': (r) => r.status === 200,
        });

        // 요청 사이에 랜덤 지연 시간 추가 (1초 ~ 3초 사이)
        const delay = getRandomInt(1, 3);
        sleep(delay);
    }
}
