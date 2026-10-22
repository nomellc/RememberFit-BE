# AI 협업 검증 장부

AI에게 테스트 작성·진단을 위임하고, 그 결과를 사람이 검증한 기록.
판정은 `채택` / `수정 후 채택` / `반려` 세 가지만 사용한다.

---

## 베이스라인 (2026-10-23)

측정 시점: JaCoCo·pitest 도입 직후, 테스트 보강 착수 **전**.

### 규모

| 항목 | 값 |
|---|---|
| 프로덕션 코드 | 39개 파일 / 1,239줄 |
| 테스트 코드 | 9개 파일 / 632줄 |
| 테스트 메서드 | 21개 (`@Test` 20 + `@ParameterizedTest` 1) |
| 실행된 테스트 케이스 | 26개 (파라미터 전개 후) |
| 테스트 실행 시간 | 12초 (전부 통과) |

### 커버리지 (JaCoCo 0.8.12)

| 지표 | 값 |
|---|---|
| LINE | **90.5%** (248/274) |
| BRANCH | **75.8%** (25/33) |
| INSTRUCTION | 90.9% (1194/1313) |
| METHOD | 84.5% (71/84) |
| COMPLEXITY | 81.4% (83/102) |

커버리지가 낮은 클래스:

| 클래스 | LINE | BRANCH |
|---|---|---|
| `StudyService` | 69.6% (16/23) | **0.0%** |
| `DeckService` | 75.0% (9/12) | n/a |
| `StatisticsService` | 95.9% (47/49) | 76.2% |
| `Sm2Calculator` | **100%** (15/15) | **100%** |

### Mutation score (pitest)

| 지표 | 값 |
|---|---|
| Mutation score | **86.6%** (58/67 killed) |
| Test strength | **100%** |
| 생존 변이 | 9개 — **전부 `NO_COVERAGE`**, `SURVIVED` 0개 |

생존 변이 9개 전체:

| 클래스 | 위치 | 변이 |
|---|---|---|
| `StudyService` | `getDueCards()` L30 | `validateDeck()` 호출 제거 |
| `StudyService` | `getDueCards()` L31 | 반환값을 빈 리스트로 교체 |
| `StudyService` | `validateDeck()` L66 | 조건 반전 |
| `DeckService` | `getAllDecks()` L28 | 반환값을 빈 리스트로 교체 |
| `DeckService` | `getDeck()` L47 | `orElseThrow` 람다 반환값을 null로 교체 |
| `CardService` | `getDeck()` L57 | `orElseThrow` 람다 반환값을 null로 교체 |
| `StatisticsService` | `calculateStreak()` L102 | 조건 반전 |
| `StudyController` | `getDueCards()` L24 | 반환값을 빈 리스트로 교체 |
| `DeckController` | `getDecks()` L29 | 반환값을 빈 리스트로 교체 |

---

## 베이스라인 해석

세 지표가 서로 다른 말을 한다.

1. **라인 커버리지 90.5%** — "보강할 여지가 거의 없다"
2. **Test strength 100%** — 기존 테스트는 자기가 실행하는 범위 안에서 **단 하나의 변이도 놓치지 않았다.**
   즉 느슨한 assertion으로 통과만 시키는 "가짜 테스트"는 **0건이었다.**
3. **생존 변이 9개가 전부 `NO_COVERAGE`** — 공백의 성격은 "검증이 허술한 코드"가 아니라
   **"테스트가 한 번도 실행하지 않은 경로"**였다.

따라서 1주차 작업의 목표는 "커버리지를 올린다"가 아니라
**생존 변이 9개를 0으로 만든다**로 정의한다. 지표는 mutation score를 주 지표로,
라인 커버리지를 보조 지표로 쓴다. 전자는 목표가 수치 자체가 아니라
"죽여야 할 변이 목록"이라는 할 일 목록으로 환산되기 때문이다.

### 두 지표 모두 잡지 못하는 결함

`Sm2Calculator`는 라인 100%, 브랜치 100%, 변이 14개 전부 killed다.
세 지표 모두 만점이다. 그런데도 복습 간격에 상한이 없어, 성공 복습이 반복되면
`(int) Math.round(interval * easeFactor)`가 `int` 범위를 넘긴다
(`Sm2Calculator.java:26`).

커버리지와 mutation score는 **"작성된 코드가 의도대로 동작하는가"**만 측정하고,
**"필요한 코드가 작성되었는가"**는 측정하지 않는다. 누락된 요구사항은
어떤 자동 지표로도 발견되지 않으며, 이 결함은 사람이 알고리즘을 읽고 찾았다.
→ 자동 지표로 커버할 영역과 사람이 직접 읽어야 할 영역의 경계로 기록한다.

