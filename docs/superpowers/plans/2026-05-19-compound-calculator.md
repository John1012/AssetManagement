# Compound Rate Calculator Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a two-screen compound interest calculator (Calculator + History) with a year-by-year Vico line chart, NT$ currency, annual compounding, and Room-persisted history.

**Architecture:** Single `calculator` package under `com.example.assetmanagement` with Clean Architecture sub-packages: `data/` (Room + repository impl), `domain/` (models, repository interface, use cases), `ui/` (ViewModels + Compose screens). Two screens wired via Navigation 3 with a Material 3 `Scaffold` bottom navigation bar.

**Tech Stack:** Kotlin, Jetpack Compose, Navigation 3, ViewModel + StateFlow, Room 2.7.1, Hilt 2.52, hilt-navigation-compose 1.2.0, Vico 2.0.0, JUnit 4 + kotlinx-coroutines-test

---

### Task 1: Add dependencies

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `build.gradle.kts` (root)
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add versions, libraries, and plugin to libs.versions.toml**

In `gradle/libs.versions.toml`, add under `[versions]`:
```toml
room = "2.7.1"
hilt = "2.52"
hiltNavigationCompose = "1.2.0"
vico = "2.0.0"
```

Under `[libraries]`, add:
```toml
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }
vico-compose-m3 = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
```

Under `[plugins]`, add:
```toml
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

- [ ] **Step 2: Declare Hilt plugin in root build.gradle.kts**

In `build.gradle.kts` (root), add inside `plugins {}`:
```kotlin
alias(libs.plugins.hilt) apply false
```

- [ ] **Step 3: Apply plugins and dependencies in app/build.gradle.kts**

In `app/build.gradle.kts`, add to `plugins {}`:
```kotlin
alias(libs.plugins.hilt)
id("kotlin-kapt")
```

In `dependencies {}`, add:
```kotlin
// Room
implementation(libs.room.runtime)
implementation(libs.room.ktx)
kapt(libs.room.compiler)

// Hilt
implementation(libs.hilt.android)
kapt(libs.hilt.compiler)
implementation(libs.hilt.navigation.compose)

// Vico chart
implementation(libs.vico.compose.m3)

// Material icons
implementation(libs.androidx.compose.material.icons.extended)
```

- [ ] **Step 4: Sync and verify**
```
./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**
```bash
git add gradle/libs.versions.toml app/build.gradle.kts build.gradle.kts
git commit -m "feat: add Room, Hilt, Vico, and material-icons dependencies"
```

---

### Task 2: Set up Hilt Application class

**Files:**
- Create: `app/src/main/java/com/example/assetmanagement/AssetManagementApplication.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/example/assetmanagement/MainActivity.kt`

- [ ] **Step 1: Create Application class**

Create `app/src/main/java/com/example/assetmanagement/AssetManagementApplication.kt`:
```kotlin
package com.example.assetmanagement

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AssetManagementApplication : Application()
```

- [ ] **Step 2: Register in AndroidManifest.xml**

In `app/src/main/AndroidManifest.xml`, add `android:name` to `<application>`:
```xml
<application
    android:name=".AssetManagementApplication"
    ...>
```

- [ ] **Step 3: Annotate MainActivity with @AndroidEntryPoint**

In `MainActivity.kt`, add the annotation and import:
```kotlin
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // existing body unchanged
}
```

- [ ] **Step 4: Build to verify Hilt generates code**
```
./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**
```bash
git add app/src/main/java/com/example/assetmanagement/AssetManagementApplication.kt \
        app/src/main/AndroidManifest.xml \
        app/src/main/java/com/example/assetmanagement/MainActivity.kt
git commit -m "feat: set up Hilt with Application class and MainActivity entry point"
```

---

### Task 3: Domain models and repository interface

**Files:**
- Create: `app/src/main/java/com/example/assetmanagement/calculator/domain/model/CalculationInput.kt`
- Create: `app/src/main/java/com/example/assetmanagement/calculator/domain/model/YearlySnapshot.kt`
- Create: `app/src/main/java/com/example/assetmanagement/calculator/domain/model/CalculationResult.kt`
- Create: `app/src/main/java/com/example/assetmanagement/calculator/domain/model/HistoryItem.kt`
- Create: `app/src/main/java/com/example/assetmanagement/calculator/domain/repository/CalculationRepository.kt`

- [ ] **Step 1: Create CalculationInput**
```kotlin
package com.example.assetmanagement.calculator.domain.model

data class CalculationInput(
    val initialFund: Double,
    val annualROI: Double,
    val durationYears: Int,
    val annualContribution: Double = 0.0
)
```

- [ ] **Step 2: Create YearlySnapshot**
```kotlin
package com.example.assetmanagement.calculator.domain.model

