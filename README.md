# Fiscal Nest

[![Core](https://github.com/skrpld/fiscal-nest/actions/workflows/core.yml/badge.svg)](https://github.com/skrpld/fiscal-nest/actions/workflows/core.yml)
[![Android](https://github.com/skrpld/fiscal-nest/actions/workflows/android.yml/badge.svg)](https://github.com/skrpld/fiscal-nest/actions/workflows/android.yml)
[![License](https://img.shields.io/github/license/skrpld/fiscal-nest)](LICENSE)

Offline budget planner: distributes income across expenses, a safety cushion and savings, and tells
how much can safely be spent today.

- **Engine**: a stateless Kotlin/JVM library. `WHAT_IF` distributes aggregate amounts once;
  `FORECAST` takes dated, recurring income and expenses and returns, per period, a plan, a cash
  view, daily budget figures and carry-forward into the next period. No I/O, no UI, no Android SDK.
- **Android app**: runs the engine on the device and keeps all data there: no account, no network.
  Overview with the safe daily budget and forecast, events, envelopes, what-if, ten UI languages.

The repository holds two components: [`core/`](core) (library `io.github.skrpld:fiscal-nest-core`)
and [`android/`](android) (Android app that builds the engine from `core/`).

## Table of Contents

- [Install](#install)
- [Usage](#usage)
- [Development](#development)
- [Documentation](#documentation)
- [Contributing](#contributing)
- [License](#license)

## Install

Android app: requires Android 8.0 (API 26)+. Install `fiscal-nest-<VERSION>.apk` from the latest
[`android-v*` release](https://github.com/skrpld/fiscal-nest/releases); see
[android/README.md](android/README.md).

Engine: requires JDK 17+. It is not published to Maven Central; install it into the local Maven
repository and depend on `io.github.skrpld:fiscal-nest-core:0.1.0-SNAPSHOT`
([details](core/README.md#install)):

```sh
git clone https://github.com/skrpld/fiscal-nest
cd fiscal-nest/core
./gradlew publishToMavenLocal
```

## Usage

In the app, follow the getting started guide on the first launch, add income and expenses on
**Events** and open **Overview** for the safe daily budget, the period plan and the forecast
([more](android/README.md#usage)).

The engine, from Kotlin:

```kotlin
val result = BudgetCalculator.calculateWhatIf(
    WhatIfInput(
        income = BigDecimal("50000"),
        mandatory = BigDecimal("20000"),
        optional = BigDecimal("10000"),
        cushionState = CushionState(current = BigDecimal("5000"), target = BigDecimal("20000")),
        config = config
    )
)
result.cushionTopup     // 4000.00
result.freeRemainder    // 11000.00
```

The full example with `EngineConfig`: [core/README.md](core/README.md#usage).

## Development

```sh
# Engine (JDK 17+)
cd core
./gradlew build

# Android app (JDK 17+, Android SDK platform 37)
cd android
./gradlew :domain:test :app:testDebugUnitTest :app:assembleDebug
```

```text
fiscal-nest/
├── core/                        # engine: standalone Gradle build, src/main/kotlin/fiscalnest/core
├── android/                     # Android app: Gradle build with :domain and :app, includes ../core
├── docs/                        # documentation
└── .github/
    ├── workflows/core.yml       # engine build and tests
    ├── workflows/android.yml    # tests, lint, APKs; release on android-v* tags
    └── scripts/                 # release checks and publishing
```

The Android build includes `core/` as a Gradle composite build, so every change under `core/src/main`
also ships in the app and triggers the Android workflow. Releases:
[Publish a release](docs/how-to/publish-a-release.md).

## Documentation

See [docs/](docs/README.md). The [locked specification](docs/reference/specification.md) (v1.2) is
the single source of truth for the engine and prevails over every other document.

## Contributing

Questions and bug reports: [issues](https://github.com/skrpld/fiscal-nest/issues). Pull requests
are welcome; CI must pass. Commits follow [Conventional Commits](https://www.conventionalcommits.org/)
with a scope such as `core`, `android`, `app`, `envelopes` or `ci`. Engine changes follow the
[engine's rules](core/README.md#contributing) (spec compliance, license headers, KDoc).

## License

[Apache-2.0](LICENSE) © skrpld. Redistributions must keep the [NOTICE](NOTICE) file.
