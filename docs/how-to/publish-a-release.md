# Publish a release

The Android app is released from this repository as GitHub releases tagged
`android-vX.Y.Z`. A release is cut either by pushing a tag or by running the
workflow manually on `main` (see [Release from a manual run](#release-from-a-manual-run));
GitHub Actions tests, builds and publishes it. Both ways give the same tag,
checks and assets.

| Component | Tag | Workflow | Release assets |
|---|---|---|---|
| Android app | `android-vX.Y.Z` | [`android.yml`](../../.github/workflows/android.yml) | `fiscal-nest-X.Y.Z.apk`, `SHA256SUMS` |
| Engine | — | [`core.yml`](../../.github/workflows/core.yml) | not released; install it with `publishToMavenLocal` ([core/README.md](../../core/README.md#install)) |

A suffix (`android-v0.3.0-rc.1`) publishes a pre-release. Release notes are
generated from the pull requests and commits since the previous `android-v*`
tag.

## Android

1. Optionally bump `fiscalnest.versionName` in
   [`android/gradle.properties`](../../android/gradle.properties) (used by
   branch builds; a release takes its version from the tag).
2. Commit and merge to `main`.
3. Tag and push:

   ```bash
   git tag android-v0.3.0
   git push origin android-v0.3.0
   ```

`versionName` comes from the tag; `versionCode` is the workflow run number
plus 100, so every build is newer than the previous one and than the releases
of the former `fiscal-nest-android` repository.

### Signing

Android installs an update only when it is signed with the same key as the
installed app. Without signing secrets the release APK is signed with the
runner's debug key, which is generated on every run, so such APKs cannot be
installed over each other. Set up a release key once:

```bash
keytool -genkeypair -v -keystore fiscal-nest.jks -alias fiscal-nest \
  -keyalg RSA -keysize 4096 -validity 10000
base64 -w0 fiscal-nest.jks   # value of FISCAL_NEST_KEYSTORE_BASE64
```

Repository secrets (**Settings → Secrets and variables → Actions**):

| Secret | Value |
|---|---|
| `FISCAL_NEST_KEYSTORE_BASE64` | the keystore, base64-encoded |
| `FISCAL_NEST_KEYSTORE_PASSWORD` | keystore password |
| `FISCAL_NEST_KEY_ALIAS` | key alias (`fiscal-nest` above) |
| `FISCAL_NEST_KEY_PASSWORD` | key password |

Keep the keystore and its passwords outside the repository (`*.jks` and
`*.keystore` are git-ignored) and back them up: a lost key means users have to
uninstall the app before the next update.

## Release from a manual run

Instead of pushing a tag, run the workflow on `main` with `release` enabled:
**Actions → Android → Run workflow**, branch `main`, `release` checked and
`version` set. The run tests and builds the commit exactly as for a tag push,
then creates the tag on that commit and publishes the release.

```bash
gh workflow run android.yml --ref main -f release=true -f version=0.3.0
```

The run refuses to release (and publishes nothing) when:

- it is not on `main`;
- the `version` input is empty or not `MAJOR.MINOR.PATCH[-suffix]`;
- the tag `android-v<version>` already exists;
- the version is not newer than the latest `android-v*` tag.

The tag is created with the workflow token, which does not start another run:
everything happens in the one manual run. Without `release` a manual run is an
ordinary build on any branch and only uploads its artifact.

## Branch and pull request builds

The workflows run on every push and pull request that touches their component:
`core.yml` on `core/**`; `android.yml` on `android/**` and the engine sources
(`core/src/main/**`, `core/build.gradle.kts`), which the app builds. The APKs
are attached to the run as an artifact (**Actions → run → Artifacts**) and are
not published.

## Re-running a release

Re-running a failed release workflow is safe: an existing release keeps its
notes and only its assets are replaced. To rebuild from a different commit,
delete the release and the tag on GitHub, then tag again or start a new manual
run.
