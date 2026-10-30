# CT4111 Topic 08 – Lab notes

## Student details

- Name: Yasin Machigov
- Student ID: 23347116
- Module: CT4111 Generative AI for Software Engineering
- Assignment: Topic 08 – AI-Supported Software Maintenance
- Repository: https://github.com/ymstack/ShopListAssmt
- Original repository: https://github.com/mmnuig/ShopListAssmt

These are working evidence notes, not a completed personal reflection. Results below come from tool responses and emulator UI inspections. All three tasks are recorded below. The user approved Task 3; final publication is recorded in the submission report and conversation handoff.

## Task 1 – Android Studio setup and baseline

### Project inspection

The repository has one Android application module, `:app`. Production code is in `app/src/main/java/com/mmgpt/shoplistgpt`; XML layouts, icons, strings and themes are in `app/src/main/res`. The root also contains Gradle scripts, the wrapper, a version catalog, `Shop-Example.xlsx`, and IDE configuration. There are existing example unit and instrumentation tests in `app/src/test` and `app/src/androidTest`.

The app uses Kotlin and the Android View system with XML and View Binding. It uses AppCompat, Fragments, RecyclerView, ViewPager2 and lifecycle LiveData/ViewModel. Compose graphics/foundation dependencies exist, but the inspected application UI uses Views. CSV import uses OpenCSV and spreadsheet import/export uses Apache POI.

| Setting | Inspected value |
| --- | --- |
| Gradle wrapper | 8.11.1 |
| Active Android Gradle Plugin | 8.9.3, declared in root build.gradle.kts |
| Active Kotlin plugin | 1.8.0, declared in root build.gradle.kts |
| compileSdk / targetSdk | 35 / 35 |
| minSdk | 26 (Android 8.0) |
| Java/Kotlin bytecode target | 1.8 |
| Application ID | com.mmgpt.shoplistgpt |
| Repositories | Google Maven, Maven Central; plugin portal for plugins |
| Available emulator | Medium_Phone_API_37.0, emulator-5554, arm64, API 37 |

The version catalog also declares AGP 8.10.1 and Kotlin 2.0.21, but the app's plugin declarations use the versions from the root build script. No version upgrade was needed for the debug build.

Installed Java discovery reported OpenJDK 23, JBR 21 and two Java 11 installations. The Android Studio bundled Java executable reported OpenJDK 25. The IDE Gradle configuration selects `#GRADLE_LOCAL_JAVA_HOME`; the exact resolved Gradle JVM was not established. Builds were executed through Android Studio's Gradle tool, so the successful result verifies the current IDE build setup, not a separately configured terminal environment. TODO: capture Android Studio's About screen and Gradle JDK setting if required in the submission.

### Architecture and entry point

`AndroidManifest.xml` declares `MainActivity` as the MAIN/LAUNCHER activity. It inflates `activity_main.xml` using View Binding and connects the start page's Plan, Shop, Import and Export buttons. `CategoryPagerAdapter` creates the appropriate mode-specific fragments. Plan puts the category overview first; Shop puts the category summary last.

`ShoppingListViewModel` owns the category list and planning date, exposes LiveData, and saves JSON in SharedPreferences. `ShoppingCategory` and `ShoppingItem` are data classes. Fragments and adapters display and update the shared state. MainActivity handles spreadsheet/CSV import and spreadsheet export.

### Build process and problems encountered

The initial Android Studio Gradle invocation was `:app:assembleDebug --stacktrace`. It returned “Build finished successfully.” No production build fix was required.

A baseline run of `:app:testDebugUnitTest --stacktrace` failed at `:app:compileDebugUnitTestKotlin`. Errors included “Unresolved reference: junit”, “Unresolved reference: Test” and “Unresolved reference: assertEquals”. The example test source existed, but the app module had no test dependencies.

The smallest test setup change was made in `app/build.gradle.kts`:

- Added `testImplementation(libs.junit)`.
- Added `androidTestImplementation(libs.androidx.junit)` and `androidTestImplementation(libs.androidx.espresso.core)`.
- Configured `androidx.test.runner.AndroidJUnitRunner`.

These use existing version catalog entries. No application implementation or appearance was changed.

During the first session, Android Studio tool requests stopped returning, so the edit and verification could not be confirmed at that point. In the continuation, the file was read again and the edit was confirmed. The first deployment retry reported “Gradle sync in process. Wait until sync is completed and retry”. A subsequent Gradle build and deployment succeeded.

### Actual results

| Check | Actual result |
| --- | --- |
| Initial debug build | Passed, before test setup changes |
| Initial unit-test task | Failed to compile: missing JUnit test dependencies |
| Debug build plus unit tests after test setup change | `:app:assembleDebug :app:testDebugUnitTest --stacktrace` succeeded; 1 passed, 0 skipped, 0 failed |
| Initial deployment | Android Studio reported successful deployment to emulator-5554 |
| Continuation deployment | Android Studio reported successful deployment of com.mmgpt.shoplistgpt to emulator-5554 |
| Launch inspection | MainActivity was focused; home UI showed SHOP, PLAN, IMPORT and EXPORT |
| Plan access | Tapped PLAN; UI showed “Plan: 30 Oct”, “Categories”, Clear All, Add rows and the existing category “borz” |
| Return home | Tapped Home; UI showed “Shop+Plan” and all four start buttons |
| Shop access | Tapped SHOP; UI showed “Shop (Planned 30 Oct)”, category “borz” and count “0/0” |
| Instrumentation test | `:app:connectedDebugAndroidTest --stacktrace` succeeded; emulator-5554: 1 PASSED, no failed tests |

The verification session took place on 30 October 2026. No new categories or items were created by the assistant for this phase. The category “borz” was already present when the continuation's Plan screen was inspected. Opening Plan updates the planning date/state through the existing app code.

A device dialog said “System UI isn't responding” and explicitly identified `com.android.systemui`. Selecting Wait did not clear it on the next inspection. Selecting Close app for System UI cleared the dialog, after which the app home and both modes were inspected successfully. This is recorded as emulator/System UI instability, not evidence of a shopping-app crash. No shopping-app crash was observed during the successful home → Plan → home → Shop sequence. This short smoke check does not prove that every application feature works.

