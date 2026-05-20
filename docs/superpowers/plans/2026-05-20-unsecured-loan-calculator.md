# Unsecured Loan Calculator Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a `:loan` Gradle module with an Unsecured Loan Calculator feature and a Home screen in `:app` so users can choose which calculator to open.

**Architecture:** New `:loan` Android Library module with Clean Architecture (data/domain/ui). The `:app` module gains a `HomeScreen` as the entry point; tapping "Compound Interest" enters the existing calculator flow, tapping "Unsecured Loan" enters a new `LoanNavigation` composable that owns its own bottom nav and back stack.

**Tech Stack:** Kotlin, Jetpack Compose + Material3, Navigation3, Hilt, Room, Vico chart, KSP, kotlinx-serialization, JUnit4, kotlinx-coroutines-test.

---

## File Map

### New — `:loan` module
| File | Role |
|------|------|
| `loan/build.gradle.kts` | Android Library build config |
| `loan/src/main/AndroidManifest.xml` | Required by Android library |
| `loan/src/main/java/com/example/assetmanagement/loan/domain/model/LoanInput.kt` | Input data class |
| `loan/src/main/java/com/example/assetmanagement/loan/domain/model/LoanResult.kt` | Computed output |
| `loan/src/main/java/com/example/assetmanagement/loan/domain/model/LoanScheduleEntry.kt` | Per-month balance |
| `loan/src/main/java/com/example/assetmanagement/loan/domain/model/LoanHistoryItem.kt` | Saved record |
| `loan/src/main/java/com/example/assetmanagement/loan/domain/repository/LoanRepository.kt` | Repository interface |
| `loan/src/main/java/com/example/assetmanagement/loan/domain/usecase/ComputeLoanUseCase.kt` | Amortization formula |
| `loan/src/main/java/com/example/assetmanagement/loan/domain/usecase/SaveLoanCalculationUseCase.kt` | Persist result |
| `loan/src/main/java/com/example/assetmanagement/loan/domain/usecase/GetLoanHistoryUseCase.kt` | Load history |
| `loan/src/main/java/com/example/assetmanagement/loan/data/local/LoanEntity.kt` | Room entity |
| `loan/src/main/java/com/example/assetmanagement/loan/data/local/LoanDao.kt` | Room DAO |
| `loan/src/main/java/com/example/assetmanagement/loan/data/local/LoanDatabase.kt` | Room database |
| `loan/src/main/java/com/example/assetmanagement/loan/data/LoanRepositoryImpl.kt` | Repository impl |
| `loan/src/main/java/com/example/assetmanagement/loan/data/LoanModule.kt` | Hilt DI module |
| `loan/src/main/java/com/example/assetmanagement/loan/ui/loan/LoanViewModel.kt` | Calculator VM |
| `loan/src/main/java/com/example/assetmanagement/loan/ui/loan/LoanScreen.kt` | Calculator screen |
| `loan/src/main/java/com/example/assetmanagement/loan/ui/loan/LoanChartContent.kt` | Balance chart |
| `loan/src/main/java/com/example/assetmanagement/loan/ui/history/LoanHistoryViewModel.kt` | History VM |
| `loan/src/main/java/com/example/assetmanagement/loan/ui/history/LoanHistoryScreen.kt` | History screen |
| `loan/src/main/java/com/example/assetmanagement/loan/LoanNavigationKeys.kt` | NavKeys for loan flow |
| `loan/src/main/java/com/example/assetmanagement/loan/LoanNavigation.kt` | Internal bottom nav |
| `loan/src/test/.../ComputeLoanUseCaseTest.kt` | 5 unit tests |
| `loan/src/test/.../SaveLoanCalculationUseCaseTest.kt` | 1 unit test |
| `loan/src/test/.../GetLoanHistoryUseCaseTest.kt` | 1 unit test |
| `loan/src/test/.../LoanViewModelTest.kt` | 3 unit tests |
| `loan/src/test/.../LoanHistoryViewModelTest.kt` | 3 unit tests |

### Modified — `:app`
| File | Change |
|------|--------|
| `gradle/libs.versions.toml` | Add `android-library` plugin alias |
| `settings.gradle.kts` | Add `include(":loan")` |
| `app/build.gradle.kts` | Add `implementation(project(":loan"))` |
| `app/src/main/java/com/example/assetmanagement/NavigationKeys.kt` | Add `HomeKey`, `LoanKey` |
| `app/src/main/java/com/example/assetmanagement/Navigation.kt` | Start at `HomeKey`, add entries |
| `.gitignore` | Add `.superpowers/` |

### New — `:app`
| File | Role |
|------|------|
| `app/src/main/java/com/example/assetmanagement/ui/home/HomeScreen.kt` | Calculator picker |

---

## Task 1: Set up `:loan` Gradle module

**Files:**
- Create: `loan/build.gradle.kts`
- Create: `loan/src/main/AndroidManifest.xml`
- Modify: `gradle/libs.versions.toml`
- Modify: `settings.gradle.kts`

- [ ] **Step 1: Add `android-library` plugin alias to version catalog**

Edit `gradle/libs.versions.toml` — add one line in the `[plugins]` section:

```toml
android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }
```

