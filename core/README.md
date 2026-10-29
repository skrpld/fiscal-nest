# Fiscal Nest Core

[![CI](https://github.com/skrpld/fiscal-nest-core/actions/workflows/ci.yml/badge.svg)](https://github.com/skrpld/fiscal-nest-core/actions/workflows/ci.yml)

Stateless Kotlin engine that distributes income across expenses, a safety cushion and savings.

Fiscal Nest Core calculates how income should be distributed across mandatory expenses, optional
expenses, a safety cushion and a piggy bank. It runs on any JVM 17+ runtime and can be embedded in
Android apps (for example [fiscal-nest-android](https://github.com/skrpld/fiscal-nest-android)),
server-side services and CLI tools.

The engine is pure business logic: no UI, no database, no I/O, no formatting, no Android SDK.
Localization, currency formatting, persistence, display data (event names, categories) and manual
adjustments belong to the client. It has two modes: `WHAT_IF` takes aggregate amounts and returns
one distribution; `FORECAST` takes dated, recurring events and returns, per period, a plan, a cash
view, daily budget figures and carry-forward into the next period. All percentage-like values are
ratios on the `0.0–1.0` scale (`0.25` = 25%).

## Table of Contents

- [Install](#install)
- [Usage](#usage)
- [Development](#development)
- [Documentation](#documentation)
- [API](#api)
- [Contributing](#contributing)
- [License](#license)

## Install

Requires JDK 17+. The library is not published to Maven Central; install it into the local Maven
repository (`~/.m2`):

```sh
git clone https://github.com/skrpld/fiscal-nest-core
cd fiscal-nest-core
./gradlew publishToMavenLocal
```

Then add it to the client build (`build.gradle.kts`):

```kotlin
repositories {
    mavenLocal()
}

dependencies {
    implementation("io.github.skrpld:fiscal-nest-core:0.1.0-SNAPSHOT")
}
```

## Usage

Create one `EngineConfig` and pass it to every calculation. A `WHAT_IF` snapshot:

```kotlin
import fiscalnest.core.*
import java.math.BigDecimal
import java.math.RoundingMode

val config = EngineConfig(
    roundingMode = RoundingMode.HALF_UP,
    moneyScale = 2,
    percentageScale = 4,
    criticalityLevels = listOf(
        CriticalityLevel(
            name = "Critical",
            maxFillPct = BigDecimal("0.30"),
            topupMode = TopupMode.PERCENT_OF_TARGET,
            topupValue = BigDecimal("0.20"),
            admissibilityPct = BigDecimal("0.80")
        ),
        CriticalityLevel(
            name = "Warning",
            maxFillPct = BigDecimal("0.70"),
            topupMode = TopupMode.PERCENT_OF_REMAINDER,
            topupValue = BigDecimal("0.10"),
            admissibilityPct = BigDecimal("0.50")
        )
    ),
    piggyBankMode = PiggyBankMode.FIXED_AMOUNT,
    piggyBankTarget = BigDecimal("5000"),
    piggyBankAdmissibilityPct = BigDecimal("0.80"),
    cashReserves = setOf(CashReserve.CUSHION_TOPUP)
)

val result = BudgetCalculator.calculateWhatIf(
    WhatIfInput(
        income = BigDecimal("50000"),
        mandatory = BigDecimal("20000"),
        optional = BigDecimal("10000"),
        cushionState = CushionState(current = BigDecimal("5000"), target = BigDecimal("20000")),
        config = config
    )
)

result.expenseCrisis       // false
result.cushionCrisis       // true, level "Critical": fill ratio 0.25 < 0.30
result.cushionTopup        // 4000.00 = min(20% of target, 80% of 20000, need 15000)
result.cushionCurrent      // 9000.00 (after distribution)
result.piggyBankActual     // 5000.00 = min(5000, 80% of 16000)
result.freeRemainder       // 11000.00
```

Invalid input throws `IllegalArgumentException` before any calculation. The engine is stateless
and thread-safe: share one `EngineConfig` and call `BudgetCalculator` from any number of threads.

Multi-period projection from dated events: [Project a multi-period forecast](docs/how-to/project-a-multi-period-forecast.md).

## Development

The project is a Gradle Kotlin/JVM module (Kotlin 2.4.20, JVM 17 bytecode, JUnit Jupiter;
warnings are errors).

| Task | Command |
|---|---|
| Compile and run all tests | `./gradlew build` |
| Tests only | `./gradlew test` |
| Install into `~/.m2` | `./gradlew publishToMavenLocal` |

CI ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)) runs `./gradlew build` on JDK 17 and 21
for every push to `main` and every pull request.

Sources are in `src/main/kotlin/fiscalnest/core/`: `BudgetCalculator.kt` is the public facade;
`DistributionEngine`, `CalendarEngine`, `DecimalUtils` and `InputValidator` are internal.

## Documentation

See [docs/](docs/README.md). The [locked specification](docs/reference/specification.md) (v1.2) is
the single source of truth and prevails over every other document.

## API

Entry points of the `fiscalnest.core` package:

```kotlin
object BudgetCalculator {
    fun calculateWhatIf(input: WhatIfInput): DistributionResult
    fun calculateForecast(input: ForecastInput): List<ForecastResult>
}
```

All public types, fields and exception messages: [docs/reference/api.md](docs/reference/api.md).

## Contributing

Questions and bug reports: [issues](https://github.com/skrpld/fiscal-nest-core/issues). Pull requests
are welcome. Requirements:

- Code complies with the [locked specification](docs/reference/specification.md); a deviation is a
  bug. Changing the specification needs an amendment with a documented reason in its §0.
- Every `.kt` file starts with the license header `Copyright 2026 skrpld` +
  `SPDX-License-Identifier: Apache-2.0`; every public declaration has KDoc.
- `./gradlew build` passes.

## License

[Apache-2.0](LICENSE) © skrpld. Redistributions must keep the [NOTICE](NOTICE) file.
