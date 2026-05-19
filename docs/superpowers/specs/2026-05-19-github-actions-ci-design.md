# GitHub Actions CI — Design Spec

**Date:** 2026-05-19  
**Project:** Asset Management (Android)

## Overview

A single CI workflow that runs on every push and pull request targeting `main`. It verifies the project compiles, passes lint, and all unit tests pass.

## Trigger

```yaml
on:
  push:
    branches: [main]
  pull_request:
    branches: [main]
```

## Structure

Single job `build` on `ubuntu-latest`. Steps run sequentially — a compile failure short-circuits lint and tests immediately.

## Steps

| Step | Action / Command | Purpose |
|------|-----------------|---------|
| Checkout | `actions/checkout@v4` | Get source |
| Java setup | `actions/setup-java@v4` (Temurin 17) | Match `jvmToolchain(17)` |
| Gradle cache | `gradle/actions/setup-gradle@v4` | Cache ~/.gradle across runs |
| Assemble | `./gradlew assembleDebug` | Verify compilation |
| Lint | `./gradlew lintDebug` | Static analysis |
| Unit tests | `./gradlew testDebugUnitTest` | Run 15 unit tests |
| Upload lint report | `actions/upload-artifact@v4` (on failure) | Inspect issues without re-running locally |

## Artifact

Lint report uploaded on failure: `app/build/reports/lint-results-debug.html`

## File Location

`.github/workflows/ci.yml`