---

## 검증 기록

| 날짜 | 대상 | AI 제안 / 주장 | 판정 | 사유 |
|---|---|---|---|---|
| 10-23 | `DatabaseMigrationTest` | "테스트가 운영 경로(Flyway + ddl-auto=validate)를 전혀 검증하지 않는다" | **반려** | 파일을 열어보니 해당 테스트가 `spring.flyway.enabled=true`, `ddl-auto=validate`를 명시적으로 켜고 돌린다. 주장은 사실과 다름. 다만 H2 `MODE=MySQL`로 돌아 운영 MySQL과 다른 점은 실제 공백으로 남겨 CI 단계에서 MySQL 서비스 컨테이너로 해결하기로 함 |
| 10-23 | 테스트 작성 프롬프트 템플릿 | 완료 기준에 "간격 최대치 도달 시 동작" 포함 | **수정 후 채택** | 현재 코드에는 간격 상한이 존재하지 않는다. 이 문구를 그대로 주면 AI가 없는 스펙을 가정하고 테스트를 생성한다. "상한이 없는 것이 의도된 것인지 먼저 확인하고, 필요하다고 판단되면 테스트가 아니라 제안으로 제출" 으로 교체 |
| 10-23 | 지표 선택 | "mutation score를 재면 AI가 생성한 테스트 중 상당수가 코드를 실행만 하고 검증하지 않는 가짜 테스트로 드러날 것" | **반려** | 실제 측정 결과 test strength 100%, `SURVIVED` 0건. 기존 테스트는 실행 범위 안에서 모든 변이를 죽였다. 예측이 틀렸고, 공백은 전부 미실행 경로였음. 지표는 그대로 채택하되 근거를 교체 |

---

## 1주차 — 생존 변이 제거

### 1차: `StudyService` 변이 3개 (2026-10-23)

생존 변이 9개 중 가장 나쁜 클래스(`StudyService`, 3/6 killed)부터 착수했다.

**사전에 정의한 완료 기준**

| 죽여야 할 변이 | 사망 조건 |
|---|---|
| `getDueCards()` L30 — `validateDeck()` 호출 제거 | 없는 덱을 조회하면 `DECK_NOT_FOUND`가 발생해야 한다 |
| `getDueCards()` L31 — 반환값을 빈 리스트로 교체 | 덱이 있고 카드가 1장 이상일 때 리스트 크기와 내용을 검증해야 한다 |
| `validateDeck()` L66 — 조건 반전 | 덱이 있을 때 예외 없음 + 덱이 없을 때 예외 발생, 양방향이 모두 필요하다 |

**결과**

| 지표 | Before | After |
|---|---|---|
| Mutation score | 86.6% (58/67) | **91.0%** (61/67) |
| 생존 변이 | 9개 | **6개** |
| Test strength | 100% | 100% (유지) |
| 라인 커버리지 | 90.5% | 93.1% |
| 브랜치 커버리지 | 75.8% | 81.8% |
| `StudyService` 변이 | 3/6 killed | **6/6 killed** |
| `StudyService` 라인 | 69.6% | 100% |
| `StudyService` 브랜치 | 0.0% | 100% |
| 테스트 메서드 | 21개 | 24개 |
| 실행 케이스 | 26개 | 29개 |

목표 변이 3개는 전부 `KILLED`로 전환되었다. 테스트가 통과하는 것은 판정 근거로 쓰지 않고,
pitest 재실행 결과만 근거로 삼았다.

**남은 생존 변이 6개 (2차 대상)**

| 클래스 | 위치 | 변이 |
|---|---|---|
| `DeckService` | `getAllDecks()` L28 | 반환값을 빈 리스트로 교체 |
| `DeckService` | `getDeck()` L47 | `orElseThrow` 람다 반환값을 null로 교체 |
| `CardService` | `getDeck()` L57 | `orElseThrow` 람다 반환값을 null로 교체 |
| `StatisticsService` | `calculateStreak()` L102 | 조건 반전 |
| `DeckController` | `getDecks()` L29 | 반환값을 빈 리스트로 교체 |
| `StudyController` | `getDueCards()` L24 | 반환값을 빈 리스트로 교체 |

---

## 검증 기록 (이어서)

