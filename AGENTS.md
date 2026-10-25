# AGENTS.md

Guidance for AI coding agents working in the Livestream Kotlin Multiplatform app (Android and iOS).

## STRICT RULE: no code comments

Never add comments of any kind: line, block, KDoc, or comments in Gradle, XML and Swift files.
Make the code explain itself through clear names. Review your diff and delete any comment you added.

## Project overview

- Kotlin Multiplatform with Compose Multiplatform (Material 3). Almost all code lives in `shared/`
  (`commonMain`), with platform code in `androidMain` and `iosMain`.
- `app/` is a thin Android shell (`Livestream` application that starts Koin, `MainActivity`).
  `iosApp/` is the Xcode project that hosts `MainViewController`.
- Local storage is Room (KMP) with the bundled SQLite driver. Dependency injection is Koin.
- The project layout and conventions follow `~/AndroidStudioProjects/FarmTrack`.

## Layout

Paths are relative to `shared/src/commonMain/kotlin/com/charlesmuchogo/livestream/`.

| Path | Contents |
|---|---|
| `presentation/live`, `schedule`, `saved`, `match` | Screens: `X.kt` (Root + Screen), `XState.kt`, `XAction.kt`, `XViewModel.kt` |
| `presentation/components/` | Shared UI. `StreamPlayer` plays `direct` sources in ExoPlayer/AVPlayer and `page` sources in an in-app web view |
| `navigation/` | `@Serializable` routes and the `NavHost` with the bottom bar |
| `domain/models`, `domain/mappers` | App models and DTO mappers |
| `data/remote` | Ktor client for the Fiber API in the parent repo (`cmd/api`) |
| `data/local/database` | Room: saved matches and followed leagues |
| `data/repository` | `MatchRepository` (API) and `LibraryRepository` (Room) |
| `di/ApiUrls.kt` | `LOCAL_API_URL`, the API address the app uses on every platform |
| `di/commonModule.kt` | Koin module. Register every new ViewModel here |
| `ui/theme/Theme.kt` | Floodlight colours and fonts. Use `Floodlight.*` rather than new hex values |

## Conventions

- Screens take `state` plus `onAction: (XAction) -> Unit`. ViewModels expose `state` as a `StateFlow`
  built with `stateIn(viewModelScope, WhileSubscribed(5_000), ...)`.
- ViewModels that take route arguments are registered with `viewModel { (a: String, b: String) -> ... }`
  and created with `koinViewModel { parametersOf(a, b) }`.
- No `android.*`, `java.*`, `Context` or `R` in `commonMain`. Put platform work behind an interface bound
  in `platformModule()`, or an `expect`/`actual` composable.

## Talking to the API

Run `go run ./cmd/api` in the parent repo. The app calls `LOCAL_API_URL`, the Mac's LAN address, so the
emulator, the simulator and phones on the same Wi-Fi all reach it. Update it when the Mac's IP changes.

## Database changes

Room schemas are exported to `shared/schemas/`. When an entity changes, bump the `version` in
`@Database` and add a migration. Commit the new schema JSON.

## Build and verify

```bash
./gradlew :shared:compileCommonMainKotlinMetadata
./gradlew :app:assembleDebug
./gradlew :shared:compileKotlinIosSimulatorArm64
./gradlew :shared:testAndroidHostTest
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 17 Pro Max' CODE_SIGNING_ALLOWED=NO build
```

All of these must pass before reporting work as done.
