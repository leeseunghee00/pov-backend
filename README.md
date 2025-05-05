# POV: Point Of View

> 본 리드미는 본인(이승희)이 기술적으로 기여한 기능 중심으로 작성되었습니다. <br />
> 개발기간: 2024.11.12 ~ 2024.12.19 (6주)

![image](https://github.com/user-attachments/assets/f2a417c0-8bf7-4a1b-9f2f-86b4e022336f)
🔗 [Click! POV 발표 영상 보러가기](https://youtu.be/PlaKQ_yPdS4?si=_Vibk6oFez13WpKY)

# _Contents._

- [Intro.]()
- [Documents.]()
- [Stack.]()
- [SW Architecture.]()
- [Challenges & Solutions.]()
    - [1. Redisson 분산 락을 적용한 트랜잭션 충돌 해결]()
        - [해결 방안]()
        - [1-1. 테스트 진행]()
        - [1-2. 테스트 결과]()
    - [2. 안전한 결제 처리를 위한 아키텍처 설계]()
        - [해결 방안]()

<br >

# _Intro._

영화의 다양한 관점을 공유하는 플랫폼 POV 에서 담당한 기능은 다음과 같습니다:

- **시사회 응모 및 결제**
    - 일정 시간에 열리는 한정된 인원의 영화 시사회에 응모 및 결제할 수 있습니다.
- **영화 리뷰 조회**
    - 모든 영화의 리뷰 목록과 상세 정보를 조회할 수 있습니다.
    - 자신이 작성한 리뷰 목록과 상세 정보를 조회할 수 있습니다.
    - 가입한 클럽별 리뷰 목록과 상세 정보를 조회할 수 있습니다.
- **관리자 기능**
    - `좋아요 관리`: 일일 단위로 하루에 좋아요를 가장 많이 받은 상위 10개의 영화와 좋아요 수를 조회할 수 있습니다. 해당 데이터는 큐레이션을 생성하는 데 참고 자료가 됩니다.
    - `리뷰 관리`: 백오피스에서 리뷰 검색을 통해 특정 리뷰를 숨김 처리하여 리뷰를 관리합니다.
- **시사회 CRUD**
    - 관리자는 영화 시사회를 CRUD 할 수 있습니다.
    - 사용자는 영화 시사회 조회할 수 있습니다.

<br />

# _Documents._

- [선착순 응모 시스템의 DB 정합성 보장을 위한 트러블 슈팅](https://velog.io/@leeseunghee00/선착순-응모-시스템의-DB-정합성-보장을-위한-트러블-슈팅)
- [안전한 결제 서비스를 제공하기 위한 고민들](https://velog.io/@leeseunghee00/안전한-결제-서비스를-제공하기-위한-고민들)
- [DB 형상 관리를 위한 Flyway 적용기](https://velog.io/@leeseunghee00/DB-형상-관리를-위한-Flyway-적용기)
- [프로젝트 회고](https://velog.io/@leeseunghee00/LG-U-유레카-백엔드-1기를-수료하며#문서화를-통한-협업을-알게-된-융합-프로젝트)
- [컨벤션 규칙 (Team | GitHub | Jira | TestCode | DTO | Method | Exception)](https://shinhm1.notion.site/13ce7e8fdd1280039f31f0e3da72d995?pvs=4)
- [API 명세서](https://shinhm1.notion.site/API-13de7e8fdd1280caa986dec01793f7ac?pvs=4)

<br />

# _Stack._

<img width="910" alt="image" src="https://github.com/user-attachments/assets/a9004588-6058-487e-ab1a-50a40e49a5f1" />

<br />

# _SW Architecture._

![image](https://github.com/user-attachments/assets/cee974ff-cefb-4fb9-8b56-60d185b7571f)

<br />

# _Challenges & Solutions._

## 1. Redisson 분산 락을 적용한 트랜잭션 충돌 해결

> **문제**: 시사회 응모는 선착순으로 진행되는데, 수 천/만 명이 동시 접속 시 트랜잭션 충돌로 인한 데이터 정합성 깨짐 <br />
> **관련 문서**: [선착순 응모 시스템의 DB 정합성 보장을 위한 트러블 슈팅](https://velog.io/@leeseunghee00/선착순-응모-시스템의-DB-정합성-보장을-위한-트러블-슈팅)

<br />

### 해결 방안

- 응모부터 결제까지의 구간에 Redisson 기반 분산 락을 적용하여 동시성 제어 및 데이터 정합성 보장
- 결제 실패 시, `ExponentialBackOffPolicy` 전략을 통해 최대 3회까지 재시도
- 모든 실패는 로그로 기록하고, 슬랙 알림을 통해 실시간 오류 대응 가능

<img width="1242" alt="image" src="https://github.com/user-attachments/assets/c82a14c0-714c-464e-9383-118c5761fe1a" /> 

<br />
<br />

### 1-1. 테스트 진행

> **시나리오**: 선착순 100명 대비 1,000명의 동시 응모 요청 <br />
> **목표**: 100건의 응모 데이터 저장 & 최소 500 TPS 보장

- 분산 락의 동시성 제어 안정성을 검증하기 위해 별도의 테스트
  프로젝트([fcfs-entry-test](https://github.com/leeseunghee00/fcfs-entry-test?tab=readme-ov-file))에서 시나리오 기반 테스트를 진행
- DB Lock, Redis Lua Script 등 다양한 동시성 제어 방식을 비교 및 검토
- 테스트 도구: JMeter, nGrinder

<br />

### 1-2. 테스트 결과

아래 결과는 일부 Redisson 과 Lua Script 의 테스트 결과입니다. 자세한 내용은 관련 문서를 참고하세요!

1. **Redisson 결과**
    - TPS 160~200 사이를 유지
    - 에러 발생 없이 데이터 정합성 보장
    - 두 결과 평균 응답 시간은 4.5s 로 느린 편

![image](https://github.com/user-attachments/assets/a17ab44c-2fa2-47ed-8de1-5950d0295ef3)
![image](https://github.com/user-attachments/assets/96d80f80-97cd-4e84-a007-95b7cc3dbd24)

2. **Lua Script 결과**
    - TPS 470~865 사이를 유지, 목표 TPS 충분히 달성
    - 에러 발생 없이 데이터 정합성 보장 & 완만한 TPS 제공으로 안정성 확보
    - 두 결과 평균 응답 시간은 1.3s 로 양호한 편

![image](https://github.com/user-attachments/assets/b5d53c5e-fa9a-4aae-a9b6-6e46266d2ba5)  
![image](https://github.com/user-attachments/assets/15e41b64-ceb5-430b-8682-a7965df2c447)

<br />

## 2. 안전한 결제 처리를 위한 아키텍처 설계

> **문제**: 결제 과정에서 발생할 수 있는 예외 상황(ex. 중복 결제, 결제 실패)에 대한 대응 필요 <br />
> **관련 문서**: [안전한 결제 서비스를 제공하기 위한 고민들](https://velog.io/@leeseunghee00/안전한-결제-서비스를-제공하기-위한-고민들)

### 해결 방안

- **중복 결제 방지**: 응모 요청 시 멱등키를 생성하여 결제 요청 시 헤더에 포함. TTL을 설정을 통해 중복 처리 차단
- **결제 실패 대응**: `ExponentialBackOffPolicy` 전략을 활용해 최대 3회 재시도
- **모니터링 및 알림**: 재시도 실패 시 에러 로그 기록 및 슬랙 알림으로 실시간 대응 체계 구축

![image](https://github.com/user-attachments/assets/f46bd0f7-bfe3-4881-b852-a0e6e964f376)

