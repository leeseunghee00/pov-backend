# POV: Point Of View

> 본 리드미는 본인(이승희)이 기술적으로 기여한 기능 중심으로 작성되었습니다. <br />
> 개발기간: 2024.11.12 ~ 2024.12.19 (6주)

![image](https://github.com/user-attachments/assets/f2a417c0-8bf7-4a1b-9f2f-86b4e022336f)
🔗 [Click! POV 발표 영상 보러가기](https://youtu.be/PlaKQ_yPdS4?si=_Vibk6oFez13WpKY)

## _Intro._

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

## _Documents._

- [DB 형상 관리를 위한 Flyway 적용기](https://velog.io/@leeseunghee00/DB-형상-관리를-위한-Flyway-적용기)
- [프로젝트 회고](https://velog.io/@leeseunghee00/LG-U-유레카-백엔드-1기를-수료하며#문서화를-통한-협업을-알게-된-융합-프로젝트)
- [컨벤션 규칙 (Team | GitHub | Jira | TestCode | DTO | Method | Exception)](https://shinhm1.notion.site/13ce7e8fdd1280039f31f0e3da72d995?pvs=4)
- [API 명세서](https://shinhm1.notion.site/API-13de7e8fdd1280caa986dec01793f7ac?pvs=4)

<br />

## _Stack._

<img width="910" alt="image" src="https://github.com/user-attachments/assets/a9004588-6058-487e-ab1a-50a40e49a5f1" />

<br />

## _SW Architecture._

![image](https://github.com/user-attachments/assets/cee974ff-cefb-4fb9-8b56-60d185b7571f)

<br />

## _Challenges & Solutions._

### Redisson 분산 락을 적용한 트랜잭션 충돌 해결

- 문제: 선착순으로 이루어지는 응모 특성 상 수 천/만 명이 동시 접속 시 트랜잭션 충돌 발생
- 해결: Redisson 기반의 분산 락을 응모~결제 구간에 적용하여 데이터 정합성을 보장
  - 결제 실패 시, RetryTemplate 의 백오프 전략으로 재시도 3번 실행
  - 재시도 실패 시, 에러 로그 기록

<img width="1242" alt="image" src="https://github.com/user-attachments/assets/c82a14c0-714c-464e-9383-118c5761fe1a" />
