# AtlasQuest — Feature Spec & Roadmap

AtlasQuest is a Duolingo-style geography game built around one core interaction:
**every question is a clue about a country, answered by dropping a pin on an
offline 3D globe that renders every country in the world.**

## Locked decisions

### 2026-07-11 — the pin-drop pivot (supersedes the region model)

- **Globe navigation and regions are gone.** The continent globe → subregion
  globe → region-scoped quiz flow, the `Region`/`Continent` enums, per-region
  stats, and the classic multiple-choice question type were all removed. The
  loop is now Home → Quiz (10 random pin questions) → Results.
- **Correctness is polygon-based, not distance-based.** The pin page hit-tests
  the confirmed pin against the country polygons (`d3.geoContains`) and reports
  which country was hit; a pin anywhere inside the answer country is correct.
  This is fair for large countries (tapping eastern Siberia counts for Russia).
  Distance still matters for near-misses: outside the country but within
  800/500/300 km (difficulty 1/2/3) of the answer point pays half XP.
- **Scoring stays in Kotlin.** JS reports `(lat, lng, hitCountryId,
  hitCountryName)`; `GeoScoring` + `IsoNumeric` decide credit and XP. The JS
  page remains a dumb input device.
- **Content = one clue question per country** present in the bundled 110m
  dataset (163 questions). Countries too small to exist as polygons at 110m
  scale (Malta, Singapore, Bahrain, Maldives, most Pacific micro-states) are
  excluded — they'd be untappable.

### Earlier locked decisions (2026-06-26, still standing)

- **Backend = Firebase all-in** — Firebase Auth + Firestore (Cloud Functions
  later). No custom REST backend.
- **Auth is guest-first / optional** — play locally, sign in to sync + compete.
- **Mascot is a globe-trotting parrot** (shipped: Lottie-based `MascotView`).

## Architecture snapshot

- Compose + Hilt + Room + kotlinx.serialization + Navigation Compose +
  WorkManager. Offline globe = WebView (`assets/globe/pin.html` with bundled
  `d3.min.js`, `topojson.min.js`, `countries-110m.json`).
- **Build note:** Gradle needs Android Studio's JBR (JDK 21), not system
  JDK 25. `$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'` then
  `.\gradlew.bat :app:assembleDebug`.

## The core game (implemented)

### Data model

`Question(id, category, text, countryName, countryCode, answerLat, answerLng,
difficulty, explanation?)`

- `countryCode` — ISO 3166-1 alpha-2; identifies the answer country for the
  polygon hit-test. `IsoNumeric.alpha2ToNumeric` converts to the topojson
  feature id (the dataset zero-pads ids like `"032"`; the JS side normalizes
  before comparing).
- `answerLat/answerLng` — representative point (usually the capital); used for
  the reveal marker, the guess-to-answer arc, and near-miss distance.
- `difficulty` 1–3 sets XP (10/20/30 for a hit, half for a near-miss) and the
  near-miss radius (800/500/300 km).
- Seeding: `assets/questions.json` → `DatabaseSeeder` (re-imports when
  `SEED_VERSION` bumps; rejects any `countryCode` missing from `IsoNumeric` at
  import time). DB schema v5, `fallbackToDestructiveMigration` (pre-release).

### Globe input page — `assets/globe/pin.html`

State machine: BROWSE → PIN_PLACED → CONFIRMED → REVEAL.

- Drag rotates, pinch/wheel zooms (0.8×–6×), tap drops/relocates the pin,
  Confirm locks it in. Taps outside the globe disc are rejected.
- On confirm the page hit-tests the pin against all country polygons and calls
  `AtlasQuest.onPinConfirmed(lat, lng, countryId, countryName)` (`countryId`
  `""` for ocean or id-less features like Kosovo/Somaliland).
- `AtlasPin.loadQuestion()` resets for the next question — zooms back out but
  keeps orientation (any framing would hint at the answer).
- `AtlasPin.reveal(lat, lng, answerCountryId, correct)` highlights the answer
  country in gold, draws the guess→answer great-circle arc, and fires
  particles on a correct hit. All verdict UI (result line, distance, Confirm
  button) is native Compose — overlay controls inside the WebView proved
  unreliable, so the page only handles globe rendering and input.
