import { check } from 'k6';
import ws from 'k6/ws';
import { sleep } from 'k6';

// 랜덤 문자열을 생성하는 함수
function generateRandomString(length) {
  const characters = 'abcdefghijklmnopqrstuvwxyz';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += characters.charAt(Math.floor(Math.random() * characters.length));
  }
  return result;
}

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m', target: 50 },
    { duration: '30s', target: 0 },
  ],
};

export default function () {
  const baseUrl = 'ws://localhost:8083/ws';
  const counter = Math.floor(Math.random() * 1000);  // Counter 값, 필요에 따라 변경
  const sessionId = generateRandomString(8);  // 8자리 랜덤 문자열 생성
  const url = `${baseUrl}/${counter}/${sessionId}/websocket`;  // 동적 URL 구성

  const symbol = 'BTCUSDT';

  // WebSocket 연결
  const response = ws.connect(url, {}, function (socket) {
    socket.on('open', () => {
      console.log(`Connected to WebSocket server at ${url}`);

      // 서버에 심볼 구독 요청
      socket.send(JSON.stringify({ type: 'subscribe', symbol: symbol }));

      // 서버에서 메시지를 받을 때마다 실행
      socket.on('message', (message) => {
        console.log(`Received: ${message}`);

        // 메시지의 'price' 값이 존재하는지 확인
        const json = JSON.parse(message);
        check(json, {
          'message has price': (msg) => msg.price !== undefined,
          'price is a valid number': (msg) => !isNaN(parseFloat(msg.price)),
        });
      });

      // 에러 핸들링
      socket.on('error', (e) => {
        console.log(`WebSocket error: ${e.error()}`);
      });

      // 연결 종료 시 로그
      socket.on('close', () => {
        console.log('WebSocket connection closed');
      });

      // 30초 동안 대기
      sleep(30);

      // 연결 종료
      socket.close();
    });
  });

  // WebSocket 연결 응답 상태 확인 (101: Switching Protocols)
  check(response, {
    'status is 101': (r) => r && r.status === 101,
  });
}
