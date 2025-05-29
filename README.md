## 항해플러스 이커머스 프로젝트

### 설계 문서

1. [요구사항 정리](docs/Requirements.md)
2. [시퀀스 다이어그램](docs/sequence_diagram.md)
3. [ERD](docs/ERD.md)
4. [API 명세서](docs/API_docs.md)
5. [DB 성능 개선](docs/DB_indexing_report.md)
6. [DB기반 동시성 이슈 개선](docs/Concurrency_Report.md)
7. [Redis 기반 캐싱 전략](docs/Cache_report.md)
8. [Redis 자료구조 사용 TOP10 인기 상품 조회 및 Redis Lock 사용 동시성 제어 및 대용량 트래픽 쿠폰 발급 비동기 이벤트 설계](docs/redis_ranked_and_asynchronous_repord.md)
9. [서비스 MSA 확장 서비스 설계 및 분산트랜잭션](dosc/MSA_Architecture_Change_Design_Report.md)
10. [주문 정보 외부 전송 kafka 메시지 처리](docs/kafka_basic_learning.md)
11. [카프카 활용 선착순 쿠폰 발급 대용량 트래픽 비동기 처리 설계 및 구현](docs/kafka_design_report.md)

### Clean + Layered Architecture
![Untitled](https://prod-files-secure.s3.us-west-2.amazonaws.com/83c75a39-3aba-4ba4-a792-7aefe4b07895/b9e36099-d0d9-47fd-b3eb-042a8fb98a30/Untitled.png)

- 애플리케이션의 핵심은 비즈니스 로직
- 데이터 계층 및 API 계층이 비즈니스 로직을 의존 ( 비즈니스의 Interface 활용 )
- 도메인 중심적인 계층 아키텍처
- Presentation 은 도메인을 API로 서빙, DataSource 는 도메인이 필요로 하는 기능을 서빙
- DIP 🆗 OCP 🆗