- [ ] **Step 2: Create `loan/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.library)
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.assetmanagement.loan"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.vico.compose.m3)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

- [ ] **Step 3: Create `loan/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest />
```

- [ ] **Step 4: Register the module in `settings.gradle.kts`**

Add `include(":loan")` after `include(":app")`:

```kotlin
rootProject.name = "Asset Management"
include(":app")
include(":loan")
```

- [ ] **Step 5: Sync and verify**

Run: `./gradlew :loan:tasks`

Expected: Task list prints without error. If Gradle can't resolve a plugin, double-check the alias name in `libs.versions.toml`.

- [ ] **Step 6: Commit**

```bash
git add gradle/libs.versions.toml settings.gradle.kts loan/
git commit -m "chore: scaffold :loan Gradle module"
```

---

## Task 2: Domain models

**Files:**
- Create: `loan/src/main/java/com/example/assetmanagement/loan/domain/model/LoanInput.kt`
- Create: `loan/src/main/java/com/example/assetmanagement/loan/domain/model/LoanResult.kt`
- Create: `loan/src/main/java/com/example/assetmanagement/loan/domain/model/LoanScheduleEntry.kt`
- Create: `loan/src/main/java/com/example/assetmanagement/loan/domain/model/LoanHistoryItem.kt`

- [ ] **Step 1: Create `LoanInput.kt`**

```kotlin
package com.example.assetmanagement.loan.domain.model

data class LoanInput(
    val loanAmount: Double,
    val annualRate: Double,
    val termMonths: Int
)
```

- [ ] **Step 2: Create `LoanScheduleEntry.kt`**

```kotlin
package com.example.assetmanagement.loan.domain.model

data class LoanScheduleEntry(
    val month: Int,
    val remainingBalance: Double
)
```

- [ ] **Step 3: Create `LoanResult.kt`**

```kotlin
package com.example.assetmanagement.loan.domain.model

data class LoanResult(
    val monthlyPayment: Double,
    val totalRepayment: Double,
    val totalInterest: Double,
    val schedule: List<LoanScheduleEntry>
)
```

- [ ] **Step 4: Create `LoanHistoryItem.kt`**

```kotlin
package com.example.assetmanagement.loan.domain.model

data class LoanHistoryItem(
    val id: Long,
    val loanAmount: Double,
    val annualRate: Double,
    val termMonths: Int,
    val monthlyPayment: Double,
    val totalInterest: Double,
    val savedAt: Long
)
```

- [ ] **Step 5: Commit**

```bash
git add loan/src/main/java/com/example/assetmanagement/loan/domain/model/
git commit -m "feat(loan): add domain models"
```

---

## Task 3: ComputeLoanUseCase (TDD)

**Files:**
- Create: `loan/src/test/java/com/example/assetmanagement/loan/domain/usecase/ComputeLoanUseCaseTest.kt`
- Create: `loan/src/main/java/com/example/assetmanagement/loan/domain/usecase/ComputeLoanUseCase.kt`

- [ ] **Step 1: Write the failing tests**

Create `loan/src/test/java/com/example/assetmanagement/loan/domain/usecase/ComputeLoanUseCaseTest.kt`:

```kotlin
package com.example.assetmanagement.loan.domain.usecase

import com.example.assetmanagement.loan.domain.model.LoanInput
import org.junit.Assert.assertEquals
import org.junit.Test

class ComputeLoanUseCaseTest {

    private val useCase = ComputeLoanUseCase()

    @Test
    fun `standard 12-month loan produces correct monthly payment`() {
        // P=120000, rate=6%, n=12 → r=0.005
        // M = 120000 * (0.005 * 1.005^12) / (1.005^12 - 1) ≈ 10327.93
        val result = useCase(LoanInput(120_000.0, 6.0, 12))
        assertEquals(10_327.93, result.monthlyPayment, 1.0)
    }

    @Test
    fun `total repayment equals monthly payment times term`() {
        val result = useCase(LoanInput(120_000.0, 6.0, 12))
        assertEquals(result.monthlyPayment * 12, result.totalRepayment, 0.01)
    }

    @Test
    fun `total interest equals total repayment minus principal`() {
        val result = useCase(LoanInput(120_000.0, 6.0, 12))
        assertEquals(result.totalRepayment - 120_000.0, result.totalInterest, 0.01)
    }

    @Test
    fun `zero interest rate monthly payment equals principal divided by term`() {
        // rate=0 → M = P / n
        val result = useCase(LoanInput(120_000.0, 0.0, 12))
        assertEquals(10_000.0, result.monthlyPayment, 0.01)
        assertEquals(0.0, result.totalInterest, 0.01)
    }

    @Test
    fun `one-month term monthly payment equals principal plus one month interest`() {
        // P=100000, rate=12%, n=1 → r=0.01, M = 100000 * 1.01 = 101000
        val result = useCase(LoanInput(100_000.0, 12.0, 1))
        assertEquals(101_000.0, result.monthlyPayment, 0.01)
        assertEquals(1_000.0, result.totalInterest, 0.01)
    }

    @Test
    fun `schedule has correct number of entries and final balance near zero`() {
        val result = useCase(LoanInput(120_000.0, 6.0, 12))
        assertEquals(12, result.schedule.size)
        assertEquals(0.0, result.schedule.last().remainingBalance, 1.0)
    }
}
```

- [ ] **Step 2: Run tests — verify they fail**

Run: `./gradlew :loan:test --tests "*.ComputeLoanUseCaseTest"`

Expected: FAIL — `ComputeLoanUseCase` does not exist yet.

- [ ] **Step 3: Implement `ComputeLoanUseCase.kt`**

```kotlin
package com.example.assetmanagement.loan.domain.usecase

import com.example.assetmanagement.loan.domain.model.LoanInput
import com.example.assetmanagement.loan.domain.model.LoanResult
import com.example.assetmanagement.loan.domain.model.LoanScheduleEntry
import javax.inject.Inject
import kotlin.math.pow

