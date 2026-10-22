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
