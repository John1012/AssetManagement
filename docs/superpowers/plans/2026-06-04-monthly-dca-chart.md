# Monthly DCA Comparison Chart Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace `annualContribution` with `monthlyContribution` throughout the app, and add a three-line DCA comparison chart (with DCA / without DCA / total contributed) shown when monthly contribution > 0.

**Architecture:** Domain-first — update `CalculationInput`, `CalculationResult`, and `ComputeCompoundGrowthUseCase` in Task 1. Data layer follows in Task 2. UI last in Task 3. Each task leaves the codebase fully compilable and all tests passing.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Hilt, Vico 2.0.0 (chart library), JUnit 4, kotlinx-coroutines-test

---

## File Map

| File | Task | Change |
|------|------|--------|
| `domain/model/CalculationInput.kt` | 1 | `annualContribution` → `monthlyContribution` |
| `domain/model/CalculationResult.kt` | 1 | Add `baselineSnapshots: List<YearlySnapshot>?` |
| `domain/model/HistoryItem.kt` | 1 | `annualContribution` → `monthlyContribution` |
| `domain/usecase/ComputeCompoundGrowthUseCase.kt` | 1 | Use `monthlyContribution * 12`, compute baseline |
| `test/.../ComputeCompoundGrowthUseCaseTest.kt` | 1 | Rewrite for new API + baseline assertions |
| `data/local/CalculationEntity.kt` | 2 | `annualContribution` → `monthlyContribution` |
| `data/local/CalculationDatabase.kt` | 2 | Version 1 → 2 |
| `data/CalculatorModule.kt` | 2 | Add `fallbackToDestructiveMigration()` |
| `data/CalculationRepositoryImpl.kt` | 2 | Use `monthlyContribution` |
| `test/.../CalculationRepositoryImplTest.kt` | 2 | Update field names |
| `ui/calculator/CalculatorScreen.kt` | 3 | Label + field name update, pass `baselineSnapshots` to chart |
| `ui/calculator/GrowthChartContent.kt` | 3 | Three-line chart + legend |
| `Navigation.kt` | 3 | Prefill mapping update |

---

## Task 1: Domain Layer — Models + UseCase

**Files:**
- Modify: `app/src/main/java/com/example/assetmanagement/calculator/domain/model/CalculationInput.kt`
- Modify: `app/src/main/java/com/example/assetmanagement/calculator/domain/model/CalculationResult.kt`
- Modify: `app/src/main/java/com/example/assetmanagement/calculator/domain/model/HistoryItem.kt`
- Modify: `app/src/main/java/com/example/assetmanagement/calculator/domain/usecase/ComputeCompoundGrowthUseCase.kt`
- Modify: `app/src/test/java/com/example/assetmanagement/calculator/domain/usecase/ComputeCompoundGrowthUseCaseTest.kt`

- [ ] **Step 1: Rewrite the UseCase test file**

Replace the entire file content:

```kotlin
package com.example.assetmanagement.calculator.domain.usecase

import com.example.assetmanagement.calculator.domain.model.CalculationInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ComputeCompoundGrowthUseCaseTest {

    private val useCase = ComputeCompoundGrowthUseCase()

    @Test
    fun `single year no monthly contribution`() {
        val result = useCase(CalculationInput(100_000.0, 10.0, 1, 0.0))
        assertEquals(110_000.0, result.finalValue, 0.01)
        assertEquals(100_000.0, result.totalContributed, 0.01)
        assertEquals(10_000.0, result.totalInterestEarned, 0.01)
        assertEquals(1, result.yearlySnapshots.size)
        assertNull(result.baselineSnapshots)
    }

    @Test
    fun `three years no monthly contribution compounds correctly`() {
        val result = useCase(CalculationInput(100_000.0, 10.0, 3, 0.0))
        assertEquals(133_100.0, result.finalValue, 0.01)
        assertEquals(110_000.0, result.yearlySnapshots[0].totalValue, 0.01)
        assertEquals(121_000.0, result.yearlySnapshots[1].totalValue, 0.01)
        assertEquals(133_100.0, result.yearlySnapshots[2].totalValue, 0.01)
        assertNull(result.baselineSnapshots)
    }

    @Test
    fun `with monthly contribution of 1000 per month`() {
        // monthlyContribution = 1000, annualEquivalent = 12000
        // Year 1: (100000 + 12000) * 1.1 = 123200
        // Year 2: (123200 + 12000) * 1.1 = 148720
        val result = useCase(CalculationInput(100_000.0, 10.0, 2, 1_000.0))
        assertEquals(148_720.0, result.finalValue, 0.01)
        assertEquals(124_000.0, result.totalContributed, 0.01)
        assertEquals(24_720.0, result.totalInterestEarned, 0.01)
        assertNotNull(result.baselineSnapshots)
    }

    @Test
    fun `baseline snapshots reflect growth without monthly contributions`() {
        val result = useCase(CalculationInput(100_000.0, 10.0, 2, 1_000.0))
        val baseline = result.baselineSnapshots!!
        // Baseline Year 1: 100000 * 1.1 = 110000
        // Baseline Year 2: 110000 * 1.1 = 121000
        assertEquals(2, baseline.size)
        assertEquals(110_000.0, baseline[0].totalValue, 0.01)
        assertEquals(121_000.0, baseline[1].totalValue, 0.01)
        assertEquals(100_000.0, baseline[0].totalContributed, 0.01)
        assertEquals(100_000.0, baseline[1].totalContributed, 0.01)
    }

    @Test
    fun `zero ROI with monthly contribution returns sum of contributions`() {
        val result = useCase(CalculationInput(100_000.0, 0.0, 3, 500.0))
        // annual = 6000/year; Y1=106000, Y2=112000, Y3=118000
        assertEquals(118_000.0, result.finalValue, 0.01)
        assertEquals(0.0, result.totalInterestEarned, 0.01)
    }

    @Test
    fun `snapshot totalContributed tracks principal plus monthly contributions per year`() {
        val result = useCase(CalculationInput(50_000.0, 5.0, 2, 1_000.0))
        // annual = 12000/year
        // Y1 contributed: 50000 + 12000 = 62000
        // Y2 contributed: 50000 + 24000 = 74000
        assertEquals(62_000.0, result.yearlySnapshots[0].totalContributed, 0.01)
        assertEquals(74_000.0, result.yearlySnapshots[1].totalContributed, 0.01)
    }
}
```

- [ ] **Step 2: Update `CalculationInput.kt`**

```kotlin
package com.example.assetmanagement.calculator.domain.model

data class CalculationInput(
    val initialFund: Double,
    val annualROI: Double,
    val durationYears: Int,
    val monthlyContribution: Double = 0.0
)
```

- [ ] **Step 3: Update `CalculationResult.kt`**

```kotlin
package com.example.assetmanagement.calculator.domain.model

data class CalculationResult(
    val finalValue: Double,
    val totalContributed: Double,
    val totalInterestEarned: Double,
    val yearlySnapshots: List<YearlySnapshot>,
    val baselineSnapshots: List<YearlySnapshot>?
)
```

- [ ] **Step 4: Update `HistoryItem.kt`**

```kotlin
package com.example.assetmanagement.calculator.domain.model

data class HistoryItem(
    val id: Long,
    val initialFund: Double,
    val annualROI: Double,
    val durationYears: Int,
    val monthlyContribution: Double,
    val finalValue: Double,
    val savedAt: Long
)
```

- [ ] **Step 5: Update `ComputeCompoundGrowthUseCase.kt`**

```kotlin
package com.example.assetmanagement.calculator.domain.usecase

import com.example.assetmanagement.calculator.domain.model.CalculationInput
import com.example.assetmanagement.calculator.domain.model.CalculationResult
import com.example.assetmanagement.calculator.domain.model.YearlySnapshot
import javax.inject.Inject

class ComputeCompoundGrowthUseCase @Inject constructor() {

    operator fun invoke(input: CalculationInput): CalculationResult {
        val rate = 1.0 + input.annualROI / 100.0
        val annualContribution = input.monthlyContribution * 12
        var value = input.initialFund
        val snapshots = mutableListOf<YearlySnapshot>()

        for (year in 1..input.durationYears) {
            value = (value + annualContribution) * rate
            val contributed = input.initialFund + annualContribution * year
            snapshots.add(
                YearlySnapshot(
                    year = year,
                    totalValue = value,
                    totalContributed = contributed,
                    totalInterestEarned = value - contributed
                )
            )
        }

        val totalContributed = input.initialFund + annualContribution * input.durationYears

        val baselineSnapshots = if (input.monthlyContribution > 0.0) {
            var baseValue = input.initialFund
            (1..input.durationYears).map { year ->
                baseValue *= rate
                YearlySnapshot(
                    year = year,
                    totalValue = baseValue,
                    totalContributed = input.initialFund,
                    totalInterestEarned = baseValue - input.initialFund
                )
            }
        } else null

        return CalculationResult(
            finalValue = value,
            totalContributed = totalContributed,
            totalInterestEarned = value - totalContributed,
            yearlySnapshots = snapshots,
            baselineSnapshots = baselineSnapshots
        )
    }
}
```

