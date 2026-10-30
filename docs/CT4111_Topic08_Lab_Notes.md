# CT4111 Topic 08 – Lab notes

## Student details

- Name: Yasin Machigov
- Student ID: 23347116
- Module: CT4111 Generative AI for Software Engineering
- Assignment: Topic 08 – AI-Supported Software Maintenance
- Repository: https://github.com/ymstack/ShopListAssmt
- Original repository: https://github.com/mmnuig/ShopListAssmt

These are working evidence notes, not a completed personal reflection. Results below come from tool responses and emulator UI inspections. Tasks 2 and 3 have not started.

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

These mode checks were tool-driven UI interactions with UI hierarchy inspection, not an automated assertion-based UI test and not a student-performed manual test. Import/export, item editing, purchases, reordering and category double-click navigation were not tested in Task 1. Task 2 investigation has not begun.

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

Not started. Waiting for the user's Phase 1 review.

TODO: bug investigation, root cause with source evidence, test cases, implementation, test outcomes, actual prompts and Yasin's reflection.

## Task 3 – Reduce duplication

Not started.

TODO: identify and compare two opportunities, select one, define success criteria before editing, implement, verify and record actual prompts. Yasin to add personal reflection.

## Version control and evidence references

The starting branch was `main`, tracking `origin/main`, at `e285176` (“Added some icons that were missing.”). The preceding commit was `8bd95c5` (“Initial Shopping List app code for programming assignments.”). These are pre-existing commits, not commits made for this maintenance work.

Existing local IDE changes were present before the assistant's changes: modified `.idea/deploymentTargetSelector.xml`, untracked `.idea/codeStyles/` and `.idea/deviceManager.xml`. The assistant has not edited, staged or reverted these.

Maintenance files so far: `app/build.gradle.kts` (test setup) and this notes file. No commit or push has been performed. Proposed later commit split: test environment fix; navigation fix/tests; selected refactoring; documentation. Each diff must be reviewed before committing, and the user must approve commits/pushes.

TODO: add approved maintenance commit hashes and actual saved screenshot/report references. Test reports are normally generated under `app/build/reports/tests/` and `app/build/reports/androidTests/`; confirm the files before citing them. Generated build files should not be committed.

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

TODO: append later follow-up prompts verbatim as work proceeds.
