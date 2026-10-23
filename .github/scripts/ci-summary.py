"""JaCoCo·pitest·JUnit 리포트를 읽어 CI 요약 마크다운을 만든다.

표준 라이브러리만 사용한다. 리포트가 없으면 그 항목만 빈 값으로 두고 계속 진행한다
(테스트가 실패해 리포트가 생성되지 않은 경우에도 요약은 남아야 한다).

출력:
  - build/ci-summary.md           PR 코멘트 본문
  - $GITHUB_STEP_SUMMARY          Actions 실행 요약 (설정된 경우)
"""

import glob
import os
import xml.etree.ElementTree as ET

JACOCO_XML = "build/reports/jacoco/test/jacocoTestReport.xml"
PITEST_XML = "build/reports/pitest/mutations.xml"
JUNIT_GLOB = "build/test-results/test/TEST-*.xml"
OUT_PATH = "build/ci-summary.md"


def read_jacoco():
    if not os.path.exists(JACOCO_XML):
        return None
    root = ET.parse(JACOCO_XML).getroot()
    out = {}
    for counter in root.findall("counter"):
        missed = int(counter.get("missed"))
        covered = int(counter.get("covered"))
        total = missed + covered
        out[counter.get("type")] = (covered, total)
    return out


def read_pitest():
    if not os.path.exists(PITEST_XML):
        return None
    root = ET.parse(PITEST_XML).getroot()
    killed = no_coverage = survived = other = 0
    weak = []
    for mutation in root.findall("mutation"):
        status = mutation.get("status")
        if status == "KILLED":
            killed += 1
        elif status == "NO_COVERAGE":
            no_coverage += 1
        elif status == "SURVIVED":
            survived += 1
            weak.append(mutation)
        else:
            other += 1
    total = killed + no_coverage + survived + other
    # test strength 는 "커버된 변이 중 죽인 비율"이다. 미커버 변이는 제외한다.
    covered = killed + survived
    return {
        "killed": killed,
        "total": total,
        "no_coverage": no_coverage,
        "survived": survived,
        "strength": (100.0 * killed / covered) if covered else None,
        "weak": weak,
    }


def read_junit():
    tests = failures = skipped = 0
    found = False
    for path in glob.glob(JUNIT_GLOB):
        found = True
        root = ET.parse(path).getroot()
        tests += int(root.get("tests") or 0)
        failures += int(root.get("failures") or 0) + int(root.get("errors") or 0)
        skipped += int(root.get("skipped") or 0)
    return {"tests": tests, "failures": failures, "skipped": skipped} if found else None


def pct(pair):
    covered, total = pair
    return f"{100.0 * covered / total:.1f}% ({covered}/{total})" if total else "n/a"


def build_markdown():
    lines = ["## CI 검증 요약", ""]

    junit = read_junit()
    if junit:
        state = "통과" if junit["failures"] == 0 else f"실패 {junit['failures']}건"
        lines.append(f"**테스트** {junit['tests']}건 — {state}")
    else:
        lines.append("**테스트** 리포트 없음 (빌드가 테스트 단계 전에 실패)")
    lines.append("")

    pit = read_pitest()
    jac = read_jacoco()

    lines.append("| 지표 | 값 |")
    lines.append("|---|---|")
    if pit:
        score = 100.0 * pit["killed"] / pit["total"] if pit["total"] else 0.0
        lines.append(f"| Mutation score | {score:.1f}% ({pit['killed']}/{pit['total']}) |")
        if pit["strength"] is not None:
            lines.append(f"| Test strength | {pit['strength']:.1f}% |")
        lines.append(f"| 생존 변이 | {pit['survived'] + pit['no_coverage']}건 "
                     f"(검증 누락 {pit['survived']}, 미실행 {pit['no_coverage']}) |")
    else:
        lines.append("| Mutation score | 리포트 없음 |")
    if jac:
        lines.append(f"| 라인 커버리지 | {pct(jac.get('LINE', (0, 0)))} |")
        lines.append(f"| 브랜치 커버리지 | {pct(jac.get('BRANCH', (0, 0)))} |")
    else:
        lines.append("| 커버리지 | 리포트 없음 |")
    lines.append("")

    # SURVIVED 는 "테스트가 실행했는데도 못 잡은" 변이다. NO_COVERAGE 보다 먼저 봐야 한다.
    if pit and pit["weak"]:
        lines.append("### 실행됐으나 잡히지 않은 변이")
        lines.append("")
        lines.append("| 클래스 | 메서드 | 줄 | 변이 |")
        lines.append("|---|---|---|---|")
        for mutation in pit["weak"][:20]:
            cls = (mutation.findtext("mutatedClass") or "").split(".")[-1]
            method = mutation.findtext("mutatedMethod") or ""
            line_no = mutation.findtext("lineNumber") or ""
            desc = mutation.findtext("description") or ""
            lines.append(f"| `{cls}` | `{method}` | {line_no} | {desc} |")
        if len(pit["weak"]) > 20:
            lines.append("")
            lines.append(f"그 외 {len(pit['weak']) - 20}건.")
        lines.append("")

    lines.append("자세한 HTML 리포트는 이 실행의 Artifacts에 있다.")
    return "\n".join(lines) + "\n"


def main():
    markdown = build_markdown()
    os.makedirs(os.path.dirname(OUT_PATH), exist_ok=True)
    with open(OUT_PATH, "w", encoding="utf-8") as handle:
        handle.write(markdown)

    step_summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if step_summary:
        with open(step_summary, "a", encoding="utf-8") as handle:
            handle.write(markdown)

    print(markdown)


if __name__ == "__main__":
    main()