| 날짜 | 대상 | AI 제안 / 주장 | 판정 | 사유 |
|---|---|---|---|---|
| 10-23 | `StudyServiceTest` 신규 테스트 3건 | `returnsDueCardsQueriedWithApplicationTimeZoneDate`, `throwsDeckNotFoundBeforeQueryingCardsWhenDeckIsMissing`, `returnsEmptyListWhenDeckHasNoDueCards` | **채택** | pitest 재실행으로 목표 변이 3개가 전부 `KILLED`로 전환된 것을 확인. 통과 여부가 아니라 변이 사망을 근거로 판정 |
| 10-23 | `returnsEmptyListWhenDeckHasNoDueCards` | 위 3건 중 1건 | **채택 (단, 변이 미기여)** | 이 테스트는 "빈 리스트로 교체" 변이 하에서도 통과하므로 변이를 하나도 죽이지 않는다. 경계 동작 문서화 목적으로만 채택하고, 변이 제거 실적에서는 제외한다 |
| 10-23 | `StudyServiceTest` 구조 변경 | 테스트별 인라인 mock 생성을 `@BeforeEach` 필드 초기화로 전환 | **수정 후 채택** | 테스트 4건에서 mock 생성이 중복되므로 전환은 타당. 단 기존 테스트 `gradesCardWithApplicationTimeZoneAndSavesStudyLog`의 assertion은 한 줄도 바꾸지 않는 조건을 걸었고, 고정 시각(`2026-09-28T15:30:00Z`)과 기대 날짜 리터럴을 그대로 유지했다 |
| 10-23 | `StudyService.gradeCard()` | `getDueCards`와 달리 `validateDeck()`을 호출하지 않아, 없는 덱에 채점하면 `DECK_NOT_FOUND`가 아니라 `CARD_NOT_FOUND`가 반환됨 | **보류** | 프로덕션 코드 수정 금지 원칙에 따라 이번 작업에서 고치지 않음. pitest도 이 결함을 변이로 잡아내지 못했다(해당 분기가 애초에 코드에 없으므로). 별도 이슈로 분리 |

---

### 2차: 남은 생존 변이 6개 (2026-10-23)

**사전에 정의한 완료 기준**

| 죽여야 할 변이 | 사망 조건 |
|---|---|
| `DeckService.getAllDecks()` L28 — 빈 리스트 교체 | 목록의 크기·제목·`cardCount`까지 검증 |
| `DeckService.getDeck()` L47 — `orElseThrow` 람다 → null | 없는 덱 수정/삭제 시 `CustomException(DECK_NOT_FOUND)` (변이 시 NPE) |
| `CardService.getDeck()` L57 — `orElseThrow` 람다 → null | 없는 덱의 카드 조회/생성 시 `DECK_NOT_FOUND` |
| `StatisticsService.calculateStreak()` L102 — 조건 반전 | `&&`의 두 번째 조건이 평가되어야 하므로 **마지막 학습이 어제**인 경우가 필요 |
| `DeckController.getDecks()` L29 — 빈 리스트 교체 | `GET /api/decks` 응답 본문 검증 |
| `StudyController.getDueCards()` L24 — 빈 리스트 교체 | `GET /api/decks/{id}/cards/due` 응답 본문 검증 |

서비스 계층은 Mockito 단위 테스트, 컨트롤러 변이는 기존 `@SpringBootTest` + MockMvc 방식으로
처리했다. 프로젝트에 `@WebMvcTest`가 없어 새 방식을 도입하지 않았다.

**결과**

| 지표 | 베이스라인 | 1차 후 | 2차 후 |
|---|---|---|---|
| Mutation score | 86.6% (58/67) | 91.0% (61/67) | **100% (67/67)** |
| 생존 변이 | 9개 | 6개 | **0개** |
| Test strength | 100% | 100% | 100% |
| 라인 커버리지 | 90.5% | 93.1% | **95.3%** |
| 브랜치 커버리지 | 75.8% | 81.8% | **90.9%** |
| 테스트 메서드 | 21개 | 24개 | **35개** |
| 실행 케이스 | 26개 | 29개 | **40개** |

클래스별 변이: 10개 클래스 전부 100% killed.

남은 라인 커버리지 미달(95.3%)은 변이 대상에서 제외한 `global` 패키지와
`BackendApplication`이다. 변이 기준으로는 더 죽일 것이 없다.

---

## 검증 기록 (2차)