data class YearlySnapshot(
    val year: Int,
    val totalValue: Double,
    val totalContributed: Double,
    val totalInterestEarned: Double
)
```

- [ ] **Step 3: Create CalculationResult**
```kotlin
package com.example.assetmanagement.calculator.domain.model

data class CalculationResult(
    val finalValue: Double,
    val totalContributed: Double,
    val totalInterestEarned: Double,
    val yearlySnapshots: List<YearlySnapshot>
)
```

- [ ] **Step 4: Create HistoryItem**
```kotlin
package com.example.assetmanagement.calculator.domain.model

data class HistoryItem(
    val id: Long,
    val initialFund: Double,
    val annualROI: Double,
    val durationYears: Int,
    val annualContribution: Double,
    val finalValue: Double,
    val savedAt: Long
)
```

- [ ] **Step 5: Create CalculationRepository interface**
```kotlin
package com.example.assetmanagement.calculator.domain.repository

import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.model.HistoryItem
import kotlinx.coroutines.flow.Flow

interface CalculationRepository {
    suspend fun save(input: CalculationInput, finalValue: Double)
    fun getAll(): Flow<List<HistoryItem>>
    suspend fun deleteById(id: Long)
}
```

- [ ] **Step 6: Verify compilation**
```
./gradlew compileDebugKotlin
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**
```bash
git add app/src/main/java/com/example/assetmanagement/calculator/
git commit -m "feat: add domain models and CalculationRepository interface"
```

---

### Task 4: ComputeCompoundGrowthUseCase (TDD)

**Files:**
- Create: `app/src/main/java/com/example/assetmanagement/calculator/domain/usecase/ComputeCompoundGrowthUseCase.kt`
- Test: `app/src/test/java/com/example/assetmanagement/calculator/domain/usecase/ComputeCompoundGrowthUseCaseTest.kt`

- [ ] **Step 1: Write failing tests**

Create `app/src/test/java/com/example/assetmanagement/calculator/domain/usecase/ComputeCompoundGrowthUseCaseTest.kt`:
```kotlin
package com.example.assetmanagement.calculator.domain.usecase

import com.example.assetmanagement.calculator.domain.model.CalculationInput
import org.junit.Assert.assertEquals
import org.junit.Test

class ComputeCompoundGrowthUseCaseTest {

    private val useCase = ComputeCompoundGrowthUseCase()

    @Test
    fun `single year no contribution`() {
        val result = useCase(CalculationInput(100_000.0, 10.0, 1, 0.0))
        assertEquals(110_000.0, result.finalValue, 0.01)
        assertEquals(100_000.0, result.totalContributed, 0.01)
        assertEquals(10_000.0, result.totalInterestEarned, 0.01)
        assertEquals(1, result.yearlySnapshots.size)
    }

    @Test
    fun `three years no contribution compounds correctly`() {
        val result = useCase(CalculationInput(100_000.0, 10.0, 3, 0.0))
        // 100000 * 1.1^3 = 133100
        assertEquals(133_100.0, result.finalValue, 0.01)
        assertEquals(110_000.0, result.yearlySnapshots[0].totalValue, 0.01)
        assertEquals(121_000.0, result.yearlySnapshots[1].totalValue, 0.01)
        assertEquals(133_100.0, result.yearlySnapshots[2].totalValue, 0.01)
    }

    @Test
    fun `with annual contribution added at start of year`() {
        val result = useCase(CalculationInput(100_000.0, 10.0, 2, 10_000.0))
        // Year 1: (100000 + 10000) * 1.1 = 121000
        // Year 2: (121000 + 10000) * 1.1 = 144100
        assertEquals(144_100.0, result.finalValue, 0.01)
        assertEquals(120_000.0, result.totalContributed, 0.01)
        assertEquals(24_100.0, result.totalInterestEarned, 0.01)
    }

    @Test
    fun `zero ROI returns sum of contributions`() {
        val result = useCase(CalculationInput(100_000.0, 0.0, 3, 5_000.0))
        assertEquals(115_000.0, result.finalValue, 0.01)
        assertEquals(0.0, result.totalInterestEarned, 0.01)
    }

    @Test
    fun `snapshot totalContributed tracks principal plus contributions per year`() {
        val result = useCase(CalculationInput(50_000.0, 5.0, 2, 10_000.0))
        assertEquals(60_000.0, result.yearlySnapshots[0].totalContributed, 0.01)
        assertEquals(70_000.0, result.yearlySnapshots[1].totalContributed, 0.01)
    }
}
```