class ComputeLoanUseCase @Inject constructor() {

    operator fun invoke(input: LoanInput): LoanResult {
        val n = input.termMonths
        val monthlyPayment: Double

        if (input.annualRate == 0.0) {
            monthlyPayment = input.loanAmount / n
        } else {
            val r = input.annualRate / 100.0 / 12.0
            monthlyPayment = input.loanAmount * (r * (1 + r).pow(n)) / ((1 + r).pow(n) - 1)
        }

        var balance = input.loanAmount
        val schedule = mutableListOf<LoanScheduleEntry>()
        val r = input.annualRate / 100.0 / 12.0

        for (month in 1..n) {
            balance = if (month == n) 0.0 else (balance * (1 + r) - monthlyPayment).coerceAtLeast(0.0)
            schedule.add(LoanScheduleEntry(month = month, remainingBalance = balance))
        }

        val totalRepayment = monthlyPayment * n
        return LoanResult(
            monthlyPayment = monthlyPayment,
            totalRepayment = totalRepayment,
            totalInterest = totalRepayment - input.loanAmount,
            schedule = schedule
        )
    }
}
```

- [ ] **Step 4: Run tests — verify they pass**

Run: `./gradlew :loan:test --tests "*.ComputeLoanUseCaseTest"`

Expected: 6 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add loan/src/
git commit -m "feat(loan): implement ComputeLoanUseCase with TDD"
```

---

## Task 4: LoanRepository + use cases (TDD)

**Files:**
- Create: `loan/src/main/java/com/example/assetmanagement/loan/domain/repository/LoanRepository.kt`
- Create: `loan/src/main/java/com/example/assetmanagement/loan/domain/usecase/SaveLoanCalculationUseCase.kt`
- Create: `loan/src/main/java/com/example/assetmanagement/loan/domain/usecase/GetLoanHistoryUseCase.kt`
- Create: `loan/src/test/java/com/example/assetmanagement/loan/domain/usecase/SaveLoanCalculationUseCaseTest.kt`
- Create: `loan/src/test/java/com/example/assetmanagement/loan/domain/usecase/GetLoanHistoryUseCaseTest.kt`

- [ ] **Step 1: Create `LoanRepository.kt`**

```kotlin
package com.example.assetmanagement.loan.domain.repository

import com.example.assetmanagement.loan.domain.model.LoanHistoryItem
import com.example.assetmanagement.loan.domain.model.LoanInput
import com.example.assetmanagement.loan.domain.model.LoanResult
import kotlinx.coroutines.flow.Flow

interface LoanRepository {
    suspend fun save(input: LoanInput, result: LoanResult)
    fun getAll(): Flow<List<LoanHistoryItem>>
    suspend fun deleteById(id: Long)
}
```

- [ ] **Step 2: Write failing tests for both use cases**

Create `loan/src/test/java/com/example/assetmanagement/loan/domain/usecase/SaveLoanCalculationUseCaseTest.kt`:

```kotlin
package com.example.assetmanagement.loan.domain.usecase

import com.example.assetmanagement.loan.domain.model.LoanHistoryItem
import com.example.assetmanagement.loan.domain.model.LoanInput
import com.example.assetmanagement.loan.domain.model.LoanResult
import com.example.assetmanagement.loan.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SaveLoanCalculationUseCaseTest {

    @Test
    fun `delegates save to repository`() = runTest {
        var savedInput: LoanInput? = null
        var savedResult: LoanResult? = null
        val fakeRepo = object : LoanRepository {
            override suspend fun save(input: LoanInput, result: LoanResult) {
                savedInput = input; savedResult = result
            }
            override fun getAll(): Flow<List<LoanHistoryItem>> = flowOf(emptyList())
            override suspend fun deleteById(id: Long) {}
        }
        val input = LoanInput(100_000.0, 5.0, 24)
        val result = LoanResult(4_386.52, 105_276.48, 5_276.48, emptyList())
        SaveLoanCalculationUseCase(fakeRepo)(input, result)
        assertEquals(input, savedInput)
        assertEquals(result, savedResult)
    }
}
```

Create `loan/src/test/java/com/example/assetmanagement/loan/domain/usecase/GetLoanHistoryUseCaseTest.kt`:

```kotlin
package com.example.assetmanagement.loan.domain.usecase

import com.example.assetmanagement.loan.domain.model.LoanHistoryItem
import com.example.assetmanagement.loan.domain.model.LoanInput
import com.example.assetmanagement.loan.domain.model.LoanResult
import com.example.assetmanagement.loan.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetLoanHistoryUseCaseTest {

    @Test
    fun `returns flow from repository`() = runTest {
        val items = listOf(LoanHistoryItem(1L, 100_000.0, 5.0, 24, 4_386.52, 5_276.48, 0L))
        val fakeRepo = object : LoanRepository {
            override suspend fun save(input: LoanInput, result: LoanResult) {}
            override fun getAll(): Flow<List<LoanHistoryItem>> = flowOf(items)
            override suspend fun deleteById(id: Long) {}
        }
        val result = GetLoanHistoryUseCase(fakeRepo)().first()
        assertEquals(items, result)
    }
}
```

- [ ] **Step 3: Run tests — verify they fail**

Run: `./gradlew :loan:test --tests "*.SaveLoanCalculationUseCaseTest" --tests "*.GetLoanHistoryUseCaseTest"`

Expected: FAIL — use cases don't exist yet.

- [ ] **Step 4: Create `SaveLoanCalculationUseCase.kt`**