- [ ] **Step 6: Run the UseCase tests**

```
./gradlew :app:testDebugUnitTest --tests "com.example.assetmanagement.calculator.domain.usecase.ComputeCompoundGrowthUseCaseTest" -q
```

Expected: 6 tests PASS. If there are compilation errors in other files referencing `annualContribution`, fix them before running (see Step 7 note).

- [ ] **Step 7: Fix remaining compilation errors**

After renaming `annualContribution` in the models, these files have named argument references that won't compile:
- `CalculationRepositoryImpl.kt` — references `input.annualContribution` and `annualContribution =` in entity construction (will be fixed in Task 2)
- `CalculatorScreen.kt` — references `prefillContribution` which maps correctly (no named arg issue, positional)
- `CalculatorViewModel.kt` — uses positional args only, no fix needed

Run a full compile check:
```
./gradlew :app:compileDebugKotlin 2>&1 | grep -i "error" | head -20
```

If `CalculationRepositoryImpl.kt` errors appear, that's expected — they'll be fixed in Task 2. All other errors must be fixed now.

- [ ] **Step 8: Commit Task 1**

```bash
git add app/src/main/java/com/example/assetmanagement/calculator/domain/model/CalculationInput.kt \
        app/src/main/java/com/example/assetmanagement/calculator/domain/model/CalculationResult.kt \
        app/src/main/java/com/example/assetmanagement/calculator/domain/model/HistoryItem.kt \
        app/src/main/java/com/example/assetmanagement/calculator/domain/usecase/ComputeCompoundGrowthUseCase.kt \
        app/src/test/java/com/example/assetmanagement/calculator/domain/usecase/ComputeCompoundGrowthUseCaseTest.kt
git commit -m "feat: replace annualContribution with monthlyContribution in domain layer, add baselineSnapshots"
```

---

## Task 2: Data Layer — Entity, Database, Repository

**Files:**
- Modify: `app/src/main/java/com/example/assetmanagement/calculator/data/local/CalculationEntity.kt`
- Modify: `app/src/main/java/com/example/assetmanagement/calculator/data/local/CalculationDatabase.kt`
- Modify: `app/src/main/java/com/example/assetmanagement/calculator/data/CalculatorModule.kt`
- Modify: `app/src/main/java/com/example/assetmanagement/calculator/data/CalculationRepositoryImpl.kt`
- Modify: `app/src/test/java/com/example/assetmanagement/calculator/data/CalculationRepositoryImplTest.kt`

- [ ] **Step 1: Rewrite the Repository test file**

Replace the entire file:

```kotlin
package com.example.assetmanagement.calculator.data

import com.example.assetmanagement.calculator.data.local.CalculationDao
import com.example.assetmanagement.calculator.data.local.CalculationEntity
import com.example.assetmanagement.calculator.domain.model.CalculationInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculationRepositoryImplTest {

    private val stored = mutableListOf<CalculationEntity>()

    private val fakeDao = object : CalculationDao {
        override suspend fun insert(entity: CalculationEntity) {
            stored += entity.copy(id = stored.size.toLong() + 1)
        }
        override fun getAll(): Flow<List<CalculationEntity>> = flowOf(stored.toList())
        override suspend fun deleteById(id: Long) {
            stored.removeIf { it.id == id }
        }
    }

    private val repo = CalculationRepositoryImpl(fakeDao)

    @Test
    fun `save maps all CalculationInput fields onto the entity`() = runTest {
        val input = CalculationInput(
            initialFund = 100_000.0,
            annualROI = 7.5,
            durationYears = 20,
            monthlyContribution = 1_200.0
        )

        repo.save(input, finalValue = 450_000.0)

        val saved = stored.single()
        assertEquals(100_000.0, saved.initialFund, 0.0)
        assertEquals(7.5, saved.annualROI, 0.0)
        assertEquals(20, saved.durationYears)
        assertEquals(1_200.0, saved.monthlyContribution, 0.0)
    }

    @Test
    fun `save records finalValue and a current timestamp on the entity`() = runTest {
        val input = CalculationInput(initialFund = 50_000.0, annualROI = 5.0, durationYears = 10)
        val timeBefore = System.currentTimeMillis()

        repo.save(input, finalValue = 81_444.73)

        val timeAfter = System.currentTimeMillis()
        val saved = stored.single()
        assertEquals(81_444.73, saved.finalValue, 0.01)
        assertTrue(saved.savedAt in timeBefore..timeAfter)
    }

    @Test
    fun `getAll maps every CalculationEntity field to HistoryItem`() = runTest {
        stored += CalculationEntity(
            id = 1L,
            initialFund = 100_000.0,
            annualROI = 5.0,
            durationYears = 10,
            monthlyContribution = 500.0,
            finalValue = 162_889.0,
            savedAt = 99_999L
        )

        val result = repo.getAll().first()

        assertEquals(1, result.size)
        val item = result[0]
        assertEquals(1L, item.id)
        assertEquals(100_000.0, item.initialFund, 0.0)
        assertEquals(5.0, item.annualROI, 0.0)
        assertEquals(10, item.durationYears)
        assertEquals(500.0, item.monthlyContribution, 0.0)
        assertEquals(162_889.0, item.finalValue, 0.0)
        assertEquals(99_999L, item.savedAt)
    }

    @Test
    fun `getAll on empty dao returns empty list`() = runTest {
        val result = repo.getAll().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `deleteById delegates to dao`() = runTest {
        stored += CalculationEntity(
            id = 1L, initialFund = 1.0, annualROI = 1.0, durationYears = 1,
            monthlyContribution = 0.0, finalValue = 1.0, savedAt = 1_000L
        )

        repo.deleteById(1L)

        assertTrue(stored.isEmpty())
    }
}
```

- [ ] **Step 2: Update `CalculationEntity.kt`**

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
    val monthlyContribution: Double,
    val finalValue: Double,
    val savedAt: Long
)
```

- [ ] **Step 3: Update `CalculationDatabase.kt`** (bump version to 2)

```kotlin
package com.example.assetmanagement.calculator.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CalculationEntity::class], version = 2, exportSchema = false)
abstract class CalculationDatabase : RoomDatabase() {
    abstract fun calculationDao(): CalculationDao
}
```

- [ ] **Step 4: Update `CalculatorModule.kt`** (add destructive migration)

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
            Room.databaseBuilder(context, CalculationDatabase::class.java, "calculations.db")
                .fallbackToDestructiveMigration()
                .build()

        @Provides
        fun provideCalculationDao(db: CalculationDatabase): CalculationDao = db.calculationDao()
    }
}
```

- [ ] **Step 5: Update `CalculationRepositoryImpl.kt`**

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
                monthlyContribution = input.monthlyContribution,
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
        monthlyContribution = monthlyContribution,
        finalValue = finalValue,
        savedAt = savedAt
    )
}
```

- [ ] **Step 6: Run data layer tests**

```
./gradlew :app:testDebugUnitTest --tests "com.example.assetmanagement.calculator.data.CalculationRepositoryImplTest" -q
```

Expected: 5 tests PASS.

- [ ] **Step 7: Commit Task 2**

```bash
git add app/src/main/java/com/example/assetmanagement/calculator/data/local/CalculationEntity.kt \
        app/src/main/java/com/example/assetmanagement/calculator/data/local/CalculationDatabase.kt \
        app/src/main/java/com/example/assetmanagement/calculator/data/CalculatorModule.kt \
        app/src/main/java/com/example/assetmanagement/calculator/data/CalculationRepositoryImpl.kt \
        app/src/test/java/com/example/assetmanagement/calculator/data/CalculationRepositoryImplTest.kt