- [ ] **Step 2: Run to confirm FAIL**
```
./gradlew test --tests "*.ComputeCompoundGrowthUseCaseTest"
```
Expected: compilation error — class not found

- [ ] **Step 3: Implement ComputeCompoundGrowthUseCase**

Create `app/src/main/java/com/example/assetmanagement/calculator/domain/usecase/ComputeCompoundGrowthUseCase.kt`:
```kotlin
package com.example.assetmanagement.calculator.domain.usecase

import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.model.CalculationResult
import com.example.assetmanagement.calculator.domain.model.YearlySnapshot
import javax.inject.Inject

class ComputeCompoundGrowthUseCase @Inject constructor() {

    operator fun invoke(input: CalculationInput): CalculationResult {
        val rate = 1.0 + input.annualROI / 100.0
        var value = input.initialFund
        val snapshots = mutableListOf<YearlySnapshot>()

        for (year in 1..input.durationYears) {
            value = (value + input.annualContribution) * rate
            val contributed = input.initialFund + input.annualContribution * year
            snapshots.add(
                YearlySnapshot(
                    year = year,
                    totalValue = value,
                    totalContributed = contributed,
                    totalInterestEarned = value - contributed
                )
            )
        }

        val totalContributed = input.initialFund + input.annualContribution * input.durationYears
        return CalculationResult(
            finalValue = value,
            totalContributed = totalContributed,
            totalInterestEarned = value - totalContributed,
            yearlySnapshots = snapshots
        )
    }
}
```

- [ ] **Step 4: Run tests to confirm PASS**
```
./gradlew test --tests "*.ComputeCompoundGrowthUseCaseTest"
```
Expected: 5 tests PASSED

- [ ] **Step 5: Commit**
```bash
git add app/src/main/java/com/example/assetmanagement/calculator/domain/usecase/ComputeCompoundGrowthUseCase.kt \
        app/src/test/java/com/example/assetmanagement/calculator/domain/usecase/ComputeCompoundGrowthUseCaseTest.kt
git commit -m "feat: implement ComputeCompoundGrowthUseCase with TDD"
```

---

### Task 5: Room data layer

**Files:**
- Create: `app/src/main/java/com/example/assetmanagement/calculator/data/local/CalculationEntity.kt`
- Create: `app/src/main/java/com/example/assetmanagement/calculator/data/local/CalculationDao.kt`
- Create: `app/src/main/java/com/example/assetmanagement/calculator/data/local/CalculationDatabase.kt`

- [ ] **Step 1: Create CalculationEntity**
```kotlin
package com.example.assetmanagement.calculator.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculations")
data class CalculationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val initialFund: Double,
    val annualROI: Double,
    val durationYears: Int,
    val annualContribution: Double,
    val finalValue: Double,
    val savedAt: Long
)
```

- [ ] **Step 2: Create CalculationDao**
```kotlin
package com.example.assetmanagement.calculator.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Insert
    suspend fun insert(entity: CalculationEntity)

    @Query("SELECT * FROM calculations ORDER BY savedAt DESC")
    fun getAll(): Flow<List<CalculationEntity>>

    @Query("DELETE FROM calculations WHERE id = :id")
    suspend fun deleteById(id: Long)
}
```

- [ ] **Step 3: Create CalculationDatabase**
```kotlin
package com.example.assetmanagement.calculator.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CalculationEntity::class], version = 1, exportSchema = false)
abstract class CalculationDatabase : RoomDatabase() {
    abstract fun calculationDao(): CalculationDao
}
```

- [ ] **Step 4: Build to verify Room annotation processing**
```
./gradlew compileDebugKotlin
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**
```bash
git add app/src/main/java/com/example/assetmanagement/calculator/data/
git commit -m "feat: add Room entity, DAO, and database"
```

---

### Task 6: CalculationRepositoryImpl and Hilt module

**Files:**
- Create: `app/src/main/java/com/example/assetmanagement/calculator/data/CalculationRepositoryImpl.kt`
- Create: `app/src/main/java/com/example/assetmanagement/calculator/data/CalculatorModule.kt`

- [ ] **Step 1: Create CalculationRepositoryImpl**
```kotlin
package com.example.assetmanagement.calculator.data