```kotlin
package com.example.assetmanagement.loan.domain.usecase

import com.example.assetmanagement.loan.domain.model.LoanInput
import com.example.assetmanagement.loan.domain.model.LoanResult
import com.example.assetmanagement.loan.domain.repository.LoanRepository
import javax.inject.Inject

class SaveLoanCalculationUseCase @Inject constructor(
    private val repository: LoanRepository
) {
    suspend operator fun invoke(input: LoanInput, result: LoanResult) =
        repository.save(input, result)
}
```

- [ ] **Step 5: Create `GetLoanHistoryUseCase.kt`**

```kotlin
package com.example.assetmanagement.loan.domain.usecase

import com.example.assetmanagement.loan.domain.model.LoanHistoryItem
import com.example.assetmanagement.loan.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLoanHistoryUseCase @Inject constructor(
    private val repository: LoanRepository
) {
    operator fun invoke(): Flow<List<LoanHistoryItem>> = repository.getAll()
}
```

- [ ] **Step 6: Run tests — verify they pass**

Run: `./gradlew :loan:test --tests "*.SaveLoanCalculationUseCaseTest" --tests "*.GetLoanHistoryUseCaseTest"`

Expected: 2 tests PASS.

- [ ] **Step 7: Commit**

```bash
git add loan/src/
git commit -m "feat(loan): add LoanRepository and use cases with TDD"
```

---

## Task 5: Data layer (Room + Hilt)

**Files:**
- Create: `loan/src/main/java/com/example/assetmanagement/loan/data/local/LoanEntity.kt`
- Create: `loan/src/main/java/com/example/assetmanagement/loan/data/local/LoanDao.kt`
- Create: `loan/src/main/java/com/example/assetmanagement/loan/data/local/LoanDatabase.kt`
- Create: `loan/src/main/java/com/example/assetmanagement/loan/data/LoanRepositoryImpl.kt`
- Create: `loan/src/main/java/com/example/assetmanagement/loan/data/LoanModule.kt`

- [ ] **Step 1: Create `LoanEntity.kt`**

```kotlin
package com.example.assetmanagement.loan.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loanAmount: Double,
    val annualRate: Double,
    val termMonths: Int,
    val monthlyPayment: Double,
    val totalInterest: Double,
    val savedAt: Long
)
```

- [ ] **Step 2: Create `LoanDao.kt`**

```kotlin
package com.example.assetmanagement.loan.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Insert
    suspend fun insert(entity: LoanEntity)

    @Query("SELECT * FROM loans ORDER BY savedAt DESC")
    fun getAll(): Flow<List<LoanEntity>>

    @Query("DELETE FROM loans WHERE id = :id")
    suspend fun deleteById(id: Long)
}
```

- [ ] **Step 3: Create `LoanDatabase.kt`**

```kotlin
package com.example.assetmanagement.loan.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [LoanEntity::class], version = 1, exportSchema = false)
abstract class LoanDatabase : RoomDatabase() {
    abstract fun loanDao(): LoanDao
}
```

- [ ] **Step 4: Create `LoanRepositoryImpl.kt`**

```kotlin
package com.example.assetmanagement.loan.data

import com.example.assetmanagement.loan.data.local.LoanDao
import com.example.assetmanagement.loan.data.local.LoanEntity
import com.example.assetmanagement.loan.domain.model.LoanHistoryItem
import com.example.assetmanagement.loan.domain.model.LoanInput
import com.example.assetmanagement.loan.domain.model.LoanResult
import com.example.assetmanagement.loan.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LoanRepositoryImpl @Inject constructor(
    private val dao: LoanDao
) : LoanRepository {

    override suspend fun save(input: LoanInput, result: LoanResult) {
        dao.insert(
            LoanEntity(
                loanAmount = input.loanAmount,
                annualRate = input.annualRate,
                termMonths = input.termMonths,
                monthlyPayment = result.monthlyPayment,
                totalInterest = result.totalInterest,
                savedAt = System.currentTimeMillis()
            )
        )
    }

    override fun getAll(): Flow<List<LoanHistoryItem>> =
        dao.getAll().map { list -> list.map { it.toHistoryItem() } }

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    private fun LoanEntity.toHistoryItem() = LoanHistoryItem(
        id = id,
        loanAmount = loanAmount,
        annualRate = annualRate,
        termMonths = termMonths,
        monthlyPayment = monthlyPayment,
        totalInterest = totalInterest,
        savedAt = savedAt
    )
}
```

- [ ] **Step 5: Create `LoanModule.kt`**

```kotlin
package com.example.assetmanagement.loan.data

import android.content.Context
import androidx.room.Room
import com.example.assetmanagement.loan.data.local.LoanDao
import com.example.assetmanagement.loan.data.local.LoanDatabase
import com.example.assetmanagement.loan.domain.repository.LoanRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LoanModule {

    @Binds
    @Singleton
    abstract fun bindLoanRepository(impl: LoanRepositoryImpl): LoanRepository

    companion object {
        @Provides
        @Singleton
        fun provideLoanDatabase(@ApplicationContext context: Context): LoanDatabase =
            Room.databaseBuilder(context, LoanDatabase::class.java, "loans.db").build()

        @Provides
        fun provideLoanDao(db: LoanDatabase): LoanDao = db.loanDao()
    }
}
```

- [ ] **Step 6: Build the module to verify Room/Hilt codegen**

Run: `./gradlew :loan:compileDebugKotlin`

Expected: BUILD SUCCESSFUL. If KSP errors appear, verify `ksp(libs.room.compiler)` is in `loan/build.gradle.kts`.

