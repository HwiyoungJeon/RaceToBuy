# RaceToBuy 🏁

## 📺 프로젝트 소개

**RaceToBuy**는 대규모 이커머스 환경에서 **블랙프라이데이**와 같은 대규모 할인 이벤트 시 발생하는 **동시성 문제**를 체험하고, 이를 해결할 수 있는 **MSA(Microservices Architecture)** 기반 구조를 설계한 프로젝트입니다. 

이 프로젝트는 다음과 같은 목표를 가지고 있습니다:
- 대규모 트래픽 환경에서의 안정적인 주문 및 결제 처리.
- 확장성과 유연성을 고려한 MSA 기반 설계.
- 동시성 문제를 해결하기 위한 Redis 및 메시징 시스템 적용.

---

## 🔥 서비스 핵심 기능
### 1. 안정적인 주문 및 결제 처리
- 주문 생성, 조회, 취소 및 반품 기능 제공.
- Redis를 활용하여 주문 데이터를 캐싱하고 타임아웃 관리.
- 결제 처리 및 상태 관리를 통해 사용자 경험 개선.

#### 유저 플로우
(이미지 넣을 예정)
1. 사용자가 상품을 선택한 후 결제 요청을 보냅니다.
2. 주문 요청이 **Order Service**로 전달되어 주문 데이터가 저장됩니다.
3. **Payment Service**가 결제를 처리하고 성공/실패 여부를 반환합니다.

---

### 2. 상품 및 이벤트 관리
- 상품 정보 조회 및 재고 관리.
- Redis Lua 스크립트를 활용하여 동시성 문제 해결.
- 이벤트 생성 및 종료 관리.

#### 유저 플로우
(이미지 넣을 예정)
1. 상품 정보가 **Product Service**를 통해 DB 및 캐시로부터 로드됩니다.
2. 이벤트 상품의 할인율 및 종료 여부를 확인하여 사용자에게 표시됩니다.

---

### 3. 사용자 인증 및 보안 강화
- JWT 기반 인증 및 Refresh Token 관리.
- Redis를 활용한 블랙리스트 관리.
- 이메일 인증 및 비밀번호 변경 기능.

#### 유저 플로우
(이미지 넣을 예정)
1. 사용자가 회원가입 요청을 보냅니다.
2. **Auth Service**가 이메일 인증 및 JWT 발급을 처리합니다.
3. 로그인된 사용자는 Refresh Token을 활용하여 세션을 유지합니다.

#### 부가 기능

- 다중 기기에서 로그아웃 처리.
- 비밀번호 변경시 모든 기기 로그아웃(보안강화)
---

## 💡 기술 스택 및 기술적 의사 결정
### 프레임워크
- **Spring WebFlux**
  - **비동기 처리**: 비동기 방식으로 요청을 처리하여 높은 처리량을 지원하며, 응답 속도를 개선합니다.
  - **높은 처리량**: 대량의 데이터를 실시간으로 처리하며 WebFlux의 논 블로킹 I/O로 높은 효율성을 제공합니다.
  - **적합성**: 라이브 스트리밍, 비동기 작업이 많은 데이터 요청 환경에 최적화되어 있습니다.

### 운영 환경
- **Docker**
  - **MSA 구현**: AWS EC2 환경에서 Docker를 사용하여 독립적인 3개의 컨테이너를 실행.
    - Content Service, Transcoding Service, Redis
  - **모니터링 도구**: K6, Grafana를 사용하여 부하 테스트 및 성능 모니터링 수행.

### 데이터베이스
- **MySQL**
  - **신뢰성과 성능**: 관계형 데이터베이스로서 주문, 결제와 같은 핵심 데이터를 안정적으로 관리.
- **Redis**
  - **결제 관리**: 빠른 트랜잭션 처리와 데이터 일관성을 유지하기 위해 사용.
  - **재고 관리**: 재고 변경 작업을 원자적으로 처리하여 데이터 충돌 방지.
  - **Refresh Token 관리**: 토큰 저장 및 빠른 인증 확인.

### 개발 도구
- **IntelliJ IDEA**
  - **주요 기능**: 코드 작성, 디버깅, 테스트 통합 지원.
  - **효율성**: Spring Boot와 Gradle 프로젝트에 최적화된 개발 환경 제공.
- **Postman**
  - **API 테스트**: 다양한 요청 시뮬레이션과 응답 검증.
  - **문서화**: 팀 협업을 위한 API 요청과 응답 샘플 공유.