import com.example.assetmanagement.calculator.data.local.CalculationDao
import com.example.assetmanagement.calculator.data.local.CalculationEntity
import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.model.HistoryItem
import com.example.assetmanagement.calculator.domain.repository.CalculationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CalculationRepositoryImpl @Inject constructor(
    private val dao: CalculationDao
) : CalculationRepository {

    override suspend fun save(input: CalculationInput, finalValue: Double) {
        dao.insert(
            CalculationEntity(
                initialFund = input.initialFund,
                annualROI = input.annualROI,
                durationYears = input.durationYears,
                annualContribution = input.annualContribution,
                finalValue = finalValue,
                savedAt = System.currentTimeMillis()
            )
        )
    }

    override fun getAll(): Flow<List<HistoryItem>> =
        dao.getAll().map { list -> list.map { it.toHistoryItem() } }

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    private fun CalculationEntity.toHistoryItem() = HistoryItem(
        id = id,
        initialFund = initialFund,
        annualROI = annualROI,
        durationYears = durationYears,
        annualContribution = annualContribution,
        finalValue = finalValue,
        savedAt = savedAt
    )
}
```

- [ ] **Step 2: Create Hilt DI module**
```kotlin
package com.example.assetmanagement.calculator.data

import android.content.Context
import androidx.room.Room
import com.example.assetmanagement.calculator.data.local.CalculationDao
import com.example.assetmanagement.calculator.data.local.CalculationDatabase
import com.example.assetmanagement.calculator.domain.repository.CalculationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CalculatorModule {

    @Binds
    @Singleton
    abstract fun bindCalculationRepository(impl: CalculationRepositoryImpl): CalculationRepository

    companion object {
        @Provides
        @Singleton
        fun provideCalculationDatabase(@ApplicationContext context: Context): CalculationDatabase =
            Room.databaseBuilder(context, CalculationDatabase::class.java, "calculations.db").build()

        @Provides
        fun provideCalculationDao(db: CalculationDatabase): CalculationDao = db.calculationDao()
    }
}
```

- [ ] **Step 3: Build to verify Hilt wiring**
```
./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**
```bash
git add app/src/main/java/com/example/assetmanagement/calculator/data/CalculationRepositoryImpl.kt \
        app/src/main/java/com/example/assetmanagement/calculator/data/CalculatorModule.kt
git commit -m "feat: implement CalculationRepositoryImpl and Hilt DI module"
```

---

### Task 7: SaveCalculationUseCase and GetCalculationHistoryUseCase (TDD)

**Files:**
- Create: `app/src/main/java/com/example/assetmanagement/calculator/domain/usecase/SaveCalculationUseCase.kt`
- Create: `app/src/main/java/com/example/assetmanagement/calculator/domain/usecase/GetCalculationHistoryUseCase.kt`
- Test: `app/src/test/java/com/example/assetmanagement/calculator/domain/usecase/SaveCalculationUseCaseTest.kt`
- Test: `app/src/test/java/com/example/assetmanagement/calculator/domain/usecase/GetCalculationHistoryUseCaseTest.kt`

- [ ] **Step 1: Write failing tests**

Create `app/src/test/java/com/example/assetmanagement/calculator/domain/usecase/SaveCalculationUseCaseTest.kt`:
```kotlin
package com.example.assetmanagement.calculator.domain.usecase

import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.model.HistoryItem
import com.example.assetmanagement.calculator.domain.repository.CalculationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SaveCalculationUseCaseTest {

    private var savedInput: CalculationInput? = null
    private var savedFinalValue: Double? = null

    private val fakeRepo = object : CalculationRepository {
        override suspend fun save(input: CalculationInput, finalValue: Double) {
            savedInput = input; savedFinalValue = finalValue
        }
        override fun getAll(): Flow<List<HistoryItem>> = flowOf(emptyList())
        override suspend fun deleteById(id: Long) {}
    }

    @Test
    fun `invoke delegates to repository with correct args`() = runTest {
        val input = CalculationInput(100_000.0, 5.0, 10, 0.0)
        SaveCalculationUseCase(fakeRepo)(input, 162_889.46)
        assertEquals(input, savedInput)
        assertEquals(162_889.46, savedFinalValue!!, 0.01)
    }
}
```

Create `app/src/test/java/com/example/assetmanagement/calculator/domain/usecase/GetCalculationHistoryUseCaseTest.kt`:
```kotlin
package com.example.assetmanagement.calculator.domain.usecase

import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.model.HistoryItem
import com.example.assetmanagement.calculator.domain.repository.CalculationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetCalculationHistoryUseCaseTest {

    private val items = listOf(HistoryItem(1L, 100_000.0, 5.0, 10, 0.0, 162_889.0, 1_000_000L))

    private val fakeRepo = object : CalculationRepository {
        override suspend fun save(input: CalculationInput, finalValue: Double) {}
        override fun getAll(): Flow<List<HistoryItem>> = flowOf(items)
        override suspend fun deleteById(id: Long) {}
    }

    @Test
    fun `invoke returns items from repository`() = runTest {
        assertEquals(items, GetCalculationHistoryUseCase(fakeRepo)().first())
    }
}
```