- [ ] **Step 7: Commit**

```bash
git add loan/src/main/java/com/example/assetmanagement/loan/data/
git commit -m "feat(loan): add Room data layer and Hilt module"
```

---

## Task 6: LoanViewModel (TDD)

**Files:**
- Create: `loan/src/test/java/com/example/assetmanagement/loan/ui/loan/LoanViewModelTest.kt`
- Create: `loan/src/main/java/com/example/assetmanagement/loan/ui/loan/LoanViewModel.kt`

- [ ] **Step 1: Write failing tests**

Create `loan/src/test/java/com/example/assetmanagement/loan/ui/loan/LoanViewModelTest.kt`:

```kotlin
package com.example.assetmanagement.loan.ui.loan

import com.example.assetmanagement.loan.domain.model.LoanHistoryItem
import com.example.assetmanagement.loan.domain.model.LoanInput
import com.example.assetmanagement.loan.domain.model.LoanResult
import com.example.assetmanagement.loan.domain.repository.LoanRepository
import com.example.assetmanagement.loan.domain.usecase.ComputeLoanUseCase
import com.example.assetmanagement.loan.domain.usecase.SaveLoanCalculationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoanViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeRepo = object : LoanRepository {
        override suspend fun save(input: LoanInput, result: LoanResult) {}
        override fun getAll(): Flow<List<LoanHistoryItem>> = flowOf(emptyList())
        override suspend fun deleteById(id: Long) {}
    }

    private lateinit var viewModel: LoanViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoanViewModel(ComputeLoanUseCase(), SaveLoanCalculationUseCase(fakeRepo))
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `initial state is Idle`() {
        assertTrue(viewModel.uiState.value is LoanUiState.Idle)
    }

    @Test
    fun `calculate transitions to ShowingResult with correct schedule size`() = runTest {
        viewModel.calculate(LoanInput(120_000.0, 6.0, 12))
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as LoanUiState.ShowingResult
        assertTrue(state.result.schedule.size == 12)
    }

    @Test
    fun `prefill transitions to ShowingResult with provided input values`() = runTest {
        viewModel.prefill(200_000.0, 5.0, 24)
        val state = viewModel.uiState.value as LoanUiState.ShowingResult
        assertTrue(state.input.loanAmount == 200_000.0)
        assertTrue(state.input.annualRate == 5.0)
        assertTrue(state.input.termMonths == 24)
    }
}
```

- [ ] **Step 2: Run tests — verify they fail**

Run: `./gradlew :loan:test --tests "*.LoanViewModelTest"`

Expected: FAIL — `LoanViewModel` and `LoanUiState` don't exist yet.

- [ ] **Step 3: Create `LoanViewModel.kt`**

```kotlin
package com.example.assetmanagement.loan.ui.loan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assetmanagement.loan.domain.model.LoanInput
import com.example.assetmanagement.loan.domain.model.LoanResult
import com.example.assetmanagement.loan.domain.usecase.ComputeLoanUseCase
import com.example.assetmanagement.loan.domain.usecase.SaveLoanCalculationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LoanUiState {
    object Idle : LoanUiState
    data class ShowingResult(val input: LoanInput, val result: LoanResult) : LoanUiState
    data class Error(val message: String) : LoanUiState
}

@HiltViewModel
class LoanViewModel @Inject constructor(
    private val computeLoanUseCase: ComputeLoanUseCase,
    private val saveLoanCalculationUseCase: SaveLoanCalculationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoanUiState>(LoanUiState.Idle)
    val uiState: StateFlow<LoanUiState> = _uiState.asStateFlow()

    fun calculate(input: LoanInput) {
        viewModelScope.launch {
            try {
                val result = computeLoanUseCase(input)
                saveLoanCalculationUseCase(input, result)
                _uiState.value = LoanUiState.ShowingResult(input, result)
            } catch (e: Exception) {
                _uiState.value = LoanUiState.Error(e.message ?: "Calculation failed")
            }
        }
    }

    fun prefill(loanAmount: Double, annualRate: Double, termMonths: Int) {
        val input = LoanInput(loanAmount, annualRate, termMonths)
        _uiState.value = LoanUiState.ShowingResult(input, computeLoanUseCase(input))
    }
}
```

- [ ] **Step 4: Run tests — verify they pass**

Run: `./gradlew :loan:test --tests "*.LoanViewModelTest"`

Expected: 3 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add loan/src/
git commit -m "feat(loan): implement LoanViewModel with TDD"
```

---

## Task 7: LoanHistoryViewModel (TDD)

**Files:**
- Create: `loan/src/test/java/com/example/assetmanagement/loan/ui/history/LoanHistoryViewModelTest.kt`
- Create: `loan/src/main/java/com/example/assetmanagement/loan/ui/history/LoanHistoryViewModel.kt`

- [ ] **Step 1: Write failing tests**

Create `loan/src/test/java/com/example/assetmanagement/loan/ui/history/LoanHistoryViewModelTest.kt`:

```kotlin
package com.example.assetmanagement.loan.ui.history

