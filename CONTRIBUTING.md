# Contributing

## Build, run, test

```shell
./gradlew :composeApp:run                          # Desktop
./gradlew :composeApp:wasmJsBrowserDevelopmentRun  # Web (Wasm)
./gradlew :composeApp:jsBrowserDevelopmentRun      # Web (JS)

./gradlew :composeApp:jvmTest
./gradlew :composeApp:jsBrowserTest :composeApp:wasmJsBrowserTest   # needs Chrome/Chromium (set CHROME_BIN if necessary)
```

Verify every target after changing shared code. A green `jvmTest` says nothing about JS or Wasm.

## Language

- Source code is English: identifiers, comments, KDoc, test names, file and package names.
- User-facing text is German, because the app calculates German maintenance law for German users.
- KDoc names the German legal term where an English identifier stands for it (see the glossary below).

## Tooling

- `.editorconfig` defines formatting, including the 150-column limit.
- The Kotlin compiler runs with `allWarningsAsErrors`.

## Code style

### Documentation

- Comments explain why, or anything the code cannot express. They never restate the code.
- KDoc describes public API contracts.
- Non-KDoc documentation describes global goals, not individual steps.
- Rules for working on the code belong in CONTRIBUTING.md.

### Structure

- Each file has one primary top-level declaration, plus its private helpers and Compose previews.
- Code lives in the module and package that own its responsibility.
- Avoid catch-all names such as Utils, Helper, Manager, and Common.
- Use nesting instead of name prefixes (Animal.Dog, not AnimalDog).
- Nest only small, closely related types. Move larger types to a subpackage.
- Use a top-level object for functions only when the namespace improves call sites.
- Order class members: properties, init blocks, public, internal, private, companion object.

### Design

- Keep code minimal. If a design needs special cases, change the design.
- Make invalid states unrepresentable. Prefer sealed types to flags and nullable combinations.
- Prefer immutable data and val.
- Use the narrowest possible visibility.
- Prefer exhaustive when expressions without an else branch for sealed types and enums.
- Do not use !!. Model absence or fail with a clear error.

### Kotlin Multiplatform

- Code belongs in commonMain unless it requires a platform API.
- Keep expect/actual declarations thin. Prefer interfaces when an implementation has state or needs to be replaced in
  tests.
- Name platform files by source set (Foo.android.kt, Foo.ios.kt).
- Prefer official multiplatform libraries to platform-specific implementations.

### Concurrency and errors

- Use structured concurrency. Do not use GlobalScope.
- Inject dispatchers. Do not hardcode them in business logic.
- Suspend functions must be safe to call from the main thread.
- Never swallow CancellationException.
- Use one error model per layer, and convert errors at layer boundaries.

### Testing

- Tests belong in commonTest unless they verify platform-specific behavior.
- Test behavior rather than implementation details.
- Test code lives only in test source sets. Production code contains no test-only code or hooks.
- Test utilities shared across modules live in one dedicated testing module.

### Quality

- Limit lines to 150 columns.
- Enforce these rules with tooling wherever possible.
- Fix warnings instead of suppressing them. Every suppression states why the warning does not apply.

### User-Facing Text

- All user-facing text, including error messages, is extremely short and to the point.
- Error messages state only what went wrong and, if the user can act, what to do next.

## Glossary

| Identifier | German term |
|---|---|
| maintenance | Unterhalt |
| need, baseNeed, remainingNeed | Bedarf, Regelbedarf, Restbedarf |
| childBenefit | Kindergeld |
| studentGrant, studentGrantIsAdvance | BAföG, Vorausleistung (§ 36 BAföG) |
| selfReserve | (angemessener) Selbstbehalt |
| warmRent | Warmmiete |
| healthInsurance, tuitionFees | Kranken-/Pflegeversicherung, Studiengebühren |
| priorObligations | vorrangige Unterhaltspflichten |
| available | einsetzbares Einkommen |
| quota | Haftungsquote |
| shortfall | Mangelfall, Fehlbetrag |
| apprenticeshipPay, trainingCostDeduction | Ausbildungsvergütung, ausbildungsbedingter Aufwand |
| sideJob, sideJobAllowance | Nebenjob, Freibetrag |
| minijobLimit | Minijob-Grenze |
| derivation, legalBasis, note | Rechenweg, Rechtsgrundlage, Erläuterung |
| LegalYear | Rechtsstand (Düsseldorfer Tabelle, Kindergeld, Minijob-Grenze eines Jahres) |

## Adding a legal year

Append an entry to `LegalYear` in `composeApp/src/commonMain/kotlin/de/consansri/unterhaltsrechner/calculation/LegalYear.kt`
(or replace `Status.Provisional(...)` of an existing entry by the default `Status.Official`) and take the values from the new
Düsseldorfer Tabelle. `LegalYear.at(date)` picks the newest entry valid on a date. Entries must stay sorted ascending.

Sources: [Düsseldorfer Tabelle (OLG Düsseldorf)](https://www.olg-duesseldorf.nrw.de/infos/Duesseldorfer_Tabelle/index.php),
Unterhaltsleitlinien der Oberlandesgerichte, Bundeskindergeldgesetz, Minijob-Zentrale.