| 날짜 | 대상 | AI 제안 / 주장 | 판정 | 사유 |
|---|---|---|---|---|
| 10-23 | `DeckServiceTest` 3건 (신규) | 목록 매핑 1건 + 없는 덱 수정/삭제 2건 | **채택** | pitest가 `DeckService` L28, L47을 killed로 기록. 삭제 테스트는 `verify(never()).delete()`로 삭제가 실행되지 않는 것까지 검증 |
| 10-23 | `CardServiceTest` 2건 (신규) | 없는 덱의 카드 조회/생성 | **채택** | `CardService` L57 killed. 조회 테스트는 `verifyNoInteractions(cardRepository)`로 덱 검증이 카드 조회보다 먼저 일어나는 순서까지 고정 |
| 10-23 | `StatisticsServiceTest` streak 2건 | 마지막 학습이 어제인 경우(streak 2) / 이틀 전인 경우(streak 0) | **채택** | L102 `&&`의 두 번째 조건은 "마지막 학습이 오늘"인 기존 테스트에서는 short-circuit으로 **평가조차 되지 않았다.** 이 조건을 밟는 입력을 만들어야 죽는 변이였음 |
| 10-23 | 컨트롤러 통합 테스트 4건 | 덱 목록 / 복습 대상 목록 / 없는 덱 수정 / 없는 덱 카드 조회 | **채택** | `DeckController` L29, `StudyController` L24 killed |
| 10-23 | "pitest 크레딧이 없는 테스트 5건은 변이에 기여하지 않는다" | `killingTest` 기록을 근거로 한 판정 | **반려** | pitest는 변이당 killing test를 **1건만** 기록한다. 이를 근거로 무용 판정을 내리려다, 프로덕션 코드에 해당 변이 3종(조건 반전, 람다 2건)을 직접 주입해 5건을 돌렸더니 **5건 전부 실패**했다. 즉 전부 유효한 테스트였고, 크레딧이 다른 테스트에 간 것이었다. 주입한 변이는 검증 직후 `git checkout -- src/main`으로 되돌렸다 |

**수동 뮤테이션 검증 기록**

| 주입한 변이 | 실행한 테스트 | 결과 |
|---|---|---|
| `StatisticsService` L102 조건 반전 | `countsStreakWhenLatestStudyWasYesterday` | FAILED (= 변이 사망) |
| `DeckService` L47 람다 → null | `throwsDeckNotFoundWhenUpdatingMissingDeck` | FAILED |
| `CardService` L57 람다 → null | `throwsDeckNotFoundBeforeQueryingCardsWhenDeckIsMissing` | FAILED |
| `DeckService` L47 람다 → null | `rejectsDeckUpdateWhenDeckIsMissing` | FAILED |
| `CardService` L57 람다 → null | `rejectsCardListingWhenDeckIsMissing` | FAILED |

이로써 2차에서 추가한 11건 전부가 변이를 죽일 수 있는 테스트임을 확인했다.
pitest 크레딧 6건 + 수동 주입 확인 5건.

---

## 1주차 결론

mutation score 100%는 "버그가 없다"는 뜻이 아니다. 베이스라인에서 이미 확인했듯
`Sm2Calculator`는 변이 14개 전부 killed인 상태로도 복습 간격 상한이 없다.
`StudyService.gradeCard`의 덱 검증 누락(보류 항목)도 변이로 잡히지 않았다.

자동 지표가 끝나는 지점이 사람이 시작하는 지점이다:

| 구분 | 담당 |
|---|---|
| 작성된 코드가 의도대로 도는가 | 커버리지 + mutation score (자동) |
| 테스트가 실제로 검증하는가 | mutation score (자동) |
| 필요한 코드가 작성되었는가 | 사람이 요구사항과 코드를 대조 |
| AI가 내놓은 판정 근거가 맞는가 | 사람이 반례를 직접 주입 |

이번 작업에서 반려·정정한 4건은 전부 네 번째 행에서 나왔다.

---

## 2단계 — CI 구축 (2026-10-23)

전제: **현재 미배포.** 따라서 배포 job은 만들지 않고 PR 검사와 `main` 보호만 구성한다.
배포 자동화의 Before/After 수치는 배포 환경이 생긴 뒤로 미룬다.

### 구성

| job | 하는 일 | 둔 이유 |
|---|---|---|
| `verify` | 빌드 + 전체 테스트 + JaCoCo + pitest(지표 게이트) + 요약/PR 코멘트/아티팩트 | PR이 들어올 때 회귀를 막는 본체 |
| `migration-mysql` | MySQL 8.0 서비스 컨테이너에서 Flyway 마이그레이션 + JPA `validate` | 베이스라인에서 보류했던 "마이그레이션 검증이 H2에서만 돈다"를 해소 |

`DatabaseMigrationTest`의 접속 정보를 환경 변수로 오버라이드할 수 있게 바꿨다.
기본값은 기존 H2 그대로이므로 로컬 실행 방식은 달라지지 않는다.

### 지표 게이트 수치의 근거