import com.example.assetmanagement.loan.domain.model.LoanHistoryItem
import com.example.assetmanagement.loan.domain.model.LoanInput
import com.example.assetmanagement.loan.domain.model.LoanResult
import com.example.assetmanagement.loan.domain.repository.LoanRepository
import com.example.assetmanagement.loan.domain.usecase.GetLoanHistoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoanHistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val historyFlow = MutableStateFlow<List<LoanHistoryItem>>(emptyList())

    private val fakeRepo = object : LoanRepository {
        override suspend fun save(input: LoanInput, result: LoanResult) {}
        override fun getAll(): Flow<List<LoanHistoryItem>> = historyFlow
        override suspend fun deleteById(id: Long) {}
    }

    private lateinit var viewModel: LoanHistoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoanHistoryViewModel(GetLoanHistoryUseCase(fakeRepo), fakeRepo)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `initial state is Loading`() {
        assertTrue(viewModel.uiState.value is LoanHistoryUiState.Loading)
    }

    @Test
    fun `empty list emits Empty state`() = runTest {
        historyFlow.value = emptyList()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is LoanHistoryUiState.Empty)
    }

    @Test
    fun `non-empty list emits Success state`() = runTest {
        historyFlow.value = listOf(LoanHistoryItem(1L, 100_000.0, 5.0, 24, 4_386.52, 5_276.48, 0L))
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is LoanHistoryUiState.Success)
    }
}
```

- [ ] **Step 2: Run tests — verify they fail**

Run: `./gradlew :loan:test --tests "*.LoanHistoryViewModelTest"`

Expected: FAIL — `LoanHistoryViewModel` and `LoanHistoryUiState` don't exist yet.

- [ ] **Step 3: Create `LoanHistoryViewModel.kt`**

```kotlin
package com.example.assetmanagement.loan.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assetmanagement.loan.domain.model.LoanHistoryItem
import com.example.assetmanagement.loan.domain.repository.LoanRepository
import com.example.assetmanagement.loan.domain.usecase.GetLoanHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LoanHistoryUiState {
    object Loading : LoanHistoryUiState
    data class Success(val items: List<LoanHistoryItem>) : LoanHistoryUiState
    object Empty : LoanHistoryUiState
}

@HiltViewModel
class LoanHistoryViewModel @Inject constructor(
    getLoanHistoryUseCase: GetLoanHistoryUseCase,
    private val repository: LoanRepository
) : ViewModel() {

    val uiState: StateFlow<LoanHistoryUiState> = getLoanHistoryUseCase()
        .map { items -> if (items.isEmpty()) LoanHistoryUiState.Empty else LoanHistoryUiState.Success(items) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, LoanHistoryUiState.Loading)

    fun delete(id: Long) {
        viewModelScope.launch { repository.deleteById(id) }
    }
}
```

- [ ] **Step 4: Run tests — verify they pass**

Run: `./gradlew :loan:test --tests "*.LoanHistoryViewModelTest"`

Expected: 3 tests PASS.

- [ ] **Step 5: Run all loan tests**

Run: `./gradlew :loan:test`

Expected: 13 tests PASS total (6 + 1 + 1 + 3 + 3).

- [ ] **Step 6: Commit**

```bash
git add loan/src/
git commit -m "feat(loan): implement LoanHistoryViewModel with TDD"
```

---

## Task 8: LoanChartContent composable

**Files:**
- Create: `loan/src/main/java/com/example/assetmanagement/loan/ui/loan/LoanChartContent.kt`

- [ ] **Step 1: Create `LoanChartContent.kt`**

```kotlin
package com.example.assetmanagement.loan.ui.loan

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.assetmanagement.loan.domain.model.LoanScheduleEntry
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