- **Git**
  - **버전 관리**: 분산 버전 관리 시스템으로 코드 히스토리 관리.
  - **협업**: 브랜치와 풀 리퀘스트를 활용하여 팀원 간 효율적인 협업.
- **Gradle**
  - **의존성 관리**: 프로젝트 빌드 및 라이브러리 의존성 자동 관리.
  - **확장성**: 멀티 모듈 프로젝트에 적합한 빌드 시스템 제공.
---

## 🔧 서버 아키텍처
![image](https://github.com/user-attachments/assets/9c5033f8-af69-4e39-9b92-7bc3d5ecfc1b)


```plaintext
├── eureka-server
├── api-gateway
├── commons
├── order-service
├── product-service
└── user-service
```
- **eureka-server**: 서비스 디스커버리와 로드 밸런싱 제공.
- **api-gateway**: 모든 서비스의 진입점으로, 인증 및 요청 라우팅 담당.
- **commons**: 공통 유틸리티와 DTO 관리.
- **order-service**: 주문 처리 및 결제 상태 관리.
- **product-service**: 상품 정보와 재고 관리.
- **user-service**: 회원 관리 및 인증.

---

## ⛔ 트러블 슈팅

1. **Redis Lua 스크립트를 활용한 재고 관리 동시성 문제 해결**

   - 문제: 대규모 트래픽으로 인해 상품 재고 데이터가 불일치하는 문제가 발생.
   - 해결: Redis Lua 스크립트를 사용해 원자적 연산으로 재고를 관리.

2. **이벤트 트래픽 분산 처리**

   - 문제: 이벤트 상품 조회 시 발생하는 대량의 API 호출로 서버 과부하 발생.
   - 해결: 캐싱과 비동기 메시징 시스템(Kafka, RabbitMQ)을 도입하여 부하를 분산.

3. **JWT 기반 인증과 Refresh Token 관리**

   - 문제: Refresh Token 탈취 가능성 및 블랙리스트 관리 이슈.
   - 해결: Redis를 사용해 블랙리스트를 관리하고 만료 기간을 동적으로 설정.

---

## 🚀 성능 개선

1. **Redis 캐싱을 활용한 데이터 접근 속도 향상**
- **개선점**: MySQL에 대한 빈번한 조회를 Redis로 이전하여 데이터 접근 시간을 단축.
- **결과**: 데이터 조회 속도가 평균 50% 이상 향상.


2. **비동기 프로세싱 도입**
- **개선점**: 주문 생성 및 결제 요청을 비동기 처리하여 서비스 응답 시간을 단축.
- **결과**: TPS(Transactions Per Second)가 기존 대비 30% 증가.

3. **데이터베이스 인덱싱 최적화**
- **개선점**: 주요 테이블(Order, Product)에 필요한 컬럼에 인덱스를 추가하여 조회 성능 향상.
- **결과**: 복잡한 쿼리의 실행 시간이 평균 40% 감소.

4 **동적 스케일링 도입**
- **개선점**: AWS Auto Scaling Group을 활용하여 트래픽 증가 시 서비스 인스턴스를 자동 확장.
- **결과**: 트래픽 급증 상황에서도 안정적인 서비스 제공.

---
## 🗺️ 로드맵

### 1. 단기 계획
- [ ] **Redis 캐싱 최적화**: 인기 상품 조회 시 Redis에 캐싱된 데이터를 활용하도록 구현 완료.
- [ ] **비동기 처리 도입**: 주문 생성 및 결제 처리에 비동기 로직 추가로 응답 시간 단축.
- [ ] **API 문서화**: Swagger를 이용해 주요 API 문서 자동 생성.

### 2. 중기 계획
- [ ] **WebSocket을 활용한 실시간 알림 기능**
  - 주문 상태 변경 시 사용자에게 실시간으로 알림 전송.
  - 장바구니 재고 부족 알림 기능 추가.
- [ ] **Kubernetes 도입**
  - Docker 컨테이너 오케스트레이션을 통해 서비스 확장성과 관리 용이성 강화.
- [ ] **다국어 지원**
  - 한국어와 영어를 기본으로 다국어 JSON 파일 기반 번역 추가.

### 3. 장기 계획
- [ ] **AI 기반 추천 시스템**
  - 사용자 구매 이력을 분석해 맞춤형 상품 추천.
- [ ] **ElasticSearch 도입**
  - 상품 검색 속도 향상을 위해 ElasticSearch 기반 검색 시스템 구축.
- [ ] **지속적인 부하 테스트**
  - K6를 이용한 정기적인 부하 테스트 자동화.
  - 성능 지표를 기준으로 병목 지점 개선.



