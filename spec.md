# AtlasQuest — Feature Spec & Roadmap

AtlasQuest is being grown into a Duolingo-style geography trivia app built around
its offline interactive globe. This document captures the agreed roadmap and the
detailed spec for Phase 1.

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
- **No user/identity/progress layer.** No points, streak, or profile persistence.
  Retrofit is in deps ("ready for future backend") but unused. No Firebase.
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

### Phase 2 — Progress & retention *(local-first, still no backend)*

- Profile store (XP total, current/longest streak, per-region stats) — DataStore
  or a Room `profile` table.
- Daily streak logic + streak UI. **Watch the timezone gotcha** (UTC vs local day
  boundary) when deciding whether a day counts.
- Streak reminder via WorkManager + notifications — highest-leverage retention
  feature.

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
- Local XP accumulation arrives in Phase 2.

### Pre-flight check before coding

- Verify `assets/questions.json` actually has content and which `region` ids it
  uses. If empty or keyed to outdated region names, the wired-up quiz will show
  nothing. Cross-check against the ids in `Region.kt`.
- Confirm `Question` / `QuestionCategory` / `QuestionDao` signatures match the
  ViewModel's assumptions.
