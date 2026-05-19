# GitHub Actions CI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a CI workflow that builds, lints, and unit-tests the app on every push and PR to `main`.

**Architecture:** Single job on `ubuntu-latest` — checkout → Java 17 → Gradle cache → `assembleDebug` → `lintDebug` → `testDebugUnitTest`. Lint HTML report is uploaded as an artifact on failure.

**Tech Stack:** GitHub Actions, Gradle 9, Android AGP 9.0.1, Java 17 (Temurin)

---

### Task 1: Create the CI workflow file

**Files:**
- Create: `.github/workflows/ci.yml`

- [ ] **Step 1: Create directory and workflow file**

Create `.github/workflows/ci.yml` with this exact content:

```yaml
name: CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Build debug APK
        run: ./gradlew assembleDebug

      - name: Run lint
        run: ./gradlew lintDebug

      - name: Run unit tests
        run: ./gradlew testDebugUnitTest

      - name: Upload lint report
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: lint-report
          path: app/build/reports/lint-results-debug.html
```

- [ ] **Step 2: Create a feature branch and commit**

```bash
git checkout -b ci/github-actions
git add .github/workflows/ci.yml docs/superpowers/specs/2026-05-19-github-actions-ci-design.md docs/superpowers/plans/2026-05-19-github-actions-ci.md
git commit -m "ci: add GitHub Actions CI workflow (build, lint, unit tests)"
```

- [ ] **Step 3: Push branch and open PR**

```bash
git push -u origin ci/github-actions
gh pr create \
  --title "ci: add GitHub Actions CI workflow" \
  --body "$(cat <<'EOF'
## Summary
- Adds `.github/workflows/ci.yml` triggered on push/PR to `main`
- Single job: build → lint → unit tests on `ubuntu-latest` with Java 17 (Temurin)
- Lint HTML report uploaded as artifact on failure

## Test plan
- [ ] Confirm the workflow appears under Actions tab after PR is opened
- [ ] Verify all three steps (assemble, lint, test) pass in the CI run
EOF
  )" \
  --base main
```

Expected: GitHub CLI prints the PR URL.