- One WebView per quiz (`PinGlobeController`), created up front, reused across
  questions; JS calls queue until `onPageReady`. The WebView calls
  `requestDisallowInterceptTouchEvent` on touch-down so globe drags win over
  the quiz column's vertical scroll.

### Scoring — `GeoScoring`

- Hit (pin inside answer country) → correct, XP = 10 × difficulty.
- Near-miss (outside, within 800/500/300 km of the answer point) → not
  correct, XP = 5 × difficulty. Keeps `score/total` binary and meaningful.
- Otherwise 0 XP. Haversine distance, Earth radius 6371 km. Unit-tested.

### Progress & retention (Phase 2, adapted)

- `ProfileEntity` (single row): `xpTotal`, `currentStreak`, `longestStreak`,
  `lastPlayedEpochDay`. Streak uses the device-local day: same day = no
  change, consecutive day = +1, gap = reset to 1. Region stats were removed
  with the region model (per-country stats are a natural future replacement).
- `QuizViewModel.next()` persists the result *before* flipping `finished`, so
  navigation can never race the DB write. The empty-quiz path records nothing.
- Daily streak reminder: `StreakReminderWorker` (WorkManager + Hilt), 24 h
  periodic work targeting ~19:00 local, `POST_NOTIFICATIONS` only.

### Mascot (Phase 3, shipped)

Lottie parrot, five emotions (IDLE / CORRECT / WRONG / CELEBRATE / ENCOURAGE),
pure function of existing UI state. **Removed from the quiz screen** (2026-07-11):
the placeholder art read as a sticker and duplicated the coloured verdict line;
it returns there only once real animation assets exist. Still shown on Home,
Results, and the empty-quiz state.

## Roadmap (next)

1. **Content depth** — more clue styles per country (flags, capitals,
   currency), photo clues via `imageResName`-style extension; every coordinate
   spot-checked against the globe's own rendering.
2. **Per-country stats** — replace the removed region stats with a
   `countryCode`-keyed table; "countries found" map coloring is the natural
   progression surface.
3. **Daily challenge** — one curated 10-pin run per day; pairs with streaks.
4. **Auth (Firebase, guest-first)** then **weekly leagues** (Firestore) — as
   previously specced; client-reported scores acceptable for v1.

## UI polish backlog

Deferred visual refinements identified during the 2026-07-11 UI passes. None
blocks play; ordered roughly by impact.

3. **Fade the on-globe hint after first use.** "drag to spin • pinch or +/- to
   zoom • tap to drop a pin" is good onboarding but becomes permanent clutter
   from Q2 on. Show it on the first question (or until the first pin drop),
   then fade it out — a one-shot flag in `pin.html` or driven from Compose.
4. **Cross-screen visual consistency.** The quiz screen is now clean and flat,
   but Home and Results still show the placeholder emoji parrot with the ✓/✗
   sticker — the app reads as two design languages. Resolve by either retiring
   that placeholder art everywhere until real mascot animations exist, or
   committing to a tasteful mascot on every screen. Pair with the Phase 3
   Lottie asset work.
5. **Country palette adjacency.** Colours are assigned by `id % 6`
   (`COUNTRY_COLORS` in `pin.html`), so neighbouring countries occasionally
   share a hue. A greedy graph-colouring pass over the shared-border adjacency
   would guarantee adjacent countries always differ — precompute once from the
   topology and bake a colour index per feature id.
6. **Overlapping reveal pins.** On a correct answer the red guess pin and the
   gold answer pin sit almost on top of each other (the guess was close), which
   looks slightly muddy. Low priority — options: suppress the guess pin when it
   is inside the answer country, or nudge/offset overlapping markers.

## Known limitations / design notes

- The 110m dataset has no features for micro-states; they're excluded from
  content rather than being unanswerable.
- Kosovo, Somaliland, and N. Cyprus have no numeric feature id; a pin there
  reports `""` and scores as a (distance-banded) miss. No questions target
  them.
- `d3.geoContains` on ~180 features runs only on confirm — no perf concern.
- Same-question replays can memorize locations; irrelevant at current content
  scale, revisit if a daily challenge ships.
