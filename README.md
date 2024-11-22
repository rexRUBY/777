## 서비스/프로젝트 소개

**"실전처럼 배우고, 전문가처럼 성장하세요"**

### 💫 실시간 시장의 박동을 느끼다

최첨단 실시간 차트 시스템으로 시장의 모든 움직임을 놓치지 않습니다. 우리만의 코인 'LPJ'로 실제 거래소의 생생한 경험을 제공합니다. 매수/매도 호가창의 긴장감 있는 숫자들 속에서, 당신의 투자 전략을 시험해보세요.

### 🌟 투자의 달인을 만나다

검증된 전문가들의 투자 비법을 직접 배워보세요. 랭킹 시스템을 통해 최고의 투자자를 발굴하고, 그들의 전략을 자동으로 학습할 수 있습니다. 더 이상 혼자만의 투자가 아닌, 거장들과 함께하는 여정이 시작됩니다.

### ⚡ 혁신적인 시스템, 완벽한 안정성

여러가지 서비스로 구현된 견고한 플랫폼이 24/7 완벽한 거래 환경을 보장합니다. 실시간 모니터링으로 당신의 투자는 언제나 안전합니다.

> *"단순한 모의투자가 아닌, 실전에 가장 가까운 트레이딩 플랫폼"*
> 

