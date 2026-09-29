# Evenly

![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin_Multiplatform-2.4-7F52FF?logo=kotlin&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Compose_Multiplatform-1.11-4285F4?logo=jetpackcompose&logoColor=white)
![Supabase](https://img.shields.io/badge/Supabase-Postgres_%2B_RLS-3FCF8E?logo=supabase&logoColor=white)
![Platforms](https://img.shields.io/badge/platforms-Android%20%7C%20iOS-3DDC84)
[![CI](https://github.com/iamafzalhassan/evenly-kmp/actions/workflows/ci.yml/badge.svg)](https://github.com/iamafzalhassan/evenly-kmp/actions/workflows/ci.yml)

A shared expense tracker for Android and iOS, built with Kotlin Multiplatform and Compose Multiplatform. Friends, flatmates and travel groups log what they spend, split it fairly, and settle up in at most one payment fewer than the number of members.

Evenly is local-first: every change is saved on the device and every screen works without a connection. When online, groups sync between everyone in them. There is no sign-up; friends join a group with an invite code.

## Features

- **Shared groups**: invite friends by sharing an 8-character code from the system share sheet; everyone in the group sees the same expenses, balances and payments, and edits sync in the background.
- **Offline-first sync**: changes are saved instantly on the device and uploaded when a connection is available, with pull-to-refresh and a sync status in Settings.
- **Groups** with members and a home currency (AED, AUD, CHF, CZK, DKK, EUR, GBP, INR, JPY, LKR, NOK, PLN, SEK, USD), defaulting to the device's local currency. Rename or delete a group at any time.
- **Expenses** split equally, by exact amounts or by percentage, with a live preview of each share, a running "left to assign" total, a note explaining what is still needed before saving, and a date you can change. Amounts group into thousands as you type and accept a decimal point or comma.
- **Multi-currency expenses**: pay in euros on a trip for a rupee group, enter the rate once, and Evenly converts every share exactly. The rate is remembered for next time.
- **Group summary** with total spent, number of expenses and amount paid back, plus search across expenses by title or payer.
- **Balances** showing who gets back and who owes, recalculated from the expenses every time anything changes.
- **Settle up** suggestions that clear every debt in at most _n − 1_ payments for a group of _n_ people. Tap a suggestion to record it as paid.
- **Payment history**, where a recorded payment can be deleted if it was a mistake.
- **Safe member removal**: a member can only be removed once no expense or payment refers to them.
- **App lock** with Face ID, fingerprint or device passcode, locking on launch and after 30 seconds in the background, and hiding the app's content in the app switcher while it is on.
- **Private by design**: no account or personal details are collected, row-level security limits each group to the people who joined it, and on-device data is protected by the OS's storage encryption and excluded from cloud backups.

## Architecture

All domain logic, data and UI live in one shared module. The Android and iOS apps are thin entry points: `MainActivity` on Android, which constructs the shared `AndroidBiometricAuthenticator` (built on `BiometricPrompt`), and a Compose view controller on iOS, which uses `IosBiometricAuthenticator` (built on `LocalAuthentication`). Both authenticators live in the shared module's platform source sets.

- **Layers.** `ui` (screens, ViewModels, UI state and events) depends on `domain` (`ExpenseSplitter`, `ExpenseValuation`, `BalanceCalculator`, `SettleUpPlanner`) and `data` (repositories). `data` reads Room entities and DAOs in `data/sources` and talks to Supabase through `SyncCoordinator` and `SyncEngine` in `data/sync`. `model` holds the shared types, and `security` holds the `BiometricAuthenticator` contract.
- **Unidirectional data flow.** Each screen has a ViewModel exposing one immutable `UiState` as a `StateFlow` and accepting a sealed `Event` through a single `onEvent`.
- **Layering is enforced by convention.** Composables never touch repositories, ViewModels never import Compose UI, and `model` and `domain` are pure Kotlin with no IO, apart from the Compose runtime `@Immutable` stability annotation on `model` types.
- **Derived data is never stored.** Balances and settle-up suggestions are recomputed from expenses and payments, so a fix to the splitting logic corrects every past expense.
- **Manual dependency graph.** `AppGraph` builds the database and repositories once per process; each navigation destination creates its ViewModel from it.

## How the money works

**Money is never a floating-point number.** `Money` holds a `Long` count of minor units (cents) and its `Currency`, and arithmetic across currencies fails fast.

**Splits always add up exactly.** `ExpenseSplitter` turns a `SplitRule` into one share per member:

| Rule | How remainders are handled |
|---|---|
| Equal | The total is divided by the number of participants; leftover cents go one each to the first participants, so LKR 100.00 across three people is 33.34, 33.33 and 33.33. |
| Exact | The amounts must sum to the total, or the split is rejected. |
| Percentage | Stored as basis points that must total 10,000. Shares are floored, then leftover cents go to the largest remainders (the largest remainder method), with ties resolved by member order. |

The same `validate` call guards the editor's Save button, the repository and the domain, so an invalid split can never be stored.

**Currency conversion is exact integer arithmetic.** An `ExchangeRate` stores the rate in millionths and converts minor units with half-up rounding, handling currencies with different decimal places (such as JPY with none). Amounts are bounded so the calculation can never overflow a `Long`. Each share is converted separately, then the rounding difference is corrected one minor unit at a time from the largest share down, so converted shares always add up to the converted total. The rate is frozen on the expense, so balances never shift when you use a different rate later.

**Settling up takes at most _n − 1_ payments.** `SettleUpPlanner` repeatedly matches the member owed the most with the member who owes the most and transfers the smaller of the two amounts. Every step clears at least one balance, so a group of _n_ people is settled in at most _n − 1_ payments.

## How sync works

Evenly is local-first. The UI only ever reads from the on-device Room database; the network is a background concern.

1. **Every write is local first.** A change is stored immediately with `isDirty = true` and the device's modification time, and a debounced sync is requested.
2. **Push.** `SyncEngine` uploads dirty rows in dependency order (groups, members, expenses, payments) as upserts, then marks each row clean only if it was not edited again while uploading.
3. **Pull by sequence, not by clock.** The server stamps every accepted write with a value from a single Postgres sequence, and a statement-level trigger takes one transaction-scoped advisory lock before any write, so rows commit in sequence order. Each device keeps one cursor per table and pulls rows past it, so pulling is gap-free and unaffected by clock skew between devices. A page is applied in one pass: rows the device already holds unchanged are skipped, and a child row whose group is not on the device yet fetches that group first.
4. **Conflicts resolve per row, last writer wins.** Every row carries `modified_at_ms`, the editing device's clock at the time of the change. A server trigger ignores any write whose `modified_at_ms` is older than the stored row's, and a pulled row never overwrites a newer unsynced local edit. Unlike pulling, this comparison uses device clocks, so a device whose clock is wrong can win or lose a conflicting edit it should not. An expense and its split travel together (shares are a JSON array on the expense row), so a split can never be half-updated.
5. **Deletes are tombstones**, so a deletion reaches every device instead of being resurrected by a stale copy.
6. **Bad rows never spread.** Check constraints on the server reject malformed amounts, currencies, split kinds and shares, and every pulled row is validated again on the device, so one broken write cannot crash another participant's app. If the server rejects a batch, rows are retried one at a time so a single bad row cannot block the rest.

**Access control.** Each install signs in anonymously. Creating a group makes you its first participant; `join_group(code)` adds anyone with the invite code and allows 20 wrong codes per user in ten minutes, so codes cannot be guessed at speed. If an install ever gets a new anonymous identity, it rejoins its groups with their stored invite codes. Row-level security on every table allows reads and writes only for participants of that group, checked through a `SECURITY DEFINER` helper kept outside the public API schema. The schema lives in [`supabase/migrations`](supabase/migrations).

## Security and privacy

- **App lock** is a `BiometricAuthenticator` interface in shared code with two native implementations: `BiometricPrompt` on Android (biometrics or device credential, supported from API 24) and `LAContext` on iOS (Face ID, Touch ID or passcode). Turning the lock on or off requires authenticating first. While the app is locked, the screen underneath receives no touches and is hidden from screen readers. With the lock on, Android marks the window secure and iOS covers the app when it leaves the foreground, so balances never show in the app switcher.
- **Storage** stays in app-private storage protected by the operating system's encryption. On Android, backups and device transfer exclude the database. On iOS, the database lives in a directory created with `NSFileProtectionCompleteUnlessOpen` and excluded from iCloud backup.
- **Minimal data on the server.** Only group content syncs: group and member names, expenses and payments. No email, phone number or device identifier is collected.

## Tech stack

| Area | Choice |
|---|---|
| Language | Kotlin 2.4, Coroutines, Flow |
| UI | Compose Multiplatform 1.11, Material 3, Compose resources for strings, plurals and fonts |
| Navigation | Compose Multiplatform Navigation with type-safe `@Serializable` routes |
| Persistence | Room for Kotlin Multiplatform, bundled SQLite driver, KSP, exported schemas with auto-migrations |
| Backend | Supabase: Postgres with row-level security, anonymous auth, supabase-kt with Ktor (OkHttp on Android, Darwin on iOS) |
| Security | AndroidX Biometric, iOS LocalAuthentication, platform file protection |
| Dates | kotlinx-datetime |
| Testing | kotlin.test in `commonTest`, run in GitHub Actions CI on the JVM and the iOS simulator |

## Design system

Evenly shares one design system with two other apps of mine: warm paper surfaces, navy ink, and a dotted divider as the signature motif. Colours, spacing and typography are tokens in `ui/theme`, with SF Pro Display for all text and tabular figures on every amount so columns of money line up. Shared components (`AppTextField`, `AppAmountField`, `AppPickerField`, `AppModalSheet`, `AppListTile`, `AppSwitchTile`, `ChoiceRow`, `TotalsBlock`, `ConfirmSheet`, `TextInputSheet`, `CurrencyPickerSheet`, `AppDatePickerDialog`, and more) live in `ui/components`. Screens never use a raw colour or a bare measurement.

## Code conventions

- A strict member ordering convention for every Kotlin and Swift file: properties sorted by type tier, then type, then name; functions ordered by call order.
- No comments in source. Names, types and ordering carry the meaning.
- All user-facing text lives in `strings.xml`.

## Project structure

```
shared/src/commonMain/kotlin/org/example/evenly/
    App.kt, AppGraph.kt
    model/          Group, Member, Expense, SplitRule, Money, ExchangeRate, Settlement, Balance, Transfer
    domain/         ExpenseSplitter, ExpenseValuation, BalanceCalculator, SettleUpPlanner, GroupLedger, LedgerIntegrity, MemberUsage
    data/           Group, Expense, Settlement, ExchangeRate and Settings repositories, mapping
    data/sources/   EvenlyDatabase, entities, DAOs
    data/sync/      Supabase client, remote rows, SyncEngine, SyncCoordinator
    security/       BiometricAuthenticator
    ui/theme/       AppColors, AppSpacing, AppTextStyles, EvenlyTheme
    ui/components/  Shared design-system components
    ui/groups/      Group list
    ui/creategroup/ New group form
    ui/group/       Summary, balances, settle up, expenses, payments, members
    ui/expenseeditor/ Add and edit expense
    ui/lock/        App lock gate
    ui/settings/    Settings
    util/           MoneyFormat, RateFormat, PercentFormat, DecimalInput, DateFormat, LocalDates, DeviceLocale
shared/src/androidMain/ AndroidBiometricAuthenticator, database location, share sheet, secure window, device currency
shared/src/iosMain/     IosBiometricAuthenticator, protected database location, share sheet, app switcher cover, device currency
shared/schemas/     Exported Room schemas
supabase/migrations/ Backend schema, row-level security and functions
androidApp/         Android entry point
iosApp/             iOS entry point (Xcode project)
```

## Building

**Requirements:** JDK 21, Android Studio with the Android SDK (compile SDK 37), and Xcode 26 on macOS for iOS. Compose Multiplatform 1.11 links against the iOS 26 SDK, so older Xcode versions fail at the link step.

- **Android:** open the project in Android Studio and run `androidApp`, or run `./gradlew :androidApp:assembleDebug`.
- **iOS:** open `iosApp/iosApp.xcodeproj` in Xcode and run on a simulator. The Xcode build compiles the shared Kotlin framework through Gradle.

## Testing

The money logic that every balance depends on is covered by unit tests in `shared/src/commonTest`, mirroring the source packages:

- **`domain/ExpenseSplitterTest`**: equal splits hand leftover cents to the first participants, percentage splits use the largest remainder, shares always add up to the total, and invalid splits are rejected.
- **`domain/ExpenseValuationTest`**: foreign-currency shares are converted one by one and the rounding difference is corrected in either direction so they add up to the converted total, and group-currency shares are left unconverted.
- **`domain/SettleUpPlannerTest`**: any group settles in at most _n − 1_ payments that clear every balance, checked on a fixed example and on 1,000 seeded random groups, largest debtor pays largest creditor first, and a settled group needs no payments.
- **`model/ExchangeRateTest`**: exact integer conversion between currencies with the same, more or fewer decimal places, rounding half up, and rejection of the wrong currency or a negative amount.
- **`util/MoneyFormatTest`**: formatting with and without minor units, thousands grouping, padded fractions and negative signs, and parsing grouped input back to minor units.

Run them with `./gradlew :shared:testAndroidHostTest` (JVM) or `./gradlew :shared:iosSimulatorArm64Test` (macOS). [CI](.github/workflows/ci.yml) runs both on demand, on the JVM and the iOS simulator; start it from the Actions tab.

## Roadmap

- Live updates with Supabase Realtime instead of periodic sync
- Field-level conflict merging for expenses edited on two devices at once
- Link an anonymous identity to an email so groups can be restored on a new phone
- Export a group's history
- Crash and performance monitoring
