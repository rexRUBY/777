# 777 코인 거래소

**목(Mock)돈 벌고 싶니?**  잘하는 사람에게 투자 전략을 구독하고 연습해보자!

777 코인 거래소는 가상 자산 거래에 관심은 있지만 경험이나 자금이 부족한 사용자들을 위한 거래 연습 플랫폼이다.

## 주요 기능

- 📈 **실시간 가격 그래프**  
  실제 코인 가격 데이터를 반영한 실시간 가격 그래프 제공으로 시장 흐름을 쉽게 파악 가능

- 📊 **구독 기반 투자 전략 학습**  
  성과가 입증된 투자 전문가를 구독하고, 자동 정산 기능을 통해 전략을 간편하게 학습할 수 있는 시스템  

- 🏆 **랭킹 시스템**  
  투자 성과를 기반으로 투자자 순위를 제공해, 사용자가 성공적인 투자자를 쉽게 찾고 구독할 수 있도록 지원

- ⚙️ **MSA 기반 멀티 모듈 시스템**  
  마이크로서비스 아키텍처(MSA)를 기반으로 멀티 모듈 시스템을 구축해, 유연하고 안정적인 운영이 가능

# 기술 스택

### 백엔드 API
<img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white">

### 배치 처리
<img src="https://img.shields.io/badge/SpringBatch-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white"> <img src="https://img.shields.io/badge/DynamoDB-4053D6?style=for-the-badge&logo=amazondynamodb&logoColor=white">

### 프론트엔드
<img src="https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=white">

### CI/CD 및 클라우드 인프라
<img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/AmazonECS-FF9900?style=for-the-badge&logo=amazonecs&logoColor=white"> <img src="https://img.shields.io/badge/AmazonFargate-262F3F?style=for-the-badge&logo=amazonecs&logoColor=white"> <img src="https://img.shields.io/badge/AmazonECR-FF9900?style=for-the-badge&logo=amazonecr&logoColor=white"> <img src="https://img.shields.io/badge/DynamoDB-4053D6?style=for-the-badge&logo=amazondynamodb&logoColor=white"> <img src="https://img.shields.io/badge/AmazonElastiCache-FF4F8B?style=for-the-badge&logo=redis&logoColor=white"> <img src="https://img.shields.io/badge/AWSLambda-FF9900?style=for-the-badge&logo=awslambda&logoColor=white"> <img src="https://img.shields.io/badge/EventBridge-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white"> <img src="https://img.shields.io/badge/AmazonS3-569A31?style=for-the-badge&logo=amazons3&logoColor=white">

## 인프라 구성

### 1. 코인 시세 저장 시스템
- **목적**: 정산 기능을 위한 코인 시세 데이터 저장
- **구성**: 서버리스 클라우드 환경 기반, 병렬 처리로 데이터 정합성 확보
- **배경**: OPEN API에서 과거 시세 데이터를 제공하지 않아 자체 구축

![코인 시세 저장 시스템](https://github.com/user-attachments/assets/4166cc25-2985-494c-8bcc-c6b606c88365)

### 2. CI/CD 및 배포 시스템
- **멀티모듈 MSA 배포**: 멀티모듈 MSA 환경에 최적화된 배포 시스템 구축
- **비용 절감**: ECR 자동 이미지 삭제 및 S3 백업 설정으로 비용 효율성 확보

![CI/CD 및 배포 시스템](https://github.com/user-attachments/assets/ffdf5376-0927-4da3-b791-2d1284addf5e)

### 3. 구독 정산 시스템
- **특정 시간별로 자동 정산**: lambda에서 api 호출을 통한 실시간 가격으로 자동 정산

![image](https://github.com/user-attachments/assets/7c39d853-5135-4936-88d9-cd104273bd37)