@Composable
fun LoanChartContent(schedule: List<LoanScheduleEntry>, modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(schedule) {
        modelProducer.runTransaction {
            lineSeries {
                series(schedule.map { it.remainingBalance.toFloat() })
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
    )
}
```

- [ ] **Step 2: Build to verify no compile errors**

Run: `./gradlew :loan:compileDebugKotlin`

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add loan/src/main/java/com/example/assetmanagement/loan/ui/loan/LoanChartContent.kt
git commit -m "feat(loan): add LoanChartContent composable"
```

---

## Task 9: LoanScreen composable

**Files:**
- Create: `loan/src/main/java/com/example/assetmanagement/loan/ui/loan/LoanScreen.kt`

- [ ] **Step 1: Create `LoanScreen.kt`**

```kotlin
package com.example.assetmanagement.loan.ui.loan

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.assetmanagement.loan.domain.model.LoanInput
import com.example.assetmanagement.loan.domain.model.LoanResult
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LoanScreen(
    prefillAmount: Double = 0.0,
    prefillRate: Double = 0.0,
    prefillMonths: Int = 0,
    hasPrefill: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: LoanViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var amountText by remember { mutableStateOf("") }
    var rateText by remember { mutableStateOf("") }
    var monthsText by remember { mutableStateOf("") }

    LaunchedEffect(hasPrefill) {
        if (hasPrefill) {
            amountText = prefillAmount.toLong().toString()
            rateText = prefillRate.toString()
            monthsText = prefillMonths.toString()
            viewModel.prefill(prefillAmount, prefillRate, prefillMonths)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Loan Calculator", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Loan Amount (NT\$)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = rateText,
            onValueChange = { rateText = it },
            label = { Text("Annual Interest Rate (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = monthsText,
            onValueChange = { monthsText = it },
            label = { Text("Loan Term (months, 1–360)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.calculate(
                    LoanInput(
                        loanAmount = amountText.toDoubleOrNull() ?: 0.0,
                        annualRate = rateText.toDoubleOrNull() ?: 0.0,
                        termMonths = monthsText.toIntOrNull()?.coerceIn(1, 360) ?: 1
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Calculate") }

        Spacer(Modifier.height(24.dp))

        when (val s = state) {
            is LoanUiState.ShowingResult -> {
                LoanChartContent(schedule = s.result.schedule)
                Spacer(Modifier.height(16.dp))
                LoanResultCard(result = s.result)
            }
            is LoanUiState.Error -> Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
            LoanUiState.Idle -> {}
        }
    }
}

@Composable
private fun LoanResultCard(result: LoanResult) {
    val fmt = NumberFormat.getNumberInstance(Locale.TAIWAN)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Results", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("Monthly Payment: NT\$${fmt.format(result.monthlyPayment.toLong())}")
            Text("Total Repayment: NT\$${fmt.format(result.totalRepayment.toLong())}")
            Text(
                "Total Interest: NT\$${fmt.format(result.totalInterest.toLong())}",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew :loan:compileDebugKotlin`

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add loan/src/main/java/com/example/assetmanagement/loan/ui/loan/LoanScreen.kt
git commit -m "feat(loan): add LoanScreen composable"
```

---

## Task 10: LoanHistoryScreen composable

**Files:**
- Create: `loan/src/main/java/com/example/assetmanagement/loan/ui/history/LoanHistoryScreen.kt`

- [ ] **Step 1: Create `LoanHistoryScreen.kt`**

```kotlin
package com.example.assetmanagement.loan.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.assetmanagement.loan.domain.model.LoanHistoryItem
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LoanHistoryScreen(
    onItemClick: (LoanHistoryItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoanHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Loan History", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        when (val s = state) {
            LoanHistoryUiState.Loading -> Text("Loading...")
            LoanHistoryUiState.Empty -> Text("No loan calculations saved yet.")
            is LoanHistoryUiState.Success -> {
                LazyColumn {
                    items(s.items, key = { it.id }) { item ->
                        SwipeToDismissLoanCard(
                            item = item,
                            onDelete = { viewModel.delete(item.id) },
                            onClick = { onItemClick(item) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissLoanCard(item: LoanHistoryItem, onDelete: () -> Unit, onClick: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.error).padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) { Text("Delete", color = MaterialTheme.colorScheme.onError) }
        }
    ) { LoanHistoryItemCard(item = item, onClick = onClick) }
}

@Composable
private fun LoanHistoryItemCard(item: LoanHistoryItem, onClick: () -> Unit) {
    val numFmt = NumberFormat.getNumberInstance(Locale.TAIWAN)
    val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                dateFmt.format(Date(item.savedAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text("NT\$${numFmt.format(item.loanAmount.toLong())}  Rate: ${item.annualRate}%  ${item.termMonths}mo")
            Text(
                "Monthly: NT\$${numFmt.format(item.monthlyPayment.toLong())}",
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew :loan:compileDebugKotlin`

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add loan/src/main/java/com/example/assetmanagement/loan/ui/history/LoanHistoryScreen.kt
git commit -m "feat(loan): add LoanHistoryScreen composable"
```

---

## Task 11: LoanNavigation + LoanNavigationKeys

**Files:**
- Create: `loan/src/main/java/com/example/assetmanagement/loan/LoanNavigationKeys.kt`
- Create: `loan/src/main/java/com/example/assetmanagement/loan/LoanNavigation.kt`

- [ ] **Step 1: Create `LoanNavigationKeys.kt`**

```kotlin
package com.example.assetmanagement.loan

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class LoanCalculatorKey(
    val prefillAmount: Double = 0.0,
    val prefillRate: Double = 0.0,
    val prefillMonths: Int = 0,
    val hasPrefill: Boolean = false
) : NavKey

@Serializable data object LoanHistoryKey : NavKey
```

- [ ] **Step 2: Create `LoanNavigation.kt`**

```kotlin
package com.example.assetmanagement.loan

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.assetmanagement.loan.ui.history.LoanHistoryScreen
import com.example.assetmanagement.loan.ui.loan.LoanScreen

@Composable
fun LoanNavigation() {
    val backStack = rememberNavBackStack(LoanCalculatorKey())
    val current = backStack.lastOrNull()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = current is LoanCalculatorKey,
                    onClick = { backStack.clear(); backStack.add(LoanCalculatorKey()) },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                    label = { Text("Loan") }
                )
                NavigationBarItem(
                    selected = current == LoanHistoryKey,
                    onClick = { backStack.clear(); backStack.add(LoanHistoryKey) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("History") }
                )
            }
        }
    ) { paddingValues ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<LoanCalculatorKey> { key ->
                    LoanScreen(
                        prefillAmount = key.prefillAmount,
                        prefillRate = key.prefillRate,
                        prefillMonths = key.prefillMonths,
                        hasPrefill = key.hasPrefill
                    )
                }
                entry<LoanHistoryKey> {
                    LoanHistoryScreen(
                        onItemClick = { item ->
                            backStack.clear()
                            backStack.add(
                                LoanCalculatorKey(
                                    prefillAmount = item.loanAmount,
                                    prefillRate = item.annualRate,
                                    prefillMonths = item.termMonths,
                                    hasPrefill = true
                                )
                            )
                        }
                    )
                }
            },
            modifier = Modifier.padding(paddingValues)
        )
    }
}
```

- [ ] **Step 3: Build to verify**

Run: `./gradlew :loan:compileDebugKotlin`

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add loan/src/main/java/com/example/assetmanagement/loan/
git commit -m "feat(loan): add LoanNavigation and navigation keys"
```

---

## Task 12: Wire `:app` — HomeScreen, NavigationKeys, Navigation

**Files:**
- Create: `app/src/main/java/com/example/assetmanagement/ui/home/HomeScreen.kt`
- Modify: `app/src/main/java/com/example/assetmanagement/NavigationKeys.kt`
- Modify: `app/src/main/java/com/example/assetmanagement/Navigation.kt`
- Modify: `app/build.gradle.kts`
- Modify: `.gitignore`

- [ ] **Step 1: Add `implementation(project(":loan"))` to `app/build.gradle.kts`**

Add after the last `implementation(...)` line in the `dependencies` block:

```kotlin
implementation(project(":loan"))
```

- [ ] **Step 2: Add `HomeKey` and `LoanKey` to `NavigationKeys.kt`**

Replace the full file content:

```kotlin
package com.example.assetmanagement

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey

@Serializable data object HomeKey : NavKey

@Serializable
data class CalculatorKey(
    val prefillFund: Double = 0.0,
    val prefillROI: Double = 0.0,
    val prefillYears: Int = 0,
    val prefillContribution: Double = 0.0,
    val hasPrefill: Boolean = false
) : NavKey

@Serializable data object HistoryKey : NavKey

@Serializable data object LoanKey : NavKey
```

- [ ] **Step 3: Create `HomeScreen.kt`**

```kotlin
package com.example.assetmanagement.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onCompoundClick: () -> Unit,
    onLoanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Asset Management", style = MaterialTheme.typography.headlineMedium)
        Text("Choose a calculator", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth().clickable { onCompoundClick() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("📊  Compound Interest", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("Grow your investment over time",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth().clickable { onLoanClick() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("💳  Unsecured Loan", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("Calculate monthly repayments",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
```

- [ ] **Step 4: Update `Navigation.kt` to start at `HomeKey`**

Replace the full file content:

```kotlin
package com.example.assetmanagement

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.assetmanagement.calculator.ui.calculator.CalculatorScreen
import com.example.assetmanagement.calculator.ui.history.HistoryScreen
import com.example.assetmanagement.loan.LoanNavigation
import com.example.assetmanagement.ui.home.HomeScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(HomeKey)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<HomeKey> {
                HomeScreen(
                    onCompoundClick = { backStack.add(CalculatorKey()) },
                    onLoanClick = { backStack.add(LoanKey) }
                )
            }
            entry<CalculatorKey> { key ->
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = true,
                                onClick = {},
                                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                label = { Text("Calculator") }
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = { backStack.clear(); backStack.add(HomeKey); backStack.add(HistoryKey) },
                                icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                label = { Text("History") }
                            )
                        }
                    }
                ) { paddingValues ->
                    CalculatorScreen(
                        prefillFund = key.prefillFund,
                        prefillROI = key.prefillROI,
                        prefillYears = key.prefillYears,
                        prefillContribution = key.prefillContribution,
                        hasPrefill = key.hasPrefill,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
            entry<HistoryKey> {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = false,
                                onClick = { backStack.clear(); backStack.add(HomeKey); backStack.add(CalculatorKey()) },
                                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                label = { Text("Calculator") }
                            )
                            NavigationBarItem(
                                selected = true,
                                onClick = {},
                                icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                label = { Text("History") }
                            )
                        }
                    }
                ) { paddingValues ->
                    HistoryScreen(
                        onItemClick = { item ->
                            backStack.clear()
                            backStack.add(HomeKey)
                            backStack.add(
                                CalculatorKey(
                                    prefillFund = item.initialFund,
                                    prefillROI = item.annualROI,
                                    prefillYears = item.durationYears,
                                    prefillContribution = item.annualContribution,
                                    hasPrefill = true
                                )
                            )
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
            entry<LoanKey> {
                LoanNavigation()
            }
        }
    )
}
```

- [ ] **Step 5: Add `.superpowers/` to `.gitignore`**

Add to `.gitignore`:

```
.superpowers/
```

- [ ] **Step 6: Build the full project**

Run: `./gradlew assembleDebug`

Expected: BUILD SUCCESSFUL. If Hilt complains about missing `@AndroidEntryPoint` component, verify `AssetManagementApplication` has `@HiltAndroidApp` (it does — no change needed).

- [ ] **Step 7: Run all unit tests**

Run: `./gradlew testDebugUnitTest`

Expected: All tests PASS (existing 15 + new 13 = 28 total).

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/example/assetmanagement/ app/build.gradle.kts .gitignore
git commit -m "feat: wire HomeScreen and LoanNavigation into app navigation"
```

---

## Task 13: Branch, push, open PR

- [ ] **Step 1: Create feature branch and push**

```bash
git checkout -b feat/unsecured-loan-calculator
git push -u origin feat/unsecured-loan-calculator
```

- [ ] **Step 2: Open PR**

```bash
gh pr create \
  --title "feat: add Unsecured Loan Calculator module" \
  --body "$(cat <<'EOF'
## Summary
- New `:loan` Android Library Gradle module with Clean Architecture (data/domain/ui)
- Amortization formula: monthly payment, total repayment, total interest, balance chart
- Separate loan history with swipe-to-delete and prefill-from-history
- New `HomeScreen` in `:app` — users choose between Compound Interest and Unsecured Loan calculators
- 13 new unit tests covering all domain and ViewModel layers

## Test plan
- [ ] `./gradlew testDebugUnitTest` — all 28 tests pass
- [ ] `./gradlew assembleDebug` — builds successfully
- [ ] App opens to Home screen with two calculator cards
- [ ] Loan Calculator: enter inputs, tap Calculate — shows monthly payment, total interest, balance chart
- [ ] Loan History: saved entries appear, swipe-to-delete works, tap to prefill inputs
- [ ] Compound Interest flow unchanged

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
  )" \
  --base master
```

Expected: GitHub CLI prints the PR URL.