## 안전한 놀이터 777 : 거래 고수의 전략을 구독하고 코인 거래를 연습할 수 있는 플랫폼
![image](https://github.com/user-attachments/assets/5f8a49d6-8677-4c76-9e3e-74a7043429ef)
## KEY SUMMARY

### 💡 **랭킹 수익률 계산 소요 시간 (총 데이터 2000만 건 기준)**

**⚙️ 최적화 적용 전**

- **826분** 소요

**📈 최적화 과정**

|                **최적화 적용** |   **처리 시간** |  **개선율** |
| --- | --- | --- |
|    **파티셔닝 및 인덱싱 적용** |      632 분 |   23.5 % |
|           **서비스 로직 변경** |      203 분 |   75.4 % |
|            **Chunk Size 조정** |         91 분 |   89.0 % |

---

### 💡 **랭킹 계산 소요 시간 (총 데이터 2000만 건 기준)**

**⚙️ 최적화 적용 전**

- **580분** 소요


**📈 최적화 과정**

| **최적화 적용** | **처리 시간** | **개선율** |
| --- |------|---------|
| **파티셔닝 및 인덱싱 적용** | 280 분| 51.7 %  |
| **서비스 로직 변경** | 38 분 | 93.4 %  |
1. **파티셔닝 및 인덱싱 적용**→ **280분**으로 단축
2. **서비스 로직 변경**→ 최종 **38분**으로 개선

---

### 💡 호가창 

1. **Kafka**
- **비동기 처리** : **api서버(Producer)** → **order서버(Consumer)** 로 주문 데이터 전달
- **동시성 제어** : 가격 별로 Partition 키 지정 → 메시지 순서 보장 및 병렬 처리 가능
1. **데이터 구조**
- **Redis**의 **ZSet** 사용 → 동일한 가격에 대한 우선순위 정렬, 특정 가격대 주문 빠르게 조회 가능


**📈 최적화 과정**
  
1. **DB 업데이트 비동기 처리**
2. **Kafka 메시지 병렬 처리**

**가상 유저 1,000명이 주문 요청을 보내는 상황 가정**

- ### 최적화 결과 (표)
| 구분                     | 동기 처리   | 비동기 처리 | 비동기 처리 + 메시지 병렬 처리 | 개선율(%)
|--------------------------|-----------------|-------------------|-------------------------|----------------------|
| **요청 지연 시간(평균)**          | 3.91s           | 3.72s             | 3.13s                   |  19.9% |
| **처리 요청 수(초당)**        | 129.46/s          | 132.82/s           | 149.75/s                 | 15.7%  |
| **데이터 수신량(평균)**    | 6.9 MB (52 kB/s)             | 7.1 MB (53 kB/s)         | 7.9 MB (60 kB/s)      | 14.5% |

---
### 💡 WebSocket
1. **코인 가격 데이터 받아와 클라이언트에 전송하여 실시간 업데이트**
2. **호가창 데이터 클라이언트로 전송**

**📈 최적화 과정**
  
1. **DB 업데이트 비동기 처리**
2. **Kafka 메시지 병렬 처리**

**가상 유저 10,000명이 WebSocket 연결 시도, 연결 유지하는 상황 가정**

- ### 최적화 결과 (표)
| 구분                     | 기존 방식   | 배치 처리 | 멀티스레드 | 배치 처리 + 멀티스레드 | 개선율(%) 
|--------------------------|-----------------|-------------------|-------------------------|----------------------|---------------|
| **연결 성공**          | 62.83%  | 68.55%   | 73.88%           | 75.82%  | 20.64%  |
| **연결 시간 (평균)**        | 3.62s          | 2.24s          | 1.58s        | 0.25s  | 92.94% |
| **데이터 전송 시간(평균)**    | 5.63ms             | 1.3ms      | 1.52ms      | 745.95µs | 86.74% |

---

### 💡 CQRS **조회 속도 개선 (총 데이터 1000만 건 기준)**

**1. MongoDB와 MySQL의 역할 분리**

- **MySQL**: 쓰기 전용 모델(Command Model)로 사용하여 데이터의 정합성을 보장하며, 복잡한 트랜잭션 처리에 집중.
- **MongoDB**: 조회 전용 모델(Read Model)로 사용하여 데이터 비정규화와 캐싱을 통해 빠른 읽기 속도를 제공.

**2. Kafka를 통한 정합성 유지**

- 쓰기 모델(Command Model)에서 발생하는 데이터 변경 이벤트를 **Kafka**를 통해 읽기 모델(Read Model)로 실시간 동기화.
  - 이러한 아키텍처는 데이터 정합성을 유지하면서도 높은 읽기 성능을 확보.

| **구분** | **CQRS 도입 전** | **CQRS 도입 후** | **개선율** |
| --- | --- | --- | --- |
| **단일 조회 소요 시간** | 150ms | 10ms | 93.33% 개선 |
| **전체 조회 소요 시간** | 2300ms | 300ms | 86.96% 개선 |

---

## 💡 **로그 최적화 및 성능 향상 전략**
###  최적화 전략

### A. JSON 형식 도입
- **구조화된 데이터**: 로그를 JSON 키-값 구조로 변환하여 중복 정보와 공백을 제거.
- **압축 효율성**: JSON 키의 반복으로 인해 압축률이 증가하며, 로그 데이터 크기를 효과적으로 줄임.

### B. AOP 및 Logstash 필터링 적용
- **핵심 로그 선별**: AOP를 활용해 Request/Response/Exception과 같은 핵심 메서드에서만 로그 생성.
- **중요 데이터 필터링**: Logstash를 사용해 불필요한 필드를 제거하고, 핵심 정보만 Elasticsearch에 저장.

### C. 로그 레벨 필터링
- **로그 수준 조정**: INFO, WARN, ERROR 수준의 로그만 수집하며 디버깅 로그를 배제.
- **Filebeat 프로세서 활용**: 수집 로그를 필터링하여 데이터 양을 줄이고 효율적으로 저장.



### 최적화 결과 (표)
| 구분                     | 초기 단순 로직   | Filebeat 필터 적용 | AOP + Logstash 필터 적용 |
|--------------------------|-----------------|-------------------|-------------------------|
| **Docs Count**          | 4700           | 4100             | 4100                   |
| **Storage Size**        | 3.4MB          | 2.39MB           | 1.3MB                  |
| **변화율 (Storage)**    | -              | ↓ 29.7%          | ↓ 62%                  |

---

### 💡 알림 **성능 개선 (총 데이터 100만 건 기준)**
**프레임워크 변경**

- **Spring MVC** : 동기 방식의 프레임워크, 서블릿 기반에서 HTTP 요청을 처리하고, 모델-뷰-컨트롤러(MVC) 패턴을 따름.
- **Spring WebFlux** : 비동기 방식의 프레임워크, 리액티브 프로그래밍을 지원, 논블로킹 I/O를 통해 더 높은 성능과 확장성을 제공.

| **구분**             | **소요 시간** | **개선율** |
|--------------------|-----------|------|
| **Spring MVC**     | 972.47 ms | 0.0% |
| **Spring WebFlux** | 397.46 ms | 59.1% |

---

## 인프라 설계도
![image](https://github.com/user-attachments/assets/9708714f-62b6-445e-924d-4975ddecabae)


# 기술 스택

### 백엔드 API
<img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white"> <img src="https://img.shields.io/badge/Apache%20Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white">

### 배치 처리
<img src="https://img.shields.io/badge/SpringBatch-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white"> <img src="https://img.shields.io/badge/DynamoDB-4053D6?style=for-the-badge&logo=amazondynamodb&logoColor=white">

### 프론트엔드
<img src="https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=white">

### CI/CD 및 클라우드 인프라
<img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/AmazonECS-FF9900?style=for-the-badge&logo=amazonecs&logoColor=white"> <img src="https://img.shields.io/badge/AmazonFargate-262F3F?style=for-the-badge&logo=amazonecs&logoColor=white"> <img src="https://img.shields.io/badge/AmazonECR-FF9900?style=for-the-badge&logo=amazonecr&logoColor=white"> <img src="https://img.shields.io/badge/DynamoDB-4053D6?style=for-the-badge&logo=amazondynamodb&logoColor=white"> <img src="https://img.shields.io/badge/AmazonElastiCache-FF4F8B?style=for-the-badge&logo=redis&logoColor=white"> <img src="https://img.shields.io/badge/AWSLambda-FF9900?style=for-the-badge&logo=awslambda&logoColor=white"> <img src="https://img.shields.io/badge/EventBridge-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white"> <img src="https://img.shields.io/badge/AmazonS3-569A31?style=for-the-badge&logo=amazons3&logoColor=white">

### 모니터링
<img src="https://img.shields.io/badge/Filebeat-005571?style=for-the-badge&logo=elastic&logoColor=white"> <img src="https://img.shields.io/badge/ELKStack-005571?style=for-the-badge&logo=elasticstack&logoColor=white">


## 주요 기능

- **📈 실시간 가격 그래프**
    
    실제 코인 가격 데이터를 반영한 실시간 가격 그래프 제공으로 시장 흐름을 쉽게 파악 가능
    
- **📊 구독 기반 투자 전략 학습**
    
    성과가 입증된 투자 전문가를 구독하고, 자동 정산 기능을 통해 전략을 간편하게 학습할 수 있는 시스템
    
- **🏆 랭킹 시스템**
    
    투자 성과를 기반으로 투자자 순위를 제공해, 사용자가 성공적인 투자자를 쉽게 찾고 구독할 수 있도록 지원
    
- **⚙️ 멀티 모듈 시스템**
    
    모듈화 된 설계를 통해 기능 확장성과 시스템 안정성을 동시에 확보, 유연한 서비스 운영 가능
    
- **🪙 호가창 기반 거래**
    
    실제 코인 거래소와 같이 우리들의 거래로 가격이 결정되며,  매도 잔량, 매수 잔량을 확인할 수 있으며, 실시간 가격 그래프까지 확인할 수 있는 우리만의 코인 ‘LPJ’ 
    
- **🔍 모니터링**
보다 안전한 서비스를 위해, 실시간 모니터링으로 시스템 로그 모니터링


## 적용 기술

- Spring Batch : 대용량 데이터를 보다 빠르고, 안전하게 정산하기 위해 결정.
- AWS EventBridge/Lambda/DynamoDB : Open API에서 제공하지 않는 과거 코인 시세 데이터를 보완하며, 스트리밍 데이터와 실시간 정산 체계에 데이터를 전달
- Kafka : 실시간 대용량 데이터 처리가 용이하고, 데이터가 손실되면 안되기 때문에 결정.
- WebFlux : 고속 트래픽 처리, 실시간 데이터 처리, 동시 사용자 처리 성능을 최적화, 비동기 처리 및 실시간 데이터 스트리밍에 유리하기 때문
- Filebeat + ELK Stack : 각 모듈에서 로그 파일을 중앙 서버로 전송하고, 별도로 구현한 프로그램에서 수집된 로그를 처리 및 필터링

## 기술적 의사 결정

- **구독 정산 처리**
  - 실시간성을 중시하여 Lambda로 API 호출 방식을 채택. 실시간 가격을 DB에 저장하면서 동시에 API를 호출하여 정산을 처리함으로써 불필요한 쿼리문을 줄임.

- **ECR 이미지 관리**
  - Lambda와 boto3 라이브러리를 활용하여 ECR 이미지의 자동 백업 및 관리. 최신 5개 이미지만 유지하고 나머지는 S3에 백업하여 서버비 20% 절감.

- **로그 수집 구성 이유**
  - ELK Stack(Filebeat, Logstash, Elasticsearch, Kibana) 도입으로 분산된 모듈의 로그 데이터를 중앙 집중화하여 실시간 모니터링 및 장애 추적을 용이하게 구성.
  - Filebeat로 경량 로그 수집, Logstash로 필터링 및 구조화, Elasticsearch로 빠른 저장 및 검색, Kibana로 시각화하여 운영 효율성 향상.

- **장애 대응 계획**
  - **로그 백업 및 복구**: Filebeat 내부 큐와 Elasticsearch 스냅샷 기능을 활용해 장애 시 데이터 손실 방지.
  - **고가용성 구성**: Elasticsearch 다중 노드 클러스터와 Logstash/Filebeat의 재시도 기능으로 시스템의 안정성을 높임.

- **알림 실시간 통신**
  - 단방향 실시간 통신에는 SSE(Server-Sent Events)를 적용. TAT 기한이 짧았기에 빠른 구현이 가능한 SSE 사용. WebSocket은 많은 클라이언트의 대규모 실시간 알림 시스템에 적합하지만, 개발 시간상의 제한으로 SSE를 채택.

- **실시간 코인 가격 데이터 전송**
  - **WebSocket vs SSE**: 높은 빈도와 성능을 요하는 대규모 실시간 데이터 전송에는 WebSocket이 적합. 따라서, 성능 요구에 따라 WebSocket을 채택하여 실시간 코인 가격 데이터 전송을 구현.


## 트러블슈팅

[Ranking 최적화과정](https://www.notion.so/Ranking-131578ba79f28068b233f1de49e71a92?pvs=21) 

[batch 동시성 제어에 대한 대책](https://www.notion.so/batch-794fe3978c7e424db6eeadd50ca4991d?pvs=21) 

[VPC 내부 리소스 접근 구성(feat. 람다 외부통신)](https://www.notion.so/VPC-feat-bc92ee204a3d485183c979ad76e793c3?pvs=21) 

[로그 성능 향상](https://www.notion.so/dcfa73e364ce4469bba2812eab40755a?pvs=21) 

[호가창 동시성 제어](https://www.notion.so/9a98618ee31f4331958704428ccb8f01?pvs=21) 

[호가창 동시성 제어2](https://www.notion.so/2-3e7074fb4e654c37a21d0962344916ad?pvs=21)


### CONTRIBUTORS

| 역할              | 주요 기여 내용                                     | 주요 기여자     | 협업 기여자                   |
|-----------------|----------------------------------------------|---------------|----------------------------|
| **배포**          | 멀티모듈 시스템의 개별 배포 및 관리                         | 박예서        | 정승헌, 이상민, 박철희, 전현욱 |
| **비용 최적화**      | 백업 및 이미지 관리 시스템 구축                           | 박예서        | 정승헌, 이상민, 박철희, 전현욱 |
| **인프라 설계**      | 코인 시세 조회 및 백업 인프라 설계                         | 박예서        | 정승헌, 이상민, 박철희, 전현욱 |
| **조회 성능 개선**    | 조회 성능 개선을 위한 CQRS 패턴 도입                      | 박예서        | 정승헌, 이상민, 박철희, 전현욱 |
| **알림 서비스 구축**   | 실시간 알림 시스템 구축                                | 박철희        | 이상민, 박예서, 전현욱, 정승헌 |
| **시스템 안정성 강화**  | 비동기 방식 및 메시지 브로커를 활용한 안정성 강화                 | 박철희        | 이상민, 박예서, 전현욱, 정승헌 |
| **모니터링 시스템 구현** | Prometheus 및 로그 수집 시스템 설계                    | 이상민        | 박철희, 전현욱, 정승헌, 박예서 |
| **로그 데이터 최적화**  | Filebeat + Logstash Filter를 활용한 로그 데이터 용량 축소 | 이상민        | 박철희, 전현욱, 정승헌, 박예서 |
| **주문 처리 효율화**   | 대용량 주문 데이터 비동기 처리 시스템 구축                     | 전현욱        | 박예서, 정승헌, 박철희, 이상민 |
| **실시간 데이터 전송**  | WebSocket 기반 Streaming 서버 설계                 | 전현욱        | 박예서, 정승헌, 박철희, 이상민 |
| **정산 최적화**      | Spring Batch를 통한 대규모 데이터 정산 최적화              | 정승헌        | 전현욱, 박예서, 박철희, 이상민 |
| **실시간 정산 시스템**  | EventBridge와 Lambda를 사용한 실시간 정산 최적화          | 정승헌        | 전현욱, 박예서, 박철희, 이상민 |

### Github Links

- [정승헌](https://github.com/wjdtmdgjs-1)
- [박예서](http://github.com/rexRUBY)
- [박철희](https://github.com/ironshine)
- [이상민](https://github.com/Sangmin1999)
- [전현욱](https://github.com/jhwook)
![Chart](https://btc-price-widget.vercel.app/api/charts?theme=summer)
![Chart](https://btc-price-widget.vercel.app/api/charts?coin=eth)