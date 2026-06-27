# Remove Legacy Dead Code Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Delete the unused "Main screen" template scaffolding (a closed, self-referential cluster of files that nothing in the running app calls) and its tests, leaving the build and all real tests green.

**Architecture:** The app's real entry point is `MainActivity → MainNavigation()` (the calculator + history feature). A leftover Android-Studio template cluster — `MainScreen`, `MainScreenViewModel`/`MainScreenUiState`, and `DataRepository`/`DefaultDataRepository` — references only itself and is never reached from `MainActivity`, the Hilt module, the manifest, or any build script. We delete the cluster and its dedicated tests, then verify nothing dangles.

**Tech Stack:** Kotlin, Gradle (`./gradlew`), Jetpack Compose. No new code is written — this is pure deletion + verification.

---

## Notes for the implementer

- This is a **deletion** task — there is no TDD red/green cycle. The safety net is: the project still compiles, the existing real unit tests still pass, and a tracked-file grep finds zero remaining references.
- Run all `git`, `grep`, and `./gradlew` commands from the repo root: `/Users/ddtddt55/AndroidStudioProjects/AssetManagement` (the shell working directory may not be the repo root — use `cd` or absolute paths).
- Use `git rm` (not a bare `rm`) so deletions are staged and any now-empty package directories are dropped from the index automatically.
- Use `git grep` for the verification sweeps — it searches only tracked files, so it ignores stale artifacts under `app/build/`.
- **Branch context:** This cleanup is independent of the open Traditional-Chinese-i18n PR. It should be done on its own branch cut from `main` (the execution skill / finishing-a-development-branch handles branch creation and integration). Do not amend or force-push.
- Do **not** touch the theme files (`theme/Color.kt`, `theme/Theme.kt`, `theme/Type.kt`) — `MyApplicationTheme` is used by `MainActivity`. Do not touch `res/` files — out of scope for this plan.

### The exact dead cluster (verified)

| File | Why it's dead |
|------|---------------|
| `app/src/main/java/com/example/assetmanagement/ui/main/MainScreen.kt` | Composable + previews; referenced only by itself. `MainActivity` calls `MainNavigation()`, not `MainScreen`. |
| `app/src/main/java/com/example/assetmanagement/ui/main/MainScreenViewModel.kt` | `MainScreenViewModel` + `MainScreenUiState`; referenced only by `MainScreen.kt`. |
| `app/src/main/java/com/example/assetmanagement/data/DataRepository.kt` | `DataRepository` interface + `DefaultDataRepository`; referenced only by `MainScreen.kt`/`MainScreenViewModel.kt`. |
| `app/src/test/java/com/example/assetmanagement/ui/main/MainScreenViewModelTest.kt` | Unit test for the dead `MainScreenViewModel`. |
| `app/src/androidTest/java/com/example/assetmanagement/ui/main/MainScreenTest.kt` | Instrumented test for the dead `MainScreen`. |

---

### Task 1: Delete the dead Main-screen UI and its tests

This removes the dead Composable and ViewModel plus the two tests that exist only to cover them. After this task, `DataRepository.kt` becomes orphaned but still compiles (it is simply unused) — it is removed in Task 2.

**Files:**
- Delete: `app/src/main/java/com/example/assetmanagement/ui/main/MainScreen.kt`
- Delete: `app/src/main/java/com/example/assetmanagement/ui/main/MainScreenViewModel.kt`
- Delete: `app/src/test/java/com/example/assetmanagement/ui/main/MainScreenViewModelTest.kt`
- Delete: `app/src/androidTest/java/com/example/assetmanagement/ui/main/MainScreenTest.kt`

- [ ] **Step 1: Remove the four files**

```bash
cd /Users/ddtddt55/AndroidStudioProjects/AssetManagement
git rm \
  app/src/main/java/com/example/assetmanagement/ui/main/MainScreen.kt \
  app/src/main/java/com/example/assetmanagement/ui/main/MainScreenViewModel.kt \
  app/src/test/java/com/example/assetmanagement/ui/main/MainScreenViewModelTest.kt \
  app/src/androidTest/java/com/example/assetmanagement/ui/main/MainScreenTest.kt
```