The automated tests are only the existing examples: `addition_isCorrect` checks 2 + 2 = 4, and `useAppContext` checks the application package name. They do not verify category navigation or item operations. The latest 40 app-filtered Logcat entries were also inspected after instrumentation; they show the test finishing with 1 test, 0 failed, 0 ignored, with no fatal exception in that limited excerpt. This is not a full-session crash-log audit.

These mode checks were tool-driven UI interactions with UI hierarchy inspection, not an automated assertion-based UI test and not a student-performed manual test. Import/export, item editing, purchases, reordering and category double-click navigation were not tested in Task 1. Task 2 investigation was deferred until Phase 1 approval; its later results appear below.

### Commands and tools used

Android Studio MCP tools were used for project discovery, reading files, editing the test configuration, Gradle builds, deployment and UI inspection/input.

Safe shell commands were run through Android Studio's shell tool:

- `git status --short --branch`, `git status --short`
- `git log -5 --oneline`, `git remote -v`
- `git diff -- app/build.gradle.kts`
- `/usr/libexec/java_home -V`
- Android Studio bundled `java -version`
- SDK `platform-tools/adb devices -l`
- SDK `emulator/emulator -list-avds`
- SDK `adb -s emulator-5554 shell getprop ro.build.version.sdk` (returned 37)
- SDK `adb -s emulator-5554 shell dumpsys activity activities` (showed MainActivity resumed)
- `/usr/libexec/PlistBuddy -c 'Print :CFBundleShortVersionString' '/Applications/Android Studio.app/Contents/Info.plist'` was attempted, but a usable version result was not recorded.

SDK command paths were under `/Users/yasinmcv/Library/Android/sdk`. No system-wide software was installed.

### Task 1 screenshot evidence to save

The UI state tool supplied screen hierarchies and transient screenshots in the conversation. No screenshot image files have been saved into this repository. TODO: save actual screenshots using the emulator camera button or macOS screenshot tool, and add their paths below. Do not label unsaved or reconstructed images as captured evidence.

1. **T1-01 Build:** Android Studio Build output showing the successful debug build. If the old result is no longer visible, rerun `:app:assembleDebug` and capture that new result with its date.
2. **T1-02 Deployment/device:** Running Devices/Device Manager showing the API 37 emulator and the launched application. Include the successful Run/deployment output if possible.
3. **T1-03 Home:** “Shop+Plan” with SHOP, PLAN, IMPORT and EXPORT visible.
4. **T1-04 Plan:** Tap PLAN and capture the Categories screen with its Plan title.
5. **T1-05 Shop:** Home → SHOP; capture the Shop title, category heading and count.
6. **T1-06 Tests:** Android Studio test results showing the actual unit test and instrumentation outcomes once available.
7. Optional environment/problem evidence: Android Studio About/Gradle JDK settings; System UI ANR only if it recurs. Do not recreate an error for a screenshot.

TODO: Yasin should confirm the visible home, Plan and Shop screens manually and record the date. Student manual verification has not yet been reported.

### Reflection

TODO (Yasin): describe what you learned about Gradle, emulator setup and checking AI claims. Mention the difference between a build passing, deployment succeeding and seeing the expected screen. Add your own reflection; none has been invented here.

## Task 2 – Category navigation

### Investigation and code evidence

Phase 1 was approved before this investigation. The relevant code is:

- `PlanCategoriesFragment.onViewCreated`: builds category-heading ShoppingItems between two Add placeholders; its drag handler calls `reorderCategory(from - 1, to - 1)`. Its double-tap handler originally used the touched adapter position, subtracted one for the leading Add row and added one for the leading overview page.
- `ShopCategoriesFragment.onViewCreated`: submits categories to CategorySummaryAdapter; its double-tap handler originally used the touched adapter position directly as the destination page.
- `ItemAdapter` and `CategorySummaryAdapter`: both extend ListAdapter, use DiffUtil and stable IDs, and return each category/item ID from `getItemId`.
- `ShoppingListViewModel.reorderCategory`: removes/reinserts a category in the current list and publishes the new LiveData value synchronously.
- `CategoryPagerAdapter.createFragment`: Plan position zero is the overview, followed by categories at index position minus one. Shop categories use the page position as their index and put the overview last.
- `PlanCategoryFragment` and `CategoryFragment`: read the category from the current ViewModel list using their index argument.
- `MainActivity`: switches modes/adapters and notifies the pager when category count changes.

The ordinary offsets are correct. Four emulator tests against the unchanged code passed for all three categories in both modes, before and after a completed reorder. A blanket off-by-one explanation would not fit this evidence.

A timing defect was then reproduced in both modes. ListAdapter applies diffs asynchronously, while the ViewModel changes its order immediately. During that gap, a visible row position can still refer to the old order. The original navigation code treated that position as an index into the new order.

The deterministic regression fixture starts with IDs/headings `10/Alpha, 30/Beta, 20/Gamma`. In one main-thread turn it changes the model to `Beta, Gamma, Alpha` and dispatches a double tap at the still-visible Alpha name before the queued list update/layout can run. Both tests failed against the original handlers: expected Alpha, actual Beta. This demonstrates a real state/order mismatch; it does not establish that every navigation complaint has the same cause or reproduce a human drag gesture end to end.

A second defect was exposed by the expanded empty-to-one-category test in Shop. The pager selected position zero, but its visible heading stayed “Summary” instead of “Alpha”. CategoryPagerAdapter previously inherited FragmentStateAdapter's position-based IDs. Shop's summary position changes with the list size, so a cached summary fragment could be reused as a category fragment at the same position. The expanded run had 13 passing instrumentation tests and this one failure. A targeted rerun passed after separating overview identity from category-page identity.

### Smallest fix and success criteria

Success criteria: the category whose row was tapped opens in either mode, even when the displayed order lags the model; the existing Plan/Shop page offsets remain correct; missing/deleted categories are ignored; existing editing and shopping actions keep their behavior; no UI/layout or dependency changes are needed.