- [ ] **Step 2: Run to confirm FAIL**
```
./gradlew test --tests "*.SaveCalculationUseCaseTest" --tests "*.GetCalculationHistoryUseCaseTest"
```
Expected: compilation error — classes not found

- [ ] **Step 3: Implement SaveCalculationUseCase**
```kotlin
package com.example.assetmanagement.calculator.domain.usecase

import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.repository.CalculationRepository
import javax.inject.Inject

class SaveCalculationUseCase @Inject constructor(
    private val repository: CalculationRepository
) {
    suspend operator fun invoke(input: CalculationInput, finalValue: Double) =
        repository.save(input, finalValue)
}
```

- [ ] **Step 4: Implement GetCalculationHistoryUseCase**
```kotlin
package com.example.assetmanagement.calculator.domain.usecase

import com.example.assetmanagement.calculator.domain.model.HistoryItem
import com.example.assetmanagement.calculator.domain.repository.CalculationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCalculationHistoryUseCase @Inject constructor(
    private val repository: CalculationRepository
) {
    operator fun invoke(): Flow<List<HistoryItem>> = repository.getAll()
}
```

- [ ] **Step 5: Run tests to confirm PASS**
```
./gradlew test --tests "*.SaveCalculationUseCaseTest" --tests "*.GetCalculationHistoryUseCaseTest"
```
Expected: 2 tests PASSED

- [ ] **Step 6: Commit**
```bash
git add app/src/main/java/com/example/assetmanagement/calculator/domain/usecase/ \
        app/src/test/java/com/example/assetmanagement/calculator/domain/usecase/
git commit -m "feat: implement SaveCalculationUseCase and GetCalculationHistoryUseCase with TDD"
```

---

### Task 8: CalculatorViewModel (TDD)

**Files:**
- Create: `app/src/main/java/com/example/assetmanagement/calculator/ui/calculator/CalculatorViewModel.kt`
- Test: `app/src/test/java/com/example/assetmanagement/calculator/ui/calculator/CalculatorViewModelTest.kt`

- [ ] **Step 1: Write failing tests**

Create `app/src/test/java/com/example/assetmanagement/calculator/ui/calculator/CalculatorViewModelTest.kt`:
```kotlin
package com.example.assetmanagement.calculator.ui.calculator

import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.model.HistoryItem
import com.example.assetmanagement.calculator.domain.repository.CalculationRepository
import com.example.assetmanagement.calculator.domain.usecase.ComputeCompoundGrowthUseCase
import com.example.assetmanagement.calculator.domain.usecase.SaveCalculationUseCase
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
class CalculatorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeRepo = object : CalculationRepository {
        override suspend fun save(input: CalculationInput, finalValue: Double) {}
        override fun getAll(): Flow<List<HistoryItem>> = flowOf(emptyList())
        override suspend fun deleteById(id: Long) {}
    }

    private lateinit var viewModel: CalculatorViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CalculatorViewModel(ComputeCompoundGrowthUseCase(), SaveCalculationUseCase(fakeRepo))
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `initial state is Idle`() {
        assertTrue(viewModel.uiState.value is CalculatorUiState.Idle)
    }

    @Test
    fun `calculate transitions to ShowingResult with correct snapshot count`() = runTest {
        viewModel.calculate(CalculationInput(100_000.0, 10.0, 5, 0.0))
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as CalculatorUiState.ShowingResult
        assertTrue(state.result.yearlySnapshots.size == 5)
    }

    @Test
    fun `prefill transitions to ShowingResult with provided values`() = runTest {
        viewModel.prefill(200_000.0, 8.0, 10, 5_000.0)
        val state = viewModel.uiState.value as CalculatorUiState.ShowingResult
        assertTrue(state.input.initialFund == 200_000.0)
        assertTrue(state.input.annualROI == 8.0)
    }
}
```

- [ ] **Step 2: Run to confirm FAIL**
```
./gradlew test --tests "*.CalculatorViewModelTest"
```
Expected: compilation error — class not found

- [ ] **Step 3: Implement CalculatorViewModel**

