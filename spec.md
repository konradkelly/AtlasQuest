# AtlasQuest — Feature Spec & Roadmap

AtlasQuest is being grown into a Duolingo-style geography trivia app built around
its offline interactive globe. This document captures the agreed roadmap and the
detailed spec for Phases 1 and 2.

## Locked decisions (2026-06-26)

- **Build order starts with the real quiz loop.** `QuizScreen.kt` is currently a
  stub returning a hardcoded `onQuizComplete(7, 10)`. The trivia data layer
  already exists and just needs wiring to a `QuizViewModel`. Everything else
  (points, streaks, leaderboards, mascot reactions) is downstream of a real quiz.
- **Backend = Firebase all-in** — Firebase Auth + Firestore (Cloud Functions
  later). Drop the unused Retrofit path rather than mixing a custom REST backend
  with Firebase.
- **Auth is guest-first / optional** — play locally, sign in to sync + compete;
  migrate local progress to cloud on first sign-in.
- **Mascot is a globe-trotting parrot** — fits the "Atlas" theme.

## Current codebase state (as of writing)

- **Trivia pipeline ~80% built:** `QuestionEntity` → `QuestionDao`
  (`getRandomQuestions`, `getQuestionsByRegion`) → `QuestionRepository` →
  `DatabaseSeeder` (reads `assets/questions.json`). `Question` already models
  `category`, `difficulty`, `options`, `correctAnswerIndex`, `region`,
  `imageResName`.
- **The hole:** `QuizScreen.kt` is a stub; there is no `QuizViewModel`.
- **Local progress layer built (Phase 2):** `ProfileEntity`/`RegionStatsEntity`
  (Room) track XP total, current/longest streak, and per-region stats;
  `QuizViewModel` awards difficulty-weighted XP and records streak progress on
  quiz completion. A daily `WorkManager` job reminds players who haven't
  played yet today. No user/identity layer yet — this is all local,
  unauthenticated state. Retrofit is in deps ("ready for future backend") but
  unused. No Firebase.
- **Architecture:** Compose, Hilt DI, Room, kotlinx.serialization, Navigation
  Compose. Globe is an offline WebView (`assets/globe/`): continent globe
  (`index.html`) → subregion globe (`subregions.html`) → quiz.
- **Build note:** Gradle needs Android Studio's JBR (JDK 21), not system JDK 25.
  `$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'` then
  `.\gradlew.bat :app:assembleDebug`.

---

## Roadmap

### Phase 1 — Make the core loop real *(no new dependencies)*

Wire the existing data layer into a real quiz. Detailed spec below.

### Phase 2 — Progress & retention *(local-first, still no backend)* ✅ Done

Local profile store, streak logic, and a WorkManager reminder. Detailed spec
below.

### Phase 3 — Mascot *(high delight, low dependency; can start earlier)*

- Reactive parrot character with emotion states: idle / correct / wrong /
  celebrate / encourage.
- Hook into answer feedback, results screen, streak milestones, empty/onboarding
  states.
- Asset format: **Lottie** animations (vector, light, loopable; one new dep) or
  static PNG emotions for a quick v1.
- A simple static version can be sprinkled into Phase 1's quiz feedback early.

### Phase 4 — Authentication *(Firebase Auth + Google)*

- Guest-first, auth-optional. Play locally; sign in to sync + compete.
- Migrate local progress to cloud on first sign-in.
- Adds `google-services` plugin + `firebase-bom` + Credential Manager.
- Setup cost: `google-services.json`, Firebase console project, signing SHA keys
  for Google sign-in.
- New: `User` model + `AuthRepository`, sign-in screen / profile screen.

### Phase 5 — Social / competition *(needs auth + points first)*

- **Firestore-backed leaderboards.** Prefer Duolingo-style **weekly leagues**
  (tiered, resets weekly) over a single global board — more motivating, caps the
  "never catch #1" problem.
- Write user XP/score to Firestore; query top-N + user's rank.
- **Anti-cheat:** client-reported scores are spoofable. Fine for v1; later move
  validation server-side (Cloud Functions).

### Differentiators to design toward

The offline globe is an asset Duolingo lacks. Worth building toward:

- **Map-based question types** ("tap Peru on the globe") — unique interaction.
- **Daily challenge** — one curated quiz/day; pairs with streaks.
- **Region mastery / unlock progression** tied to the globe.

### Hidden costs to budget

- **Content is the real work** — hundreds of quality, accurate questions per
  region is a content project, not engineering. An LLM can draft; you curate. The
  schema (category + difficulty) already supports it.
- Firebase setup overhead (see Phase 4).

---

## Phase 1 — Detailed spec

Goal: replace the stubbed quiz with a real loop that loads questions from the
existing repository, scores answers, and reports a real result. No new deps.

### 1. `QuizViewModel` (new) — `ui/screens/quiz/QuizViewModel.kt`

- `@HiltViewModel`, inject `QuestionRepository`.
- Take the `region` (from the nav arg) and load questions via
  `getQuestionsByRegion(region.id, limit = 10)`, falling back to
  `getRandomQuestions` for regions thin on content (e.g. Eurasia).
- Expose a `StateFlow<QuizUiState>` containing: current question, index/total,
  selected option, answer-revealed flag, running score.
- Intents: `selectOption(i)`, `next()`. Score on reveal
  (base × difficulty; speed/streak-in-quiz bonuses can come later).

### 2. `QuizScreen` (rewrite the stub)

- Render question text, the four options (`Question.options`), a progress
  indicator.
- On tap: reveal correct/incorrect coloring, then advance; on the last question
  call the real `onQuizComplete(score, total)`.
- Natural first home for a **simple mascot reaction** (parrot 👍 / 👀 on
  correct/wrong) — cheap personality before Phase 3.

### 3. Content — `assets/questions.json`

- Confirm it exists and which regions it covers; author/expand real questions
  keyed by `region` id (e.g. `"south_america_andean"`, `"eurasia"`).
- An LLM can draft a first batch per region for curation.

### 4. Scoring → Results

- `ResultsScreen` already accepts `score`/`total`; feed it real numbers.
- Local XP accumulation: see Phase 2.

### Pre-flight check before coding

- Verify `assets/questions.json` actually has content and which `region` ids it
  uses. If empty or keyed to outdated region names, the wired-up quiz will show
  nothing. Cross-check against the ids in `Region.kt`.
- Confirm `Question` / `QuestionCategory` / `QuestionDao` signatures match the
  ViewModel's assumptions.

---

## Phase 2 — Detailed spec

Goal: make finishing a quiz mean something locally — XP, a daily streak, a
nudge to come back — with no backend and no new screens. Status: **done**.

### 1. Storage — Room, not DataStore

Per-region stats is naturally a list of rows, and Room already has the
established DI/migration pattern in this codebase, so it was reused rather
than introducing DataStore as a second persistence mechanism.

- `data/local/entity/ProfileEntity.kt` — single row, fixed `@PrimaryKey id = 0`
  (Room has no native "singleton row" concept; the fixed id is the
  convention). Fields: `xpTotal`, `currentStreak`, `longestStreak`,
  `lastPlayedEpochDay` (sentinel `Long.MIN_VALUE` for "never played", so the
  first-ever quiz falls into the normal streak-reset branch with no special
  casing).
- `data/local/entity/RegionStatsEntity.kt` — `regionId` (matches `Region.id`)
  as primary key, `quizzesPlayed` / `questionsCorrect` / `questionsTotal`.
- `data/local/dao/ProfileDao.kt` — `observeProfile(): Flow<ProfileEntity?>`
  plus a `@Transaction` `recordQuizResult(...)` that applies the streak
  transition and upserts both rows atomically.
- `data/local/AppDatabase.kt` — both entities registered, version bumped to 3
  (kept `fallbackToDestructiveMigration`, consistent with the existing
  rationale for `questions`: this is pre-release, bundled/local data, not
  worth hand-written migrations yet).
- `data/repository/ProfileRepository.kt` — domain-facing `Flow<Profile>` plus
  `recordQuizResult(...)` and `hasPlayedToday()` (used by the reminder
  worker).

### 2. XP formula

`10 × difficulty` per correct answer (`Question.difficulty` is 1–3, so 10/20/30
XP per question). Computed in `QuizViewModel` alongside the existing `score`
counter and exposed as `QuizUiState.xpEarned`.

### 3. Streak logic — local day, not UTC