Both overview handlers now take the touched ViewHolder's stable `itemId`, find that ID in the latest ViewModel category order, and navigate using the resulting index. Plan still adds one for the overview page; Shop adds zero. No matching ID means no navigation, which also excludes Plan Add placeholders. Using the ViewHolder ID ties selection to the actual visible row rather than an adapter position or a possibly stale list snapshot. The existing gesture detection, coordinate conversion, editing controls and data model are preserved.

CategoryPagerAdapter also overrides getItemId/containsItem. The overview uses Long.MIN_VALUE, separate from non-negative category-page indices. Category pages intentionally retain index-based identities, consistent with their existing index arguments and LiveData observers; the overview can move without being confused with a category. The change does not convert all fragments to category-ID arguments or redesign the pager.

### Test design and results so far

The tests are in `CategoryNavigationTest.kt`. Fixtures deliberately use IDs that differ from their positions. The test saves the existing SharedPreferences state, uses its own categories and restores the saved state during teardown.

| Run | Actual result |
| --- | --- |
| First Espresso-based baseline, 4 tests | All 4 failed before navigation: Espresso 3.5.1 on API 37 threw NoSuchMethodException for InputManager.getInstance |
| Baseline using Activity touch dispatch, 4 tests | All 4 passed against unchanged application code |
| Pending-reorder regression, 2 tests | Both failed against unchanged code; Alpha opened Beta in Plan and Shop |
| Same suite after fix | All 6 navigation tests passed on emulator-5554 |
| Expanded regression suite plus build/unit test, before pager fix | Instrumentation: 13 passed, 1 failed (Shop empty → single category displayed Summary instead of Alpha); unit test: 1 passed |
| Focused Shop empty → single regression after pager fix | 1 passed |
| Final `:app:assembleDebug :app:testDebugUnitTest :app:connectedDebugAndroidTest --stacktrace` | Build successful; unit tests: 1 passed, 0 skipped, 0 failed; instrumentation: 16 passed, 0 skipped, 0 failed |

To avoid an unrelated dependency upgrade, the final tests dispatch timestamped MotionEvents through `Activity.dispatchTouchEvent`. This exercises the Android view hierarchy and the real GestureDetector/RecyclerView handlers, but bypasses the OS input injector. Mode buttons are activated with performClick; reorders use the ViewModel method called by the production drag handler. Layout/diff/pager transitions are allowed to settle before ordinary assertions; the two race tests intentionally do not settle between model reorder and touch dispatch.

Commands: `:app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.mmgpt.shoplistgpt.CategoryNavigationTest --stacktrace`; the two failing race tests were also run alone using the runner's class#method filter. All runs used emulator-5554 (API 37).

The Android gesture guide was consulted through Android Studio's documentation search/fetch tools: https://developer.android.com/develop/ui/views/touch-and-input/gestures/detector . The source trace and failing regression tests, rather than the guide alone, establish the defect above.

### Limits and reflection

Expanded cases include every category in both modes, completed and pending reorders, duplicate labels with distinct IDs/items, removed visible category IDs (same-count replacement isolates identity from resizing), empty and single-category lists, ignoring Plan Add rows, and single taps staying on the overview. A regression case edits category/item names and adds/deletes a temporary item through the ViewModel, then exercises the actual Plan select-all and Shop checkbox/flag controls and asserts model values and the displayed 1/1 count. Edit dialogs and physical drag input were not exercised. The suite restores the prior emulator state after each test.

Final verification: the combined debug build/unit/instrumentation command completed successfully. There are 15 new CategoryNavigationTest cases plus the existing instrumentation smoke test. The generated `app/build/reports/androidTests/connected/debug/index.html` was read and confirms 16 tests, zero failures, zero skipped, duration 2m2.63s. This report is generated evidence and is not staged or committed. The existing arithmetic unit test also passed; no new local unit tests were added because these regressions depend on Android's adapters, touch dispatch and fragment lifecycle.

Final `git diff --check` passed. The production diff is limited to PlanCategoriesFragment.kt, ShopCategoriesFragment.kt and CategoryPagerAdapter.kt. CategoryNavigationTest.kt and the updated notes are the other Task 2 files. At the initial Task 2 handoff, the staging area was empty and no Task 2 changes had been committed or pushed; the user subsequently approved finalisation. Existing local deploymentTargetSelector.xml changes were left alone. Editor inspections reported warnings but no errors; existing deprecation/accessibility warnings were not broadened into unrelated cleanup. Physical-device testing, actual finger double taps and drag reordering, and a full import/export regression are not yet verified. No student-performed manual check is claimed.

TODO (Yasin): reflect on why an intermittent bug needs timing-specific tests and why passing ordinary tests was not enough. Add your own observations and screenshots; no personal reflection has been invented.


## Task 3 – Reduce duplication

### Phase A: candidates and selection

Starting revision: `0ed4e3818872e46f61f9c3da87ae66dfbea22700`, the approved Task 2 commit. Before Task 3, the only local changes were the existing IDE configuration files; no application changes were pending.

**Candidate A: overview double-tap navigation (selected).**

- Exact locations: `PlanCategoriesFragment.onViewCreated`, lines 81–100, and `ShopCategoriesFragment.onViewCreated`, lines 38–61 at the starting revision. The scope is the local GestureDetector construction through RecyclerView.setOnTouchListener.
- Duplicated work: construct the detector; hit-test a row; read its stable ID; resolve its current category index; ignore missing IDs; navigate; forward touch events while returning false to preserve normal touch handling.
- Why it exists: the modes use separate overview fragments, with different overview positions. Shop also converts raw screen coordinates to RecyclerView coordinates, while Plan already uses local event coordinates. These are near-duplicate blocks, not byte-for-byte copies.
- Baseline: 20 + 24 = **44 physical lines**, or 17 + 21 = **38 nonblank, non-comment lines**. This includes braces and listener setup but excludes imports and surrounding blank separator lines.
- Proposed refactoring: one small RecyclerView extension in CategoryNavigation.kt with a current-categories provider and a navigation callback. Keep the mode-specific page offset in each fragment. Use the event's RecyclerView-local x/y for hit-testing in the shared listener; this is the coordinate system expected by findChildViewUnder. The provider must be called when a double tap occurs, not captured as a list snapshot.
- Benefit: one place to maintain gesture recognition and Task 2's stable-ID lookup; both modes receive future fixes together. It avoids a shared fragment hierarchy and adds no dependency.
- Risks: coordinates, touch-event consumption, async list timing, or accidentally applying Plan's offset to Shop. The existing Task 2 tests cover both modes, offsets, duplicate names, pending diffs, removed IDs, single taps and pager identity. Additional tests will check navigation after scrolling each overview.

