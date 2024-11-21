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

// 테스트 옵션 설정
export const options = {
  stages: [
    { duration: '10s', target: 10000 },  // 10초 동안 10,000명 가상 사용자
    { duration: '10s', target: 0 },      // 10초 동안 사용자 수를 0으로 감소시켜 연결 종료
  ],
};

export default function () {
  const baseUrl = 'ws://localhost:8083/ws';

  const counter = __VU; // 가상 사용자 번호를 counter로 사용
  const sessionId = generateRandomString(8);
  const url = `${baseUrl}/${counter}/${sessionId}/websocket`;

  const symbol = 'BTCUSDT'; // 구독할 심볼

  // WebSocket 연결
  const response = ws.connect(url, {}, function (socket) {
    socket.on('open', () => {
      console.log(`연결 성공!!! ${url}`);

      // 서버에서 전송한 메시지를 받을 때마다 실행
      socket.on('message', (message) => {
        console.log(`Received: ${message}`);

        try {
          // 서버에서 받은 메시지를 JSON으로 파싱하고 검증
          const json = JSON.parse(message);
          check(json, {
            'message has price': (msg) => msg.price !== undefined,
            'price is a valid number': (msg) => !isNaN(parseFloat(msg.price)),
          });
        } catch (error) {
          console.error('Error parsing message: ', error);
        }
      });

      // 에러 핸들링
      socket.on('error', (e) => {
        console.log(`WebSocket error: ${e.error()}`);
      });

      // 연결 종료 시 로그
      socket.on('close', () => {
        console.log('WebSocket connection closed');
      });

      // 10초 대기 후 연결 종료
      sleep(10);
      socket.close(); // 연결 종료
    });
  });

  // WebSocket 연결 응답 상태 확인 (101: Switching Protocols)
  check(response, {
    'status is 101': (r) => r && r.status === 101,
  });
}
