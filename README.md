# RememberFit

<img width="1920" height="1080" alt="1" src="https://github.com/user-attachments/assets/651aa0d3-bbfe-4e15-a293-647ee98650b2" />



---

## 기술 구성

- Java 17
- Spring Boot 3.5.16
- Spring Web, Validation, Spring Data JPA
- Spring Security + JWT(JJWT) 기반 인증

---

## 로컬 실행

MySQL 설치 없이 실행할 수 있습니다.

```bash
cd /Users/yujin/Desktop/Programming/backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

서버는 모든 로컬 네트워크 인터페이스의 8080 포트에서 요청을 받습니다. 첫 실행에 Flyway가 `V1__create_rememberfit_schema.sql`을 적용하고, 이후 데이터는 `data/rememberfit.mv.db`에 유지됩니다.


---

## 인증

이메일과 비밀번호로 가입·로그인한다. 모든 암기장과 학습 기록은 사용자별로 분리되며,
다른 사용자의 자원을 요청하면 존재 여부를 숨기기 위해 403 이 아니라 404 로 답한다.

| 메서드 | 경로 | 토큰 | 설명 |
|---|---|---|---|
| POST | `/api/auth/signup` | 불필요 | 회원가입. 가입과 동시에 토큰을 발급한다 |
| POST | `/api/auth/login` | 불필요 | 로그인 |
| POST | `/api/auth/refresh` | 불필요 | 재발급 토큰으로 새 토큰 쌍을 받는다 |
| POST | `/api/auth/logout` | 필요 | 해당 사용자의 재발급 토큰을 모두 폐기한다 |
| GET | `/api/auth/me` | 필요 | 로그인한 사용자 정보 |

그 밖의 모든 `/api/**` 요청은 `Authorization: Bearer <accessToken>` 헤더가 필요하다.

토큰 정책:

1. 비밀번호는 BCrypt 해시로만 저장한다.
2. 액세스 토큰은 기본 1시간짜리 JWT다.
3. 재발급 토큰은 난수이며 DB 에는 SHA-256 해시만 저장한다. 원문은 저장하지 않는다.
4. 재발급 토큰은 한 번 쓰면 폐기되고 새 토큰으로 교체된다(회전).
5. 토큰을 보내지 않으면 `UNAUTHORIZED`, 보냈지만 쓸 수 없으면 `INVALID_TOKEN` 으로 구분해 응답한다.
   클라이언트는 후자일 때만 재발급을 시도한다.

### 필요한 환경 변수

| 변수 | 기본값 | 설명 |
|---|---|---|
| `JWT_SECRET` | 없음 (필수) | HS256 서명 키. 32바이트 이상이어야 하며, 없으면 애플리케이션이 시작되지 않는다 |
| `JWT_ACCESS_TOKEN_TTL` | `PT1H` | 액세스 토큰 수명 |
| `JWT_REFRESH_TOKEN_TTL` | `P30D` | 재발급 토큰 수명 |

기본값을 두지 않은 이유는, 기본 키가 있으면 그대로 운영에 올라갈 수 있기 때문이다.
로컬 실행(`--spring.profiles.active=local`)에는 개발 전용 키가 들어 있어 따로 설정하지 않아도 된다.

---

## 데이터베이스 마이그레이션

마이그레이션 위치:

```text
src/main/resources/db/migration/
├── V1__create_rememberfit_schema.sql
└── V2__add_users_and_scope_data.sql
```

`V2` 는 `users`·`refresh_tokens` 테이블을 만들고 `decks`·`study_logs` 에 `user_id` 를 추가한다.
이미 암기장이 있는 DB 에서는 로그인할 수 없는 자리표시 사용자를 만들어 기존 행을 귀속시키므로
데이터가 사라지지 않는다. 빈 DB 에서는 그 계정도 만들지 않는다.

규칙:

1. 이미 적용된 파일을 수정하지 않습니다.
2. 다음 변경은 `V2__description.sql`, `V3__description.sql`처럼 새 파일로 추가합니다.
3. 운영 적용 전 빈 DB와 운영 백업 복원본에서 모두 테스트합니다.
4. 배포 시 Flyway 검증 또는 migration 실패가 발생하면 애플리케이션을 시작하지 않습니다.


---

## 패키지 구조

```text
com.rememberfit.backend
├── domain
│   ├── card
│   ├── deck
│   ├── statistics
│   └── study
└── global
    ├── config
    ├── exception
    └── response
```

---

## 사용 방법

### 1. 홈 화면
오늘 학습할 새 카드와 복습 카드 수, 기억 완료 현황을 한눈에 확인하고 바로 학습을 시작할 수 있습니다. <br/>

<img width="1920" height="1080" alt="5" src="https://github.com/user-attachments/assets/518f878e-b1e1-40e7-981d-ded2fc08361a" />



---

### 2. 암기장 화면
학습 주제별로 암기장을 만들고 이름을 수정하거나 삭제할 수 있습니다. 각 암기장에 포함된 카드 수도 함께 표시됩니다. <br/>

<img width="1920" height="1080" alt="6" src="https://github.com/user-attachments/assets/effd576f-05fb-4299-8ef3-80574e8e6f5a" />


---


### 3. 학습 화면
카드 앞면을 터치하면 뒷면에 정답이 나옵니다. <br/>
난이도를 선택하면 그 난이도에 따라 다음 학습 날짜가 자동으로 결정됩니다. <br/>

<img width="1920" height="1080" alt="7" src="https://github.com/user-attachments/assets/bb221ad8-b1c1-48d3-a3c1-ff92dd5d3ca3" />



---

### 4. 통계 화면
누적 학습 횟수, 최근 7일 학습량, 연속 학습일과 평가 분포를 실제 학습 기록을 기준으로 확인할 수 있습니다. <br/>


<img width="1920" height="1080" alt="8" src="https://github.com/user-attachments/assets/87d09246-6684-4361-9b44-3198bafe74a3" />


