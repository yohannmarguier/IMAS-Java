# Repository Guidelines

Any changes should be also reflected in AGENTS.md

## Project Structure & Module Organization

`src/imasjava/` contains the hand-written Java High-Level Interface (HLI), including vector types and wrapper code. `wrapper/` contains the JNI C++ binding. Java IDS and identifier classes are generated during the CMake build from `IDSDef2Java.xsl`, `identifiers.xsl`, and the IMAS Data Dictionary; do not edit generated files under the build directory. `examples/` contains executable Java integration tests, while `tests/junit/` contains JUnit 5 unit tests. Sphinx documentation lives in `doc/`; shared CMake and XSLT utilities are in `common/`.

## Build, Test, and Development Commands

Use an out-of-source build. CMake normally fetches IMAS-Core and related dependencies; for a sibling checkout layout, use `-D AL_DOWNLOAD_DEPENDENCIES=OFF`.

```sh
cmake -B build -D AL_EXAMPLES=ON -D AL_TESTS=ON
cmake --build build --parallel
ctest --test-dir build --output-on-failure
cmake -B build-docs -D AL_HLI_DOCS=ON -D AL_DOCS_ONLY=ON
cmake --build build-docs --target al-java-docs
```

The first command configures Java, JNI, generated sources, and enabled backends. The CTest suite executes the Java examples; backend-dependent tests may be skipped when neither MDSplus nor HDF5 is enabled. `ci/build_and_test.sh` documents the full CI configuration and its required module environment.

## Coding Style & Naming Conventions

Follow the surrounding Java style: two-space indentation, braces on the declaration line, and Javadoc for public APIs. Use PascalCase for classes (`LimitedSizeStringBuilder`), camelCase for methods and fields, and `Test<ClassOrFeature>` for integration-test classes. Keep JNI changes paired between `wrapper/imasjava_wrapper_LowLevel.h` and `.cpp`. There is no repository formatter or linter configured; avoid unrelated reformatting.

## Testing Guidelines

Add or update a matching `examples/Test*.java` test for new Java behavior; the CMake helper reports unmatched example sources. Put focused unit tests in `tests/junit/imasjava/`, using JUnit Jupiter `@Test` and descriptive method names. Run the relevant CTest selection with `ctest --test-dir build -R example-java-<Name> --output-on-failure` before the full suite.

## Commit & Pull Request Guidelines

Write short, imperative commit subjects, as in `update readme` or `fix for idsinfo issue`; keep each commit narrowly scoped. Start feature work from the latest `develop` branch. Open or reference an issue before proposing substantial changes, explain the behavior and validation in the PR description, and include documentation updates or screenshots only when user-facing docs or output change. Follow `CONTRIBUTING.md` and the Code of Conduct.

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

When the user types `/graphify`, use the installed graphify skill or instructions before doing anything else.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- Dirty graphify-out/ files are expected after hooks or incremental updates; dirty graph files are not a reason to skip graphify. Only skip graphify if the task is about stale or incorrect graph output, or the user explicitly says not to use it.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