| 설정 | 값 | 현재 실측 | 근거 |
|---|---|---|---|
| `mutationThreshold` | 95 | 100% | 여유 5pt ≈ 변이 3개. 새 코드가 지표를 눈에 띄게 떨어뜨리면 막는다 |
| `testStrengthThreshold` | 95 | 100% | 실행됐는데도 변이를 못 잡는 테스트(= 느슨한 assertion)가 들어오는 것을 막는다 |
| `coverageThreshold` | 90 | 98% | 변이 대상 클래스의 라인 커버리지 하한 |

100으로 조이지 않은 이유는 1인 개발에서 모든 PR이 만점을 유지하도록 강제하면
게이트를 끄게 되기 때문이다. 지표가 안정되면 올린다.

### 설계 판단 기록

| 판단 | 선택 | 근거 |
|---|---|---|
| 커버리지 PR 코멘트 | 서드파티 액션을 쓰지 않고 직접 파싱 스크립트 + `actions/github-script` | 면접에서 각 step을 설명할 수 있어야 하고, 공급망 의존을 줄이기 위해. 스크립트는 표준 라이브러리만 사용하므로 로컬에서도 같은 결과를 낸다 |
| PR 코멘트 방식 | 마커로 기존 코멘트를 찾아 **수정** | 매 push마다 새 코멘트를 쌓지 않는다 |
| 요약 생성 시점 | `if: always()` | 테스트가 실패한 실행의 수치가 가장 필요하다 |
| MySQL 비밀번호 | Secrets가 아닌 평문 | job 수명 동안만 존재하는 임시 컨테이너용. Secrets로 두면 "비밀이 아닌 값"이 섞여 실제 비밀 관리가 흐려진다 |
| 생존 변이 표시 순서 | `SURVIVED`를 `NO_COVERAGE`보다 먼저 | 전자는 "테스트가 실행했는데도 못 잡은" 변이로, 미실행보다 나쁜 신호다 |
| 동시 실행 | `concurrency` + `cancel-in-progress` | 같은 브랜치에 새 커밋이 오면 이전 실행을 취소 |

---

## 검증 기록 (3차)

| 날짜 | 대상 | AI 제안 / 주장 | 판정 | 사유 |
|---|---|---|---|---|
| 10-23 | pitest 지표 게이트 | "threshold를 설정하면 지표 하락 시 PR이 막힌다" | **채택 (직접 확인)** | 주장을 믿지 않고 `mutationThreshold`를 101로 올려 실행했다. `RuntimeException: Mutation score of 100 is below threshold of 101`, exit 1로 빌드가 실패하는 것을 확인한 뒤 95로 되돌렸다 |
| 10-23 | `DatabaseMigrationTest` 환경 변수 오버라이드 | "`${MIGRATION_TEST_DB_URL:기본값}` 형태로 CI에서 접속 대상을 바꿀 수 있다" | **채택 (직접 확인)** | 기본값 실행은 통과, 일부러 깨뜨린 URL을 환경 변수로 주입했을 때 테스트가 FAILED 되는 것까지 확인했다. 통과만 봤다면 "환경 변수가 무시되고 기본값으로 돌았을 가능성"을 배제할 수 없다 |
| 10-23 | `migration-mysql` job | 실제 MySQL에서 마이그레이션이 통과할 것 | **미검증** | 로컬 Docker 데몬이 꺼져 있어 확인하지 못했다. **이 job의 첫 CI 실행 자체가 검증이다.** V1 SQL의 `DOUBLE PRECISION`, `TIMESTAMP(6)` 널 허용 컬럼은 H2 MySQL 모드와 실제 MySQL의 동작이 갈릴 수 있는 지점이므로, 첫 실행이 실패하면 그것이 곧 찾으려던 결함이다 |
| 10-23 | CI 요약 스크립트 | 리포트 파싱 결과가 맞다 | **채택 (직접 확인)** | 리포트가 일부만 있는 상태(테스트 1건 실패)와 전체 실행 상태 양쪽에서 돌려, 전자는 "실패 1건 / 라인 10.9%", 후자는 "40건 통과 / 100% / 95.3%"로 정확히 보고하는 것을 확인했다 |

### 남은 수동 작업 (YAML로 할 수 없는 것)

"실패 시 머지 차단"은 워크플로우 파일이 아니라 레포 설정이다.
GitHub → Settings → Branches → `main` 브랜치 규칙에서
`빌드·테스트·변이 검증`과 `마이그레이션 검증 (실제 MySQL)`을
required status check로 지정해야 비로소 머지가 막힌다.
워크플로우만 추가한 상태는 "검사는 돌지만 무시하고 머지할 수 있는" 상태다.