git commit -m "feat: update data layer — rename annualContribution to monthlyContribution, bump DB version"
```

---

## Task 3: UI Layer — CalculatorScreen, GrowthChartContent, Navigation

**Files:**
- Modify: `app/src/main/java/com/example/assetmanagement/calculator/ui/calculator/CalculatorScreen.kt`
- Modify: `app/src/main/java/com/example/assetmanagement/calculator/ui/calculator/GrowthChartContent.kt`
- Modify: `app/src/main/java/com/example/assetmanagement/Navigation.kt`

- [ ] **Step 1: Update `CalculatorScreen.kt`**

Two changes: label text (`Annual Contribution` → `Monthly Contribution`) and the `CalculationInput` constructor call passes `monthlyContribution`.

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
            label = { Text("Monthly Contribution (NT\$, optional)") },
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
                        monthlyContribution = contributionText.toDoubleOrNull() ?: 0.0
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Calculate") }

        Spacer(Modifier.height(24.dp))

        when (val s = state) {
            is CalculatorUiState.ShowingResult -> {
                GrowthChartContent(
                    snapshots = s.result.yearlySnapshots,
                    baselineSnapshots = s.result.baselineSnapshots
                )
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

- [ ] **Step 2: Rewrite `GrowthChartContent.kt`**

Three-line chart with legend when `baselineSnapshots != null`, two-line chart otherwise. All three line colors are always defined so `rememberLine` is never called conditionally.

```kotlin
package com.example.assetmanagement.calculator.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.assetmanagement.calculator.domain.model.YearlySnapshot
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill

private val dcaColor = Color(0xFF1976D2)
private val noDcaColor = Color(0xFFE64A19)
private val contributedColor = Color(0xFF388E3C)

@Composable
fun GrowthChartContent(
    snapshots: List<YearlySnapshot>,
    baselineSnapshots: List<YearlySnapshot>?,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    val line1 = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(dcaColor.toArgb()))
    )
    val line2 = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(noDcaColor.toArgb()))
    )
    val line3 = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(contributedColor.toArgb()))
    )
    val layer = rememberLineCartesianLayer(
        LineCartesianLayer.LineProvider.series(line1, line2, line3)
    )

    LaunchedEffect(snapshots, baselineSnapshots) {
        modelProducer.runTransaction {
            lineSeries {
                if (baselineSnapshots != null) {
                    series(snapshots.map { it.totalValue.toFloat() })
                    series(baselineSnapshots.map { it.totalValue.toFloat() })
                    series(snapshots.map { it.totalContributed.toFloat() })
                } else {
                    series(snapshots.map { it.totalValue.toFloat() })
                    series(snapshots.map { it.totalContributed.toFloat() })
                }
            }
        }
    }

    Column(modifier = modifier) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                layer,
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(),
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )
        if (baselineSnapshots != null) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LegendItem("With DCA", dcaColor)
                LegendItem("Without DCA", noDcaColor)
                LegendItem("Contributed", contributedColor)
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
```

- [ ] **Step 3: Update `Navigation.kt`** (one line: `item.annualContribution` → `item.monthlyContribution`)

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
                                    prefillContribution = item.monthlyContribution,
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

- [ ] **Step 4: Run all unit tests to confirm no regressions**

```
./gradlew :app:testDebugUnitTest -q
```

Expected: All tests PASS (ComputeCompoundGrowthUseCaseTest × 6, CalculationRepositoryImplTest × 5, SaveCalculationUseCaseTest × 1, GetCalculationHistoryUseCaseTest × 1, CalculatorViewModelTest × 3, HistoryViewModelTest × N).

- [ ] **Step 5: Compile check**

```
./gradlew :app:compileDebugKotlin -q
```

Expected: BUILD SUCCESSFUL with no errors.

- [ ] **Step 6: Commit Task 3**

```bash
git add app/src/main/java/com/example/assetmanagement/calculator/ui/calculator/CalculatorScreen.kt \
        app/src/main/java/com/example/assetmanagement/calculator/ui/calculator/GrowthChartContent.kt \
        app/src/main/java/com/example/assetmanagement/Navigation.kt
git commit -m "feat: add monthly DCA comparison chart with three-line display and legend"
```