**Candidate B: drag-reorder callback setup (not selected).**

- Exact locations: `PlanCategoriesFragment.onViewCreated`, lines 40–61, and `PlanCategoryFragment.onViewCreated`, lines 43–64 at the starting revision.
- Duplicated work: an UP/DOWN ItemTouchHelper.SimpleCallback, adapter-position lookup, placeholder rejection, conversion from UI positions to model indices, adapter.moveItem, empty onSwiped and disabled long-press drag.
- Why it exists: Plan has two editable lists: category headings and the items inside a category. This candidate concerns the two planning screens; Shop has no drag editing.
- Baseline: **two 22-line blocks = 44 physical lines**, or **40 nonblank, non-comment lines**. After trimming indentation and ignoring blank/comment lines, 19 of the 20 lines in each block match; the ViewModel reorder call differs.
- Possible refactoring: a callback factory taking the adapter/current-list provider and an onMove callback for either reorderCategory or reorderItem.
- Benefit: centralise drag direction, placeholder rules and index conversion; reduce the risk of fixing only one Plan editing screen.
- Risks: ItemTouchHelper event ordering, asynchronous adapter.moveItem interactions, adapter initialisation and placeholder indices. Existing regression tests call the ViewModel reorder method rather than simulating a physical drag, so they would not fully verify this extraction.

| Criterion | Candidate A: navigation | Candidate B: drag setup |
| --- | --- | --- |
| Maintainability | One shared Plan/Shop behavior and stable-ID rule | One shared rule for two Plan editing lists |
| Simplicity | Small extension, two callbacks at call sites | Factory needs adapter access plus model/adapter movement coordination |
| Regression risk | Lower, with substantial existing coverage | Higher, because drag event ordering lacks direct coverage |
| Ease of verification | Existing 15 Task 2 cases plus scrolled-overview checks | Would need additional drag-event tests/manual evidence |
| Selection | Recommended and authorised by the user's small/low-risk criterion | Documented for later consideration; not implemented |

### Phase B: original state and success criteria (recorded before implementation)

This section was written before changing production or test code for Task 3. The source blocks were read through Android Studio's buffer-aware tools. Their counts were calculated from those exact strings, using JavaScript in the tool orchestrator; no shell file-reading scripts were used. The measurement excludes imports, blank lines and comment-only lines for the second count, but includes braces. It is a scoped manual extraction measurement, not a repository-wide clone-detector score.

Success criteria:

1. Replace **two** overview gesture/ID-resolution implementations with **one** shared implementation and **two** call sites. Neither fragment should retain its own GestureDetector or stable-ID index lookup.
2. Reduce the combined scoped implementation below its baseline **38 nonblank, non-comment lines**, counting the entire new helper function plus both replacement call sites. Report physical lines as well. Count new helper overhead, not just deleted fragment lines; exclude test/documentation lines from this production-code metric.
3. Preserve stable-ID selection from the actual visible row and resolve it against the latest model at gesture time. Missing IDs and Plan Add placeholders must remain ignored; single taps must stay on the overview.
4. Preserve Plan's index + 1 and Shop's index offsets, the pager identity fix, normal editing/shopping controls, XML appearance and touch-event return behavior. No ViewModel, pager, layout or dependency changes.
5. Debug build must pass; the existing unit test and all 16 instrumentation tests must pass, including all 15 Task 2 regression cases. Add Plan and Shop scrolled-overview navigation cases to cover the coordinate consolidation. These will use the existing Activity touch-dispatch approach, with no extra library.
6. Git diff must be limited to the two overview fragments, the shared helper, navigation tests and these notes. Do not stage, commit, push or alter local IDE configuration.
7. Manual checks still needed: actual finger double taps after vertical scrolling, swipe paging, physical category drag then double tap, edit dialogs, and import/export on a real device or emulator. Automated Activity dispatch bypasses OS input injection and cannot establish those manual results.

### Phase C: refactoring implementation

The baseline/candidate/success-criteria section above was saved first. Then two scrolled-overview tests were added and run against the original production code: **2 passed** on emulator-5554. Only after that baseline run was the implementation extracted.

A new internal RecyclerView extension, `setCategoryNavigation`, lives in CategoryNavigation.kt. It owns the GestureDetector, local-coordinate row hit-testing, stable-ID lookup against the latest categories and forwarding touch events. It receives a provider rather than a list snapshot so the pending-diff fix remains effective. Its navigation callback receives the current category index. Each fragment has one four-line call site and retains its own page offset (Plan +1, Shop +0).

Shop's old raw-screen-to-local conversion is replaced by the event's existing RecyclerView-local x/y in the common listener. The new scroll tests are important because both the toolbar offset and parent scroll offset affect hit-testing. The listener still returns false from onTouch, and unmatched IDs/no row still return false from onDoubleTap. The existing detector class and gesture timing are retained. No fragment superclass, ViewModel, pager identity, layout, model, adapter or dependency was changed.

Files changed for Task 3:

- `app/src/main/java/com/mmgpt/shoplistgpt/CategoryNavigation.kt` (new shared extension).
- `app/src/main/java/com/mmgpt/shoplistgpt/PlanCategoriesFragment.kt` (replace local listener with shared call; remove three gesture imports).
- `app/src/main/java/com/mmgpt/shoplistgpt/ShopCategoriesFragment.kt` (same extraction, retaining Shop's offset).
- `app/src/androidTest/java/com/mmgpt/shoplistgpt/CategoryNavigationTest.kt` (two new scrolled-overview cases and shared fixture).
- `docs/CT4111_Topic08_Lab_Notes.md` (candidates, criteria, measurements, evidence and actual prompt).

### Phase D: before-and-after comparison

The same scoped counting rule from Phase B was applied to the final buffer contents: trim each line, exclude empty lines and comment-only lines, retain braces and signatures. The complete helper function is included, not just its inner statements. Package/import/Javadoc lines are outside this defined scope.

| Scope | Before physical lines | Before nonblank/non-comment | After physical lines | After nonblank/non-comment |
| --- | ---: | ---: | ---: | ---: |
| Plan overview block/call | 20 | 17 | 4 | 4 |
| Shop overview block/call | 24 | 21 | 4 | 4 |
| Shared helper function | 0 | 0 | 20 | 19 |
| Total selected scope | **44** | **38** | **28** | **27** |

Result: **one duplicate implementation eliminated** (two implementations → one plus two call sites). There are **11 fewer nonblank/non-comment production lines** in the selected scope, a 28.9% reduction, or **16 fewer physical lines** in that scope. This is not a claim that all 44 old lines were identical, nor a repository-wide duplication percentage. The new helper file has 28 physical lines including its package, imports, comment and spacing; those file-level lines are explicitly excluded from the scoped function metric, just as imports were excluded from the original blocks.

As a separate whole-file cross-check, counting every newline-terminated physical line (including package/import/comment/blank lines, but not an extra empty split entry after the final newline), the original two files totalled 220 lines (146 + 74). The two refactored files plus the helper total 206 (127 + 51 + 28): **14 fewer production-file lines overall**. Test and documentation additions are not included in this code-size comparison.

The smaller call sites still have the mode-specific page assignment; that intentional difference remains local and visible. The two Plan drag blocks and common fragment binding/lifecycle code remain duplicated. They were not refactored because that would expand this task's selected scope.

### Phase D: verification results

| Check | Actual result |
| --- | --- |
| New Plan/Shop scroll tests before production extraction | 2 passed |
| Debug build + unit + full instrumentation after extraction | Build successful; 1 unit test passed, 0 skipped, 0 failed; 18 instrumentation tests passed, 0 skipped, 0 failed |

Post-refactoring command: `:app:assembleDebug :app:testDebugUnitTest :app:connectedDebugAndroidTest --rerun-tasks --stacktrace`. The completed suite contains 18 instrumentation tests (the previous 16 plus the 2 scroll tests), including all 15 Task 2 navigation cases. The generated Android report at `app/build/reports/androidTests/connected/debug/index.html` was read and confirms 18 tests, zero failures, zero skipped, duration 2m17.51s. This generated report is evidence to inspect/save, not a file to commit. The test fixture builds 20 categories, asserts that the NestedScrollView really scrolled, double-taps the last visible category and checks both the pager index and displayed heading. The existing fixture restores the emulator's prior shopping state after each test.

### Phase E: evaluation, limitations and reflection suggestions

Evaluation against the pre-recorded criteria: the two local implementations were eliminated in favour of one shared implementation; the scoped count dropped below 38 to 27; both mode-specific page offsets remain local; all 15 Task 2 tests plus both new scroll cases passed; the debug build and unit test passed. No unintended behavioral change was observed within these automated checks. This supports preservation of navigation and the tested controls, not a guarantee about every app feature.

At the initial Task 3 review (before the subsequent approval to commit), only the five listed Task 3 files were intentionally changed. `git diff --check` passed, the staging area is empty, and HEAD remains `0ed4e38` on main tracking origin/main. The four IDE-file hashes recorded before Task 3 matched after the build/tests, including the existing modified deploymentTargetSelector.xml. No IDE configuration was edited, no generated artifact was staged, and Task 3 was not committed or pushed. Editor inspections found warnings but no errors; inherited detector deprecations and unrelated style warnings remain out of scope.

All automated success criteria were met. The remaining manual checks listed in Phase B are explicitly unexecuted. These tests use timestamped Activity-level touch dispatch, bypass OS injection and use ViewModel methods for reordering; they do not simulate physical drag or certify import/export. Fixed layout-settling waits and the single API 37 emulator limit the breadth of evidence. No student-performed manual result is claimed.

Reflection suggestions for Yasin to review: compare extracting shared behavior with merely shortening files; consider why tests made navigation a lower-risk choice than drag callbacks; explain why a modest net line reduction can still remove a meaningful maintenance obligation. These are suggestions, not personal reflections attributed to the student.

## Version control and evidence references

The starting branch was `main`, tracking `origin/main`, at `e285176` (“Added some icons that were missing.”). The preceding commit was `8bd95c5` (“Initial Shopping List app code for programming assignments.”). These are pre-existing commits, not commits made for this maintenance work.

Existing local IDE changes were present before the assistant's changes: modified `.idea/deploymentTargetSelector.xml`, untracked `.idea/codeStyles/` and `.idea/deviceManager.xml`. The assistant has not edited or reverted their local contents. The accidental commit inclusion and later correction are recorded below.

Task 1 commit attempt: the staged-name review showed only `app/build.gradle.kts` and these notes. The subsequent commit `c16a122` (Complete Task 1 Android build and verification) unexpectedly included `.idea/codeStyles/Project.xml`, `.idea/codeStyles/codeStyleConfig.xml` and `.idea/deviceManager.xml` as well. The cause of the staging change has not been established. That commit was not pushed. The user explicitly approved amending it while preserving local IDE files and Task 2 work. An isolated temporary Git index was populated from the old commit, and only the three accidental IDE entries were removed before amending. The normal index was then updated with git rm --cached for those files; no working file was deleted. No reset, clean or force-push was used.

Corrected Task 1 commit: [`a48aa19ff4f8b7a52701d393887f42d9a4ba1721`](https://github.com/ymstack/ShopListAssmt/commit/a48aa19ff4f8b7a52701d393887f42d9a4ba1721), “Complete Task 1 Android build and verification”. Its file list contains exactly app/build.gradle.kts and this notes file's Task 1 version. A comparison against c16a122 confirmed those two committed contents did not change. All nine working-file hashes recorded before the amendment matched afterward: three excluded IDE files, deploymentTargetSelector.xml, three Task 2 production files, the regression test and current notes. This confirms that the local IDE contents and Task 2 work were preserved.

The Task 2 review confirmed the production changes are restricted to the two overview navigation handlers and pager identity handling; there are no layout changes or additional dependencies. Regression tests explicitly cover the two demonstrated failures, with both pager position and visible content assertions. Finalisation rerun: `:app:assembleDebug :app:testDebugUnitTest :app:connectedDebugAndroidTest --rerun-tasks --stacktrace` completed successfully after the Git correction. Unit tests: 1 passed, 0 skipped, 0 failed. Instrumentation tests on emulator-5554: 16 passed, no failed tests. The forced rerun executed the tasks again rather than relying on the previous run. Emulator logs included slow frames and a long GC pause, but the completed test run reported no failures.

Published Task 2 commit: [`0ed4e3818872e46f61f9c3da87ae66dfbea22700`](https://github.com/ymstack/ShopListAssmt/commit/0ed4e3818872e46f61f9c3da87ae66dfbea22700), “Fix category navigation and add regression tests”, following corrected Task 1 commit a48aa19. Its five-file scope is CategoryPagerAdapter.kt, PlanCategoriesFragment.kt, ShopCategoriesFragment.kt, CategoryNavigationTest.kt and these notes. The remote main branch was verified to contain both commits. Neither commit introduced IDE settings or generated files.

Approved Task 3 commit message: “Refactor duplicated category navigation logic”. Its five intended files are listed in Task 3 above. The final source/test hashes match the tested implementation, so no new behavior change was introduced during submission preparation. The final Task 3 hash and remote verification will appear in the separate submission report and handoff after committing; this avoids trying to embed a commit's own hash in its contents. Repository history: https://github.com/ymstack/ShopListAssmt/commits/main/ .

Publication verification must compare the remote main hash with the final local HEAD and confirm Task 1 is its parent. No force-push was used. That completed the Tasks 1/2 publication stage; Task 3 work is documented separately above.

TODO (Yasin): insert actual saved screenshot references. Approved Task 1/2 hashes are above; the final Task 3 hash is in the submission report/handoff. Test reports are normally generated under `app/build/reports/tests/` and `app/build/reports/androidTests/`; confirm the files before citing them. Generated build files should not be committed.

## AI tools and actual prompt record

The assistant used Android Studio MCP tools in this conversation. The IDE presents a Codex assistant; an exact model/version was not independently established. No external coding agents were delegated work. The instructions and outcomes in this document should not be described as student observations unless Yasin verifies them personally.

### Original assignment prompt

````text
You are my AI software engineering assistant working on my university assignment for CT4111 – Generative AI for Software Engineering, Topic 08: AI-Supported Software Maintenance.

## Project context

I have cloned the following GitHub repository and opened it in Android Studio:

https://github.com/ymstack/ShopListAssmt

The original repository is:
https://github.com/mmnuig/ShopListAssmt

This is an existing Android shopping application with two modes: Plan and Shop.

I am using Android Studio on macOS. I have limited experience with Android Studio, Gradle, Android emulators and Android application development, so I need you to handle as much of the technical work as possible while clearly explaining your actions.

## Your responsibilities

Act as a senior Android software engineer and AI-assisted maintenance agent.

You should inspect the existing codebase, investigate issues, implement solutions, run tests and document your findings.

Do not assume something works without verifying it.

Do not rewrite the entire application or introduce unnecessary dependencies.

Preserve the application's existing functionality, architecture and appearance wherever possible.

## Phase 1 — Project inspection and setup

1. Inspect the complete repository structure.
2. Identify the programming language, Android framework, Gradle configuration, SDK requirements and dependencies.
3. Determine whether the project is configured correctly for my installed Android Studio environment.
4. Identify the application's entry point and explain its architecture briefly.
5. Run the appropriate Gradle build and report the results.
6. If the build fails, diagnose the actual error and implement the smallest justified fix.
7. Determine whether an Android emulator is available.
8. If possible, build, install and launch the application on an emulator.
9. Verify that the application launches and that Plan and Shop modes are accessible.

Do not claim that the application runs successfully unless this has actually been verified.

## Phase 2 — Task 2: Investigate and fix category navigation

The assignment describes the following bug:

In Plan mode, the first screen displays a list of categories that can be edited and reorganised.

In Shop mode, the last screen displays the categories and summarises how many items have been bought in each category.

Double-clicking a category name should navigate to the corresponding category page.

However, this navigation is inconsistent. Sometimes the correct page opens, and sometimes it does not.

Your tasks:

1. Find the code responsible for category display, ordering, double-click detection and page navigation.
2. Trace the relevant data flow and event handling.
3. Investigate whether category positions, list indices, ordering, state changes or event timing could explain the problem.
4. Identify the actual root cause using code evidence, rather than guessing.
5. Design test cases covering normal category navigation, reordered categories, both application modes and relevant edge cases.
6. Implement the smallest maintainable fix.
7. Run the tests and report their results.
8. Verify that existing category editing and shopping functionality remain unaffected.

Explain the original bug, the relevant files, why it occurred and how your solution fixes it.

## Phase 3 — Task 3: Reduce code duplication

The application contains duplicated code associated with its Plan and Shop modes.

Your tasks:

1. Inspect the codebase for duplication.
2. Identify at least TWO distinct opportunities to eliminate duplicated code.
3. For each opportunity, explain:
   - Relevant files and functions
   - What is duplicated
   - Why it is duplicated
   - Potential benefits of refactoring
   - Risks of changing the implementation
4. Compare the two opportunities.
5. Recommend ONE refactoring opportunity, prioritising low risk, maintainability and verifiability.
6. Define success criteria before modifying the code.
7. Implement the selected refactoring.
8. Preserve the existing behaviour of both modes.
9. Run relevant tests and build checks.
10. Evaluate whether the refactoring was successful.

Avoid refactoring unrelated parts of the application.

## Phase 4 — Testing and verification

After implementing the changes:

1. Run the relevant unit tests.
2. Run instrumentation or UI tests where practical.
3. Build the application again.
4. Check for regressions in Plan and Shop modes.
5. Report exactly which tests passed, failed or could not be executed.
6. Distinguish automated verification from manual checks.
7. Do not fabricate test results or claim that unexecuted tests passed.

## Phase 5 — GitHub and version control

I need a clear Git commit history demonstrating the development process.

Use small, logically separated commits for:

- Environment or build fixes, if necessary
- Category navigation bug fix and tests
- Code duplication refactoring
- Documentation

Before each commit, review the diff and confirm that only intended files have changed.

Do not force-push, rewrite history or commit generated build files, secrets or machine-specific configuration.

You may prepare commits, but ask me before committing or pushing changes.

## Phase 6 — Assignment evidence and documentation

Create a Markdown file at:

docs/CT4111_Topic08_Lab_Notes.md

This document will serve as the source material for my final Word/PDF submission.

Include:

1. Student details:
   - Name: Yasin Machigov
   - Student ID: 23347116
   - Module: CT4111 Generative AI for Software Engineering
   - Assignment: Topic 08 – AI-Supported Software Maintenance

2. Task 1:
   - Android Studio setup
   - Build process
   - Build and run results
   - Problems encountered
   - AI tools and prompts used
   - Reflection

3. Task 2:
   - Bug investigation
   - Root cause and code evidence
   - Test cases
   - Implementation
   - Test outcomes
   - AI tools and prompts used
   - Reflection

4. Task 3:
   - Two duplication opportunities
   - Comparison and selection rationale
   - Proposed success criteria
   - Refactoring implementation
   - Verification results
   - AI tools and prompts used
   - Reflection

5. GitHub repository:
   https://github.com/ymstack/ShopListAssmt

6. Commit history and evidence references.

Record the actual prompts used, including this original prompt and any follow-up prompts. Do not invent prompts, observations, results, screenshots or personal reflections.

Where evidence is missing, add a clearly labelled TODO for me to complete.

Write the documentation in clear, natural student-level English. Avoid unnecessarily formal language.

## Important working instructions

Work through the phases sequentially.

Start by inspecting the repository and development environment.

Create a short plan before making changes.

Explain what you are about to do and why.

You may inspect files, run safe commands, build and test the project without requesting permission for every step.

Do not delete files, install system-wide software, make major architectural changes or perform destructive operations without my approval.

If something cannot be completed because of Android Studio, SDK, emulator or tool limitations, explain the blocker and provide the exact next action required.

Keep a running record of your actions and findings.

At the end of each phase, provide:
- What you investigated
- Files changed
- Commands executed
- Actual results
- Remaining issues
- What I need to verify manually

## Begin now

Start with Phase 1.

Inspect the project and Android Studio environment, then determine what is required to build and run the application.

Do not begin Task 2 or Task 3 until Phase 1 has been completed and its results have been reviewed.
````

### Follow-up prompt 1 (verbatim)

> Continue with Phase 1.
>
> The debug build has passed and the API 37 emulator is running.
>
> Please:
>
> 1. Install and launch the application on the running emulator.
> 2. Verify that the app opens without crashing.
> 3. Check that both Plan and Shop modes are accessible.
> 4. Capture or identify the screenshots I should take as evidence for Task 1.
> 5. Record the actual build, deployment and verification results in docs/CT4111_Topic08_Lab_Notes.md.
> 6. Leave existing local IDE configuration changes untouched.
> 7. Report any errors or limitations.
>
> Do not begin Task 2 yet.
>
> Once Task 1 is verified, provide a concise summary of the results, files changed and any remaining issues. Wait for my approval before continuing.

### Follow-up prompt 2 (verbatim)

````text
Phase 1 is approved. Good work.

Before starting Task 2, please:

1. Review the final Git diff and confirm that only intended Task 1 changes are included.
2. Verify that the Gradle test dependencies and instrumentation runner changes are necessary and correctly configured.
3. Stage only the Task 1 Gradle changes and the lab notes. Do not include existing local IDE configuration changes or generated files.
4. Create a commit with the message:
   "Complete Task 1 Android build and verification"
5. Push the commit to my GitHub repository:
   https://github.com/ymstack/ShopListAssmt

Then begin Task 2: Investigate the category navigation bug.

The issue described by the lecturer is that double-clicking a category name in the Plan or Shop category overview sometimes navigates to the wrong category page.

Please work systematically:

1. Inspect the category navigation code and identify the relevant files, functions and event handlers.
2. Trace how categories are ordered and how the destination page is calculated.
3. Determine the root cause using actual code evidence.
4. Design meaningful test cases, including category reordering and navigation in both modes.
5. Implement the smallest maintainable fix.
6. Run the tests and check for regressions.
7. Document the investigation, implementation, tests, results and exact AI prompts in the lab notes.

Important:
- Do not guess the root cause.
- Do not rewrite unrelated functionality.
- Do not claim tests passed unless they actually ran.
- Preserve Plan and Shop behaviour.
- Keep the Task 2 changes separate from the Task 1 commit.
- Do not commit or push Task 2 yet.

When finished, report the root cause, files changed, tests executed, actual results and any remaining limitations. Wait for my review before proceeding to Task 3.
````

### Follow-up prompt 3 (verbatim)

````text
Task 2 implementation and test results are provisionally approved.

Before moving to Task 3, please resolve the Git issue and finalise Tasks 1 and 2.

### Part A — Correct the Task 1 commit

You have my approval to amend the unpushed Task 1 commit c16a122, but only if you can safely preserve all existing local IDE configuration changes and Task 2 work.

1. Inspect the commit and working tree.
2. Identify the three IDE files accidentally included.
3. Preserve their current local contents.
4. Remove only those IDE files from the Task 1 commit.
5. Preserve the intended Gradle and lab notes changes.
6. Do not use destructive reset, clean or force-push commands.
7. Verify that the corrected Task 1 commit contains only the intended files.
8. Confirm that Task 2 changes remain intact.

If the amendment cannot be completed safely, stop and explain why.

### Part B — Finalise Task 2

1. Review the Task 2 changes and ensure they are minimal and justified.
2. Confirm that the tests cover both identified navigation defects.
3. Run the build, unit tests and instrumentation tests again.
4. Verify that all relevant tests pass.
5. Update the lab notes with:
   - Both root causes
   - Evidence of the original failures
   - Test cases and outcomes
   - Files modified
   - AI prompts used
   - Limitations of automated testing
6. Stage only the intended Task 2 files.
7. Commit with the message:
   "Fix category navigation and add regression tests"

### Part C — Push to GitHub

After verifying both commits:

1. Push the commits to origin.
2. Confirm the remote branch contains both commits.
3. Provide the commit hashes and GitHub links.
4. Confirm that no IDE configuration files or generated build artifacts were accidentally committed.

Do not begin Task 3 yet.

### Part D — Report

Provide a concise summary of:
- Git corrections
- Final commit history
- Build results
- Test results
- Any outstanding issues

Wait for my approval before beginning Task 3.
````

### Follow-up prompt 4 (verbatim)

````text
Tasks 1 and 2 are approved and successfully pushed to GitHub.

We are now beginning Task 3 of CT4111 Topic 08: AI-Supported Software Maintenance.

## Objective

Identify and reduce code duplication in the existing ShopList Android application, particularly duplication associated with Plan and Shop modes.

Follow the lecturer's requirements exactly:

1. Identify at least TWO distinct opportunities to reduce code duplication.
2. Explain and justify which opportunity should be selected.
3. Define measurable success criteria BEFORE refactoring.
4. Conduct the refactoring using AI.
5. Evaluate and document the results.

## Phase A — Investigate duplication

Inspect the existing Kotlin codebase, focusing on Plan and Shop functionality.

Identify at least two meaningful duplication opportunities.

For each candidate, provide:
- Exact files and functions involved
- What code is duplicated
- Why the duplication exists
- Approximate amount of duplication
- How it could be refactored
- Expected maintainability benefits
- Potential risks

Compare both opportunities and recommend the best one based on maintainability, simplicity, regression risk and ease of testing.

Document both candidates and your recommendation in the lab notes.

## Phase B — Define success criteria

Before changing any implementation code, establish measurable success criteria.

Include:
- Which duplicated code should be eliminated
- How you will measure the reduction
- Which existing behaviours must remain unchanged
- What automated tests will verify the refactoring
- What manual checks remain necessary

Record the original state and success criteria in the lab notes before proceeding.

## Phase C — Implement the refactoring

Proceed with the recommended refactoring, provided it is small, well-defined and low-risk.

Requirements:

1. Make the smallest maintainable changes necessary.
2. Preserve Plan and Shop functionality.
3. Avoid unnecessary architectural changes.
4. Do not modify unrelated features.
5. Do not introduce unnecessary dependencies.
6. Add or update tests where appropriate.
7. Keep the Task 3 changes separate from previous commits.

## Phase D — Verify the results

After refactoring:

1. Run the debug build.
2. Run unit tests.
3. Run instrumentation tests.
4. Verify that existing Task 2 navigation regression tests still pass.
5. Compare duplication before and after refactoring.
6. Report the number of duplicated lines or blocks removed, using a clearly explained measurement.
7. Identify any limitations or remaining duplication.
8. Confirm that no unintended behavioural changes were introduced, to the extent supported by testing.

Do not fabricate results.

## Phase E — Documentation

Update docs/CT4111_Topic08_Lab_Notes.md.

Include:
- Both duplication opportunities
- Comparison and selection rationale
- Original duplication measurements
- Success criteria established before implementation
- Refactoring details
- Files changed
- Test results
- Before-and-after comparison
- Exact AI prompts used
- Limitations
- Reflection notes

Write clearly in natural student-level English.

Do not invent my personal reflections. Instead, provide short reflection suggestions grounded in the actual work for me to review.

## Phase F — Final review

When finished, provide:

1. The two duplication opportunities identified.
2. Which one was selected and why.
3. Exact files changed.
4. Before-and-after duplication measurements.
5. Build and test results.
6. Any remaining risks or manual verification requirements.
7. Git status.

Do not commit or push Task 3 yet.

Do not modify existing IDE configuration files.

Wait for my approval before committing.

Begin Task 3 now.
````

### Follow-up prompt 5 (verbatim)

````text
Task 3 is approved based on the reported implementation and test results.

Please finalise Task 3 and prepare the assignment for submission.

### Part 1 — Finalise GitHub

1. Review the Task 3 diff one final time.
2. Confirm the five intended Task 3 files are the only files being included.
3. Preserve all existing local IDE configuration changes.
4. Stage only the intended Task 3 changes.
5. Commit with the message:
   "Refactor duplicated category navigation logic"
6. Push the commit to origin/main.
7. Verify the remote commit and provide its hash and GitHub link.
8. Confirm that no generated files or IDE settings were committed.

### Part 2 — Final assignment documentation

Review docs/CT4111_Topic08_Lab_Notes.md against the original assignment requirements.

Make sure the document contains:

- My name and student ID
- GitHub repository URL
- Task 1: Android setup, build and deployment
- Task 2: Bug investigation, root causes, tests and fixes
- Task 3: Two duplication opportunities, selection, success criteria, refactoring and evaluation
- AI tools used and exact prompts for each task
- Actual results and limitations
- Reflection notes for each task
- Commit history

Clearly distinguish verified results from anything not manually checked.

Do not invent screenshots, prompts, results or personal reflections.

### Part 3 — Submission preparation

Prepare a clean, well-organised final report suitable for copying into Microsoft Word and exporting as PDF.

Use the title:

CT4111 – Topic 08 Lab Assignment
AI-Supported Software Maintenance

Student: Yasin Machigov
Student ID: 23347116

Use clear headings, concise paragraphs and tables where appropriate.

Keep the writing natural and at university student level, not unnecessarily formal.

Include the actual prompts used. If they are too long for the main report, place them in an appendix.

Leave clearly marked placeholders for screenshots and any reflections that require my personal input.

Do not claim that the PDF has been created unless you actually create it.

After completing the commit and preparing the report, provide the final GitHub link, commit hash, report file location and a list of anything I still need to do manually.
````

### Submission documentation audit

The notes contain student details, repository URLs, setup/build/deployment, both navigation root causes with failing/passing evidence, two duplication candidates, criteria recorded before extraction, measurements, executed tests, limitations and the original prompt plus five follow-ups. Task 1 and Task 2 reflection sections remain prompts for Yasin's own input; Task 3 contains grounded suggestions rather than an invented personal reflection. Screenshot and manual-check gaps are labelled TODO. The separate final report places the exact prompt record in an appendix. No PDF has been created by the assistant.
