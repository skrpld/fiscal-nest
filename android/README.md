# Fiscal Nest for Android

[![Android](https://github.com/skrpld/fiscal-nest/actions/workflows/android.yml/badge.svg)](https://github.com/skrpld/fiscal-nest/actions/workflows/android.yml)

Offline Android budget planner built on Fiscal Nest Core: no account, no network.

The app (installed as **Fiscal Nest**) runs [Fiscal Nest Core](../core)
on the device and keeps all data there. It answers three questions every day:

- **How much can I safely spend today?** Cash on hand after upcoming mandatory payments and the
  reserves you choose, divided by the days left in the period.
- **How is this period's money allocated?** Income minus mandatory and optional expenses, then the
  safety cushion top-up, the piggy bank and the free remainder.
- **Where is it heading?** A multi-period forecast with carry-forward of free money and cushion balance.

Five screens: **Overview** (safe daily budget, cash view, period plan, cushion progress, off-plan
spending, crisis warnings, forecast), **Events** (one-time and recurring income and expenses),
**Envelopes** (cards, accounts and deposits with spending rules), **What if** (distributes any
amounts with your budget rules without saving anything) and **Settings**. Material 3 with dynamic
color on Android 12+, adaptive layout for phones, tablets and foldables, ten UI languages (English,
Russian, Chinese (Simplified), Spanish, Portuguese (Brazil), French, German, Hindi, Arabic,
Japanese).

## Table of Contents

- [Install](#install)
- [Usage](#usage)
- [Development](#development)
- [Architecture](#architecture)
- [Release](#release)
- [Contributing](#contributing)
- [License](#license)

## Install

Requires Android 8.0 (API 26) or newer.

Download `fiscal-nest-<VERSION>.apk` from the latest
[`android-v*` release](https://github.com/skrpld/fiscal-nest/releases) and open it on the device
(allow installs from unknown sources when asked). `<VERSION>` is the release number, for example
`0.3.0`. Each release also has a `SHA256SUMS` file.

### Build from source

Requires JDK 17+ (CI uses 21) and Android SDK platform 37. The Gradle wrapper downloads Gradle 9.8.0.

```sh
git clone https://github.com/skrpld/fiscal-nest
cd fiscal-nest/android
./gradlew :app:assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

The debug build has the application ID `io.github.skrpld.fiscalnest.debug`, so it installs next to
a release build.

## Usage

1. On the first launch, follow the getting started guide: it explains events, the daily budget and
   savings, then offers a quick setup of the monthly income and pay day (which also align the budget
   period) and the cushion balance and target. Every field is optional; the guide can be reopened
   from Settings.
2. Add income and expenses on **Events**: one-time, every N days, or every N months on a day
   (31 = last day of the month), with an optional end date. Mark each expense mandatory or optional.
3. Open **Overview** to see the safe daily budget, the period plan and the forecast.
4. Log off-plan purchases from the overview or the spending log; they count as already spent in the
   current period.
5. Optionally add **Envelopes** for money you already have. Each has a role (spending, safety
   cushion, piggy bank or none) and a spending rule (any time, a limit per budget period, locked
   until a date, or locked until a goal is reached). When envelopes have the safety cushion role,
   their total replaces the cushion balance from Settings.

**Settings** holds the budget period (monthly from a pay day, or a fixed length), forecast horizon,
currency, cents, cushion and piggy bank rules, cash reserves, language, theme, and JSON backup and
restore. On Android 13+ the language can also be chosen in the system per-app language settings.

## Development

Run the commands from `android/`.

| Task | Command |
|---|---|
| Client logic tests (plain JVM) | `./gradlew :domain:test` |
| View model and Robolectric UI tests | `./gradlew :app:testDebugUnitTest` |
| Android lint | `./gradlew :app:lintDebug` |
| Debug APK | `./gradlew :app:assembleDebug` (`app/build/outputs/apk/debug/app-debug.apk`) |
| Minified release APK | `./gradlew :app:assembleRelease` (see [Release](#release) for signing) |

Toolchain, from [`gradle/libs.versions.toml`](gradle/libs.versions.toml): Android Gradle Plugin
9.3.2, Kotlin 2.4.20, compileSdk and targetSdk 37, minSdk 26, JVM target 17.

The engine is not copied into this build: [`settings.gradle.kts`](settings.gradle.kts) includes
[`../core`](../core) as a composite build, and `:domain` depends on its coordinates
`io.github.skrpld:fiscal-nest-core`, which Gradle substitutes with that build. Engine tests run in
`core/` (`./gradlew test`).

## Architecture

```text
../core/  Fiscal Nest Core engine (pure Kotlin/JVM), a separate Gradle build included here
domain/   Pure Kotlin client logic: models, JSON format, period resolution, engine mapping,
          input parsing and validation, formatters, repository contract
app/      Android: DataStore persistence, ViewModels, Compose UI, theme, resources
```

- Single source of truth: one `AppData` document in DataStore. `domain` defines its JSON format,
  which is also the backup format.
- Unidirectional data flow: repositories expose `Flow`, view models expose `StateFlow`; form text
  lives in Compose state.
- Manual dependency injection through `AppContainer`.
- Type-safe Navigation Compose routes.

## Release

The [Android workflow](../.github/workflows/android.yml) runs on every push and pull request that
touches `android/` or the engine sources in `core/`: tests and Android lint, then the debug and
release APKs, uploaded as a run artifact. A tag `android-vX.Y.Z` (or a manual run on `main` with
`release`) builds the release APK with that version and publishes `fiscal-nest-X.Y.Z.apk` as a
GitHub release: [Publish a release](../docs/how-to/publish-a-release.md).

Without signing secrets, release APKs are signed with the CI runner's temporary debug key: they
install, but cannot update each other. To sign with your own key, add these repository secrets:

| Secret | Value |
|--------|-------|
| `FISCAL_NEST_KEYSTORE_BASE64` | output of `base64 -w0 release.jks` |
| `FISCAL_NEST_KEYSTORE_PASSWORD` | keystore password |
| `FISCAL_NEST_KEY_ALIAS` | key alias |
| `FISCAL_NEST_KEY_PASSWORD` | key password |

For a signed local build, set `FISCAL_NEST_KEYSTORE_FILE` to the keystore path, plus the last three
variables above, before running `./gradlew :app:assembleRelease`.

## Contributing

Questions and bug reports: [issues](https://github.com/skrpld/fiscal-nest/issues). Pull requests
are welcome; CI must pass. Engine changes go to [`core/`](../core) and must follow its
[contributing rules](../core/README.md#contributing).

## License

[Apache-2.0](../LICENSE) © skrpld. See [NOTICE](../NOTICE).
