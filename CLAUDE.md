# CLAUDE.md

## Project overview

**test-coverage-extension** is a Maven plugin (published to Maven Central) that enforces test coverage on changed lines using JaCoCo + git diff. It compares your branch against a base branch and validates coverage rules on the changed code.

- **Group ID**: `tech.linebyline`
- **Current version**: `1.0.2-ALPHA`
- **Java**: 17 (SDKMAN managed)
- **Repo**: https://github.com/emileplas/test-coverage-extension

## Module structure

```
test-coverage-extension/          # Parent POM (packaging: pom)
├── core/                         # Core logic — git diff parsing, JaCoCo integration, rule validation
├── maven-plugin/                 # Maven Mojo entry point (CoverageCheckMojo)
└── single-module-example/        # Test fixture project used by integration tests
```

### Key packages (core module)

- `tech.linebyline.coverage.extension.core` — `CoverageChecker` orchestrator
- `...core.integration` — `GitInteractor` (git subprocess), `JaCoCoInteractor` (JaCoCo API)
- `...core.model` — `Rule`, `RuleValidationResult`, `CodeCoverage`
- `...core.services` — `RuleManager`
- `...core.configuration` — `ConfigurationManager`

### Rule types

- `OVERALL` — Total project coverage threshold
- `PER_CLASS` — Per-file coverage threshold on changed files
- `TOTAL_CHANGED_LINES` — Aggregate coverage on all changed lines
- `PER_CLASS_CHANGED_LINES` — Per-file coverage on changed lines only

## Build & test

```bash
mvn clean install           # Build all modules + run tests
mvn test -pl core           # Run only core tests
mvn test -pl maven-plugin   # Run only plugin tests
```

Tests use JUnit 5 + Mockito. The `single-module-example` module provides test fixtures (compiled classes + jacoco.exec) for integration tests.

## Branching & workflow

- **`main`** — production, releases are cut from here
- **`develop`** — integration branch, PRs target this
- Feature branches: `feature/<description>` or `task/<description>`
- Default diff comparison branch in plugin config: `develop`

## Working with issues

All work is tracked via GitHub Issues and the GitHub Project board (#4, project ID `PVT_kwHOAl11PM4Azc-d`). Always:

1. **Work from issues** — every code change should reference an issue
2. **Use milestones** — issues are grouped into release milestones (1.0.3, 1.0.4, etc.)
3. **Respect priority labels** — P0-critical, P1-important, P2-minor
4. **Update the board** — move issues through Status (Backlog → Ready → In progress → In review → Done)
5. **Branch naming** — use the issue number: `fix/56-hunk-header-matching` or `feature/71-python-port`
6. **Update status on PR creation** — when a PR is created for an issue, move it to "In review" on the board

When creating new issues, always:
- Assign a priority label (P0/P1/P2)
- Assign to the appropriate milestone
- Add to the project board with Priority, Size, and Start/End dates

### Project board field IDs (for `gh project item-edit`)

- **Status**: `PVTSSF_lAHOAl11PM4Azc-dzgpPguE`
  - Backlog: `f75ad846`, Ready: `61e4505c`, In progress: `47fc9ee4`, In review: `df73e18b`, Done: `98236657`
- **Priority**: `PVTSSF_lAHOAl11PM4Azc-dzgpPgzM`
  - P0: `79628723`, P1: `0a877460`, P2: `da944a9c`
- **Size**: `PVTSSF_lAHOAl11PM4Azc-dzgpPgzY`
  - XS: `6c6483d2`, S: `f784b110`, M: `7515a9f1`, L: `817d0097`, XL: `db339eb2`
- **Start date**: `PVTF_lAHOAl11PM4Azc-dzgpPgzw`
- **End date**: `PVTF_lAHOAl11PM4Azc-dzgpPgz8`

## Publishing

Published to Maven Central via Sonatype Central Publishing plugin. Artifacts are GPG-signed. The `single-module-example` module is excluded from publishing.

## Code style

- No specific formatter enforced yet — follow existing conventions
- Keep methods focused; the codebase already has clear separation between git, JaCoCo, and rule logic
- Prefer explicit error messages over generic catches
- Use try-with-resources for all I/O