Uses `LocalDate.now()` (device default timezone) and stores
`lastPlayedEpochDay: Long`. There's no server time authority in a local-first
app, so local day is what "today" means to the player — this was the
deliberate resolution of the roadmap's "watch the timezone gotcha" note.

On every completed quiz: `today == lastPlayedEpochDay` → streak unchanged
(same-day replay); `today == lastPlayedEpochDay + 1` → `+1`; otherwise (gap,
or first-ever play) → reset to 1. `longestStreak` tracks the max. **Region
stats accumulate unconditionally on every quiz**, independent of the streak
branch — same-day replays still count toward `quizzesPlayed`/correct/total.

Accepted simplifications, documented in code rather than solved: DST shifts
time-of-day, not the calendar date, so it doesn't affect `epochDay` math
(not a bug). Manual clock changes or crossing timezones at midnight can still
cause streak miscounts — unsolvable without a backend, out of scope for
local-first Phase 2.

### 4. Wiring into the quiz flow

In `QuizViewModel.next()`, the last-question branch persists via
`profileRepository.recordQuizResult(...)` **inside** the same coroutine that
then flips `state.finished = true` — i.e. the DB write completes before
`QuizScreen` reacts to `finished` and navigates to Results, so navigation can
never race the write. The empty-questions fallback in `QuizScreen.kt` (its
"Back" button calls `onQuizComplete(0, 0, 0)` directly) bypasses `next()`
entirely and therefore never records a result — no XP/streak for an empty
quiz.

`Screen.Results` route extended to `results/{region}/{score}/{total}/{xp}` to
carry `xpEarned` through navigation. A shared `ui/profile/ProfileViewModel.kt`
exposes `ProfileRepository.profile` as a `StateFlow`, reused by both
`HomeScreen` (streak/XP row above the Play button) and `ResultsScreen`
(`+N XP earned` and current streak).

### 5. Streak reminder — WorkManager + Hilt

- Deps added: `androidx.work:work-runtime-ktx`, `androidx.hilt:hilt-work`,
  plus a **distinctly-named** `androidx.hilt:hilt-compiler` KSP alias — this
  is a different artifact from the existing `hilt.compiler` alias
  (`com.google.dagger:hilt-android-compiler`); both KSP processors run side
  by side.
- `worker/StreakReminderWorker.kt` — `@HiltWorker` `CoroutineWorker`; if
  `!profileRepository.hasPlayedToday()`, shows the reminder notification.
- `AtlasQuestApp.kt` implements `Configuration.Provider` (the
  `workManagerConfiguration` **property**, WorkManager 2.8+ API) supplying a
  Hilt-injected `WorkerFactory`. No manifest `<provider tools:node="remove">`
  needed — WorkManager auto-detects `Configuration.Provider` on the
  `Application` and skips its default App Startup initializer. Schedules a
  24h `PeriodicWorkRequest`, `ExistingPeriodicWorkPolicy.KEEP`, with an
  initial delay computed to land near a fixed local hour (19:00) on first
  run.
- Permission: **`POST_NOTIFICATIONS` only** (API 33+), not
  `SCHEDULE_EXACT_ALARM` — WorkManager's regular periodic work doesn't need
  exact alarms, so the more sensitive permission was deliberately avoided.
  Requested once via `LaunchedEffect` on `HomeScreen`'s first composition;
  denial is silent (reminders just don't show, no nagging).
- `notifications/StreakNotifications.kt` — creates the notification channel
  (idempotent) and builds the reminder notification, currently reusing
  `ic_launcher_foreground` as the small icon (no dedicated status-bar icon
  exists yet — cosmetically rough, easy to swap later).

### Known limitations / future follow-ups

- A single 24h `PeriodicWorkRequest` can drift a few minutes-to-hours per
  cycle under Doze/battery optimization rather than landing on the exact hour
  every time. Re-enqueuing a fresh one-time request each run (recomputing the
  delay-to-next-19:00) would fix this but wasn't worth the extra complexity
  for v1.
- No dedicated monochrome notification icon yet — worth pairing with the
  Phase 3 mascot art pass.
- `RegionStatsEntity` is written but has no UI surface yet (no per-region
  breakdown screen) — natural extension point once that's wanted.