- [ ] **Step 2: Verify the app still compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`. (The remaining orphan `DataRepository.kt` is unused but valid Kotlin, so compilation passes.)

- [ ] **Step 3: Verify the remaining unit tests still pass**

Run: `./gradlew testDebugUnitTest`
Expected: `BUILD SUCCESSFUL`. The deleted `MainScreenViewModelTest` no longer runs; all calculator/history/usecase/repository tests still pass.

- [ ] **Step 4: Commit**

```bash
git commit -m "chore: remove unused Main-screen template scaffolding and its tests"
```

---

### Task 2: Delete the now-orphaned DataRepository

With `MainScreenViewModel` gone, `DataRepository`/`DefaultDataRepository` has no remaining consumers.

**Files:**
- Delete: `app/src/main/java/com/example/assetmanagement/data/DataRepository.kt`

- [ ] **Step 1: Confirm DataRepository has no remaining references in tracked source**

Run:
```bash
cd /Users/ddtddt55/AndroidStudioProjects/AssetManagement
git grep -n "DataRepository\|DefaultDataRepository" -- 'app/src' || echo "NO REFERENCES"
```
Expected: prints `NO REFERENCES` (the only previous references were in the files deleted in Task 1). If any line other than `NO REFERENCES` appears, STOP — investigate before deleting.

- [ ] **Step 2: Remove the file**

```bash
git rm app/src/main/java/com/example/assetmanagement/data/DataRepository.kt
```

- [ ] **Step 3: Verify the app still compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git commit -m "chore: remove orphaned DataRepository scaffolding"
```

---

### Task 3: Final verification sweep

No files change in this task — it confirms the deletions are complete and the project is healthy.

- [ ] **Step 1: Confirm zero dangling references to the removed cluster in tracked source**

Run:
```bash
cd /Users/ddtddt55/AndroidStudioProjects/AssetManagement
git grep -nE "MainScreen|MainScreenViewModel|MainScreenUiState|DataRepository|DefaultDataRepository|com\.example\.assetmanagement\.ui\.main|com\.example\.assetmanagement\.data\.DataRepository" -- 'app/src' || echo "CLEAN: no references"
```
Expected: `CLEAN: no references`. If anything prints, it is a real dangling reference — fix or report before continuing.

- [ ] **Step 2: Confirm the dead package directories are gone**

Run:
```bash
ls app/src/main/java/com/example/assetmanagement/ui/main 2>&1 || echo "ui/main removed (main)"
ls app/src/main/java/com/example/assetmanagement/data 2>&1 || echo "data removed (main)"
ls app/src/test/java/com/example/assetmanagement/ui/main 2>&1 || echo "ui/main removed (test)"
ls app/src/androidTest/java/com/example/assetmanagement/ui/main 2>&1 || echo "ui/main removed (androidTest)"
```
Expected: each line reports the directory was removed. If a directory still exists and is empty (some filesystems leave the empty dir behind after `git rm`), remove it: `rmdir <path>` (it is untracked once empty, so this needs no commit).

- [ ] **Step 3: Full debug build**

Run: `./gradlew assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Full unit-test run**

Run: `./gradlew testDebugUnitTest`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: (Optional) Lint to confirm no newly-introduced warnings**

Run: `./gradlew :app:lintDebug`
Expected: `BUILD SUCCESSFUL`. Note: the pre-existing `HardcodedText` warning that previously lived in the deleted `MainScreen.kt` ("Error loading data: …" / "Hello …") should now be gone — its disappearance is a positive side effect, not something to act on.

---

## Self-Review Notes

- **Spec coverage:** The request is "remove legacy code which is never called." The verified dead cluster is exactly five files: 3 source (`MainScreen.kt`, `MainScreenViewModel.kt`, `DataRepository.kt`) + 2 tests (`MainScreenViewModelTest.kt`, `MainScreenTest.kt`). Task 1 removes the UI pair + both tests; Task 2 removes the orphaned data file; Task 3 proves nothing dangles and the build/tests are green. No requirement left unaddressed.
- **Scope discipline (YAGNI):** Theme files and `res/` are explicitly excluded because they are still used (`MyApplicationTheme` ← `MainActivity`) or out of scope. No refactoring of live code is included.
- **Ordering correctness:** Task 1 leaves `DataRepository.kt` as a still-compiling orphan, so `compileDebugKotlin` passes after Task 1; Task 2 then removes it. No intermediate state fails to build.
- **Reference names consistent:** Symbols named identically throughout (`MainScreen`, `MainScreenViewModel`, `MainScreenUiState`, `DataRepository`, `DefaultDataRepository`) and match the actual files verified in the codebase. Verification greps in Tasks 2 and 3 use those exact names. No placeholders.