Create `app/src/main/java/com/example/assetmanagement/calculator/ui/calculator/CalculatorViewModel.kt`:
```kotlin
package com.example.assetmanagement.calculator.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.model.CalculationResult
import com.example.assetmanagement.calculator.domain.usecase.ComputeCompoundGrowthUseCase
import com.example.assetmanagement.calculator.domain.usecase.SaveCalculationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CalculatorUiState {
    object Idle : CalculatorUiState
    data class ShowingResult(val input: CalculationInput, val result: CalculationResult) : CalculatorUiState
    data class Error(val message: String) : CalculatorUiState
}

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val computeCompoundGrowthUseCase: ComputeCompoundGrowthUseCase,
    private val saveCalculationUseCase: SaveCalculationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CalculatorUiState>(CalculatorUiState.Idle)
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    fun calculate(input: CalculationInput) {
        viewModelScope.launch {
            try {
                val result = computeCompoundGrowthUseCase(input)
                saveCalculationUseCase(input, result.finalValue)
                _uiState.value = CalculatorUiState.ShowingResult(input, result)
            } catch (e: Exception) {
                _uiState.value = CalculatorUiState.Error(e.message ?: "Calculation failed")
            }
        }
    }

    fun prefill(initialFund: Double, annualROI: Double, durationYears: Int, annualContribution: Double) {
        val input = CalculationInput(initialFund, annualROI, durationYears, annualContribution)
        _uiState.value = CalculatorUiState.ShowingResult(input, computeCompoundGrowthUseCase(input))
    }
}
```

- [ ] **Step 4: Run tests to confirm PASS**
```
./gradlew test --tests "*.CalculatorViewModelTest"
```
Expected: 3 tests PASSED

- [ ] **Step 5: Commit**
```bash
git add app/src/main/java/com/example/assetmanagement/calculator/ui/calculator/CalculatorViewModel.kt \
        app/src/test/java/com/example/assetmanagement/calculator/ui/calculator/CalculatorViewModelTest.kt
git commit -m "feat: implement CalculatorViewModel with TDD"
```

---

### Task 9: HistoryViewModel (TDD)

**Files:**
- Create: `app/src/main/java/com/example/assetmanagement/calculator/ui/history/HistoryViewModel.kt`
- Test: `app/src/test/java/com/example/assetmanagement/calculator/ui/history/HistoryViewModelTest.kt`

- [ ] **Step 1: Write failing tests**

Create `app/src/test/java/com/example/assetmanagement/calculator/ui/history/HistoryViewModelTest.kt`:
```kotlin
package com.example.assetmanagement.calculator.ui.history

import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.model.HistoryItem
import com.example.assetmanagement.calculator.domain.repository.CalculationRepository
import com.example.assetmanagement.calculator.domain.usecase.GetCalculationHistoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val itemsFlow = MutableSharedFlow<List<HistoryItem>>(replay = 1)

    private val fakeRepo = object : CalculationRepository {
        override suspend fun save(input: CalculationInput, finalValue: Double) {}
        override fun getAll(): Flow<List<HistoryItem>> = itemsFlow
        override suspend fun deleteById(id: Long) {}
    }

    private lateinit var viewModel: HistoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = HistoryViewModel(GetCalculationHistoryUseCase(fakeRepo), fakeRepo)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `initial state is Loading`() {
        assertTrue(viewModel.uiState.value is HistoryUiState.Loading)
    }

    @Test
    fun `empty list emits Empty`() = runTest {
        itemsFlow.emit(emptyList())
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is HistoryUiState.Empty)
    }

    @Test
    fun `non-empty list emits Success with correct items`() = runTest {
        val items = listOf(HistoryItem(1L, 100_000.0, 5.0, 10, 0.0, 162_889.0, 1_000_000L))
        itemsFlow.emit(items)
        advanceUntilIdle()
        val state = viewModel.uiState.value as HistoryUiState.Success
        assertTrue(state.items == items)
    }
}
```

- [ ] **Step 2: Run to confirm FAIL**
```
./gradlew test --tests "*.HistoryViewModelTest"
```
Expected: compilation error — class not found

- [ ] **Step 3: Implement HistoryViewModel**

Create `app/src/main/java/com/example/assetmanagement/calculator/ui/history/HistoryViewModel.kt`:
```kotlin
package com.example.assetmanagement.calculator.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assetmanagement.calculator.domain.model.HistoryItem
import com.example.assetmanagement.calculator.domain.repository.CalculationRepository
import com.example.assetmanagement.calculator.domain.usecase.GetCalculationHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HistoryUiState {
    object Loading : HistoryUiState
    data class Success(val items: List<HistoryItem>) : HistoryUiState
    object Empty : HistoryUiState
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getCalculationHistoryUseCase: GetCalculationHistoryUseCase,
    private val repository: CalculationRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = getCalculationHistoryUseCase()
        .map { items -> if (items.isEmpty()) HistoryUiState.Empty else HistoryUiState.Success(items) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState.Loading)

    fun delete(id: Long) {
        viewModelScope.launch { repository.deleteById(id) }
    }
}
```

