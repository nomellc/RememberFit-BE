# RememberFit

<img width="1920" height="1080" alt="1" src="https://github.com/user-attachments/assets/c347fba2-3197-4349-9f9f-f6aeefb7cb99" />



---

## 기술 구성

- Java 17
- Spring Boot 3.5.16
- Spring Web, Validation, Spring Data JPA

---

## 로컬 실행

MySQL 설치 없이 실행할 수 있습니다.

```bash
cd /Users/yujin/Desktop/Programming/backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

서버는 모든 로컬 네트워크 인터페이스의 8080 포트에서 요청을 받습니다. 첫 실행에 Flyway가 `V1__create_rememberfit_schema.sql`을 적용하고, 이후 데이터는 `data/rememberfit.mv.db`에 유지됩니다.


---

## 데이터베이스 마이그레이션

마이그레이션 위치:

```text
src/main/resources/db/migration/
└── V1__create_rememberfit_schema.sql
```

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

<img width="1920" height="1080" alt="5" src="https://github.com/user-attachments/assets/cde918ca-10bb-4f28-ba8e-3865265b93a8" />



---

### 2. 암기장 화면
학습 주제별로 암기장을 만들고 이름을 수정하거나 삭제할 수 있습니다. 각 암기장에 포함된 카드 수도 함께 표시됩니다. <br/>

<img width="1920" height="1080" alt="6" src="https://github.com/user-attachments/assets/0615866e-f514-49f8-8e8a-be6491042308" />


---


### 3. 학습 화면
카드 앞면을 터치하면 뒷면에 정답이 나옵니다. <br/>
난이도를 선택하면 그 난이도에 따라 다음 학습 날짜가 자동으로 결정됩니다. <br/>

<img width="1920" height="1080" alt="7" src="https://github.com/user-attachments/assets/022ac752-7bbb-40c2-aad8-1e166705d394" />


---

### 4. 통계 화면
누적 학습 횟수, 최근 7일 학습량, 연속 학습일과 평가 분포를 실제 학습 기록을 기준으로 확인할 수 있습니다. <br/>

<img width="1920" height="1080" alt="8" src="https://github.com/user-attachments/assets/5f848d5a-5556-46d6-9dfe-d90bc909e666" />