- [ ] **Step 4: Run tests to confirm PASS**
```
./gradlew test --tests "*.HistoryViewModelTest"
```
Expected: 3 tests PASSED

- [ ] **Step 5: Commit**
```bash
git add app/src/main/java/com/example/assetmanagement/calculator/ui/history/HistoryViewModel.kt \
        app/src/test/java/com/example/assetmanagement/calculator/ui/history/HistoryViewModelTest.kt
git commit -m "feat: implement HistoryViewModel with TDD"
```

---

### Task 10: GrowthChartContent composable (Vico)

**Files:**
- Create: `app/src/main/java/com/example/assetmanagement/calculator/ui/calculator/GrowthChartContent.kt`

- [ ] **Step 1: Create GrowthChartContent**

Create `app/src/main/java/com/example/assetmanagement/calculator/ui/calculator/GrowthChartContent.kt`:
```kotlin
package com.example.assetmanagement.calculator.ui.calculator

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.assetmanagement.calculator.domain.model.YearlySnapshot
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

@Composable
fun GrowthChartContent(snapshots: List<YearlySnapshot>, modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(snapshots) {
        modelProducer.runTransaction {
            lineSeries {
                series(snapshots.map { it.totalValue.toFloat() })
                series(snapshots.map { it.totalContributed.toFloat() })
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

- [ ] **Step 2: Build to verify**
```
./gradlew compileDebugKotlin
```
Expected: BUILD SUCCESSFUL. If Vico axis API has changed, check the Vico 2.x migration guide and update the import paths for `VerticalAxis`/`HorizontalAxis`.

- [ ] **Step 3: Commit**
```bash
git add app/src/main/java/com/example/assetmanagement/calculator/ui/calculator/GrowthChartContent.kt
git commit -m "feat: add Vico growth chart composable"
```

---

### Task 11: CalculatorScreen composable

**Files:**
- Create: `app/src/main/java/com/example/assetmanagement/calculator/ui/calculator/CalculatorScreen.kt`

- [ ] **Step 1: Create CalculatorScreen**

Create `app/src/main/java/com/example/assetmanagement/calculator/ui/calculator/CalculatorScreen.kt`:
```kotlin
package com.example.assetmanagement.calculator.ui.calculator

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
import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.model.CalculationResult
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CalculatorScreen(
    prefillFund: Double = 0.0,
    prefillROI: Double = 0.0,
    prefillYears: Int = 0,
    prefillContribution: Double = 0.0,
    hasPrefill: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var fundText by remember { mutableStateOf("") }
    var roiText by remember { mutableStateOf("") }
    var yearsText by remember { mutableStateOf("") }
    var contributionText by remember { mutableStateOf("") }

    LaunchedEffect(hasPrefill) {
        if (hasPrefill) {
            fundText = prefillFund.toLong().toString()
            roiText = prefillROI.toString()
            yearsText = prefillYears.toString()
            contributionText = prefillContribution.toLong().toString()
            viewModel.prefill(prefillFund, prefillROI, prefillYears, prefillContribution)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Compound Calculator", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = fundText,
            onValueChange = { fundText = it },
            label = { Text("Initial Fund (NT\$)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = roiText,
            onValueChange = { roiText = it },
            label = { Text("Annual ROI (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = yearsText,
            onValueChange = { yearsText = it },
            label = { Text("Duration (years, 1–100)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = contributionText,
            onValueChange = { contributionText = it },
            label = { Text("Annual Contribution (NT\$, optional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.calculate(
                    CalculationInput(
                        initialFund = fundText.toDoubleOrNull() ?: 0.0,
                        annualROI = roiText.toDoubleOrNull() ?: 0.0,
                        durationYears = yearsText.toIntOrNull()?.coerceIn(1, 100) ?: 1,
                        annualContribution = contributionText.toDoubleOrNull() ?: 0.0
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Calculate") }

        Spacer(Modifier.height(24.dp))

        when (val s = state) {
            is CalculatorUiState.ShowingResult -> {
                GrowthChartContent(snapshots = s.result.yearlySnapshots)
                Spacer(Modifier.height(16.dp))
                ResultSummaryCard(result = s.result)
            }
            is CalculatorUiState.Error -> Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
            CalculatorUiState.Idle -> {}
        }
    }
}

@Composable
private fun ResultSummaryCard(result: CalculationResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Results", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            ResultRow("Final Value", result.finalValue)
            ResultRow("Total Contributed", result.totalContributed)
            ResultRow("Total Interest Earned", result.totalInterestEarned)
        }
    }
}

@Composable
private fun ResultRow(label: String, value: Double) {
    val formatted = NumberFormat.getNumberInstance(Locale.TAIWAN).format(value.toLong())
    Text("$label: NT\$$formatted")
}
```

- [ ] **Step 2: Build to verify**
```
./gradlew compileDebugKotlin
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**
```bash
git add app/src/main/java/com/example/assetmanagement/calculator/ui/calculator/CalculatorScreen.kt
git commit -m "feat: implement CalculatorScreen with input form, chart, and result summary"
```

---

### Task 12: HistoryScreen composable

**Files:**
- Create: `app/src/main/java/com/example/assetmanagement/calculator/ui/history/HistoryScreen.kt`

- [ ] **Step 1: Create HistoryScreen**

Create `app/src/main/java/com/example/assetmanagement/calculator/ui/history/HistoryScreen.kt`:
```kotlin
package com.example.assetmanagement.calculator.ui.history

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
import com.example.assetmanagement.calculator.domain.model.HistoryItem
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    onItemClick: (HistoryItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("History", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        when (val s = state) {
            HistoryUiState.Loading -> Text("Loading...")
            HistoryUiState.Empty -> Text("No calculations saved yet.")
            is HistoryUiState.Success -> {
                LazyColumn {
                    items(s.items, key = { it.id }) { item ->
                        SwipeToDismissHistoryCard(
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
private fun SwipeToDismissHistoryCard(item: HistoryItem, onDelete: () -> Unit, onClick: () -> Unit) {
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
    ) { HistoryItemCard(item = item, onClick = onClick) }
}

@Composable
private fun HistoryItemCard(item: HistoryItem, onClick: () -> Unit) {
    val numFmt = NumberFormat.getNumberInstance(Locale.TAIWAN)
    val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(dateFmt.format(Date(item.savedAt)), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text("NT\$${numFmt.format(item.initialFund.toLong())}  ROI: ${item.annualROI}%  ${item.durationYears}yr")
            Text("Final: NT\$${numFmt.format(item.finalValue.toLong())}", style = MaterialTheme.typography.titleSmall)
        }
    }
}
```

- [ ] **Step 2: Build to verify**
```
./gradlew compileDebugKotlin
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**
```bash
git add app/src/main/java/com/example/assetmanagement/calculator/ui/history/HistoryScreen.kt
git commit -m "feat: implement HistoryScreen with LazyColumn and swipe-to-delete"
```

---

### Task 13: Navigation — bottom nav with Calculator and History tabs

**Files:**
- Modify: `app/src/main/java/com/example/assetmanagement/NavigationKeys.kt`
- Modify: `app/src/main/java/com/example/assetmanagement/Navigation.kt`

- [ ] **Step 1: Add CalculatorKey and HistoryKey to NavigationKeys.kt**

Replace the full content of `NavigationKeys.kt`:
```kotlin
package com.example.assetmanagement

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey

@Serializable
data class CalculatorKey(
    val prefillFund: Double = 0.0,
    val prefillROI: Double = 0.0,
    val prefillYears: Int = 0,
    val prefillContribution: Double = 0.0,
    val hasPrefill: Boolean = false
) : NavKey

@Serializable data object HistoryKey : NavKey
```

- [ ] **Step 2: Replace MainNavigation with bottom-nav Scaffold**

Replace the full content of `Navigation.kt`:
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

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(CalculatorKey())
    val current = backStack.lastOrNull()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = current is CalculatorKey,
                    onClick = { backStack.clear(); backStack.add(CalculatorKey()) },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Calculator") }
                )
                NavigationBarItem(
                    selected = current == HistoryKey,
                    onClick = { backStack.clear(); backStack.add(HistoryKey) },
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
                entry<CalculatorKey> { key ->
                    CalculatorScreen(
                        prefillFund = key.prefillFund,
                        prefillROI = key.prefillROI,
                        prefillYears = key.prefillYears,
                        prefillContribution = key.prefillContribution,
                        hasPrefill = key.hasPrefill
                    )
                }
                entry<HistoryKey> {
                    HistoryScreen(
                        onItemClick = { item ->
                            backStack.clear()
                            backStack.add(
                                CalculatorKey(
                                    prefillFund = item.initialFund,
                                    prefillROI = item.annualROI,
                                    prefillYears = item.durationYears,
                                    prefillContribution = item.annualContribution,
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

- [ ] **Step 3: Full build and all tests**
```
./gradlew assembleDebug
./gradlew test
```
Expected: BUILD SUCCESSFUL, all tests PASS

- [ ] **Step 4: Commit**
```bash
git add app/src/main/java/com/example/assetmanagement/NavigationKeys.kt \
        app/src/main/java/com/example/assetmanagement/Navigation.kt
git commit -m "feat: wire bottom navigation with Calculator and History tabs"
```
