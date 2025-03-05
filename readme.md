# Test Coverage Extension

## Introduction

This project introduces a **Test Coverage Extension** for enhancing JaCoCo test coverage by enforcing rules based on Git branches. It is designed to help developers maintain and improve test coverage in a flexible and branch-specific manner.

## Goal

Codebases with sufficient test coverage are more reliable and easier to maintain. However, achieving high test coverage can be challenging, especially in legacy projects or during rapid prototyping phases. Legacy projects may lack tests entirely, or their tests may be outdated or incomplete. Similarly, when working on proof-of-concept implementations, developers often prioritize speed over writing comprehensive tests.

As Michael Feathers aptly states in *Working Effectively with Legacy Code*:  
*“To me, legacy code is simply code without tests.”*

While the JaCoCo plugin is a powerful tool for measuring test coverage, it lacks the ability to enforce coverage rules based on Git branches. Setting a fixed test coverage threshold for an entire project is often impractical, particularly for legacy projects, as it can hinder development progress. Instead, this plugin provides a way to enforce test coverage rules **based on the changes made in a branch compared to another branch** (e.g., `develop`). This approach encourages developers to write tests for the code they modify without imposing a rigid, project-wide coverage requirement.

## Usage

### Prerequisites

Ensure that the JaCoCo plugin is configured in your project. Here’s an example configuration:

```xml
<!-- https://mvnrepository.com/artifact/org.jacoco/jacoco-maven-plugin -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <!-- Attaches the JaCoCo agent to the JVM -->
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>

        <!-- Generates code coverage reports in multiple formats -->
        <execution>
            <id>report</id>
            <phase>prepare-package</phase>
            <goals>
                <goal>report</goal>
            </goals>
            <configuration>
                <outputDirectory>${project.build.directory}/jacoco-report</outputDirectory>
                <formats>
                    <format>HTML</format>
                    <format>XML</format>
                    <format>CSV</format>
                </formats>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Adding the Test Coverage Extension Plugin

To integrate the **Test Coverage Extension Plugin** into your project, add the following configuration to your `pom.xml`:

```xml
<plugin>
    <groupId>tech.linebyline</groupId>
    <artifactId>test-coverage-extension-plugin</artifactId>
    <version>1.0.0-ALPHA</version>
    <configuration>
        <basedir>${project.basedir}</basedir>
        <classpath>${project.build.outputDirectory}</classpath>
        <sourcepaths>
            <sourcepath>src/main/java</sourcepath>
        </sourcepaths>
        <jacocoExecFile>${project.build.directory}/jacoco.exec</jacocoExecFile>
        <branchToCompare>develop</branchToCompare>
        <rules>
            <rule>
                <type>OVERALL</type>
                <threshold>60</threshold>
            </rule>
            <rule>
                <type>PER_CLASS_CHANGED_LINES</type>
                <threshold>95</threshold>
            </rule>
        </rules>
    </configuration>
    <executions>
        <execution>
            <phase>prepare-package</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

The plugin will evaluate test coverage based on the configured rules. If the coverage falls below the specified thresholds, the build will fail.

### Configuration Options

The plugin supports the following configuration parameters:

| **Parameter**       | **Description**                                                                 | **Default Value**                                |
|---------------------|---------------------------------------------------------------------------------|-------------------------------------------------|
| `project`           | The Maven project object, injected by Maven.                                   | `${project}` (read-only, required)              |
| `basedir`           | The base directory of the project.                                              | `${project.basedir}` (read-only)                |
| `classpath`         | The output directory for compiled classes.                                      | `${project.build.outputDirectory}`              |
| `sourcepaths`       | A list of source paths to be analyzed.                                          | (No default value)                              |
| `jacocoExecFile`    | The path to the JaCoCo execution data file.                                     | `${project.build.directory}/jacoco.exec`        |
| `branchToCompare`   | The branch to compare for code coverage analysis.                               | `develop`                                       |
| `rules`             | A list of rules defining thresholds for code coverage validation.               | (No default value)                              |

### Rule Types

The plugin supports the following rule types:

- **`OVERALL`**: The overall test coverage threshold for the entire project.
- **`PER_CLASS`**: The test coverage threshold per changed class compared to the `branchToCompare`.
- **`TOTAL_CHANGED_LINES`**: The test coverage threshold for the changed lines in the entire project compared to the `branchToCompare`.
- **`PER_CLASS_CHANGED_LINES`**: The test coverage threshold per changed line in a class compared to the `branchToCompare`.

## Github actions

To ensure that the plugin works properly in Github actions, it is important to set the fetch depth. 

```yaml
name: Maven verify

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven
      - name: Run the Maven verify phase
        run: mvn --batch-mode --update-snapshots verify
```

Plugin:

```xml
<plugin>
    <groupId>tech.linebyline</groupId>
    <artifactId>test-coverage-extension-plugin</artifactId>
    <version>1.0.0-ALPHA</version>
    <configuration>
        <basedir>${project.basedir}</basedir>
        <classpath>${project.build.outputDirectory}</classpath>
        <sourcepaths>
            <sourcepath>src/main/java</sourcepath>
        </sourcepaths>
        <jacocoExecFile>${project.build.directory}/jacoco.exec</jacocoExecFile>
        <branchToCompare>origin/empty-branch</branchToCompare>
        <rules>
            <rule>
                <type>TOTAL_CHANGED_LINES</type>
                <threshold>10</threshold>
            </rule>
        </rules>
    </configuration>
    <executions>
        <execution>
            <phase>prepare-package</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```


Also, referring to the branch as `origin/<branch name>` is important.

## Contributions

We welcome contributions! Please review our [contributing guidelines](CONTRIBUTING.md) for more information.

To report issues or request features, please open an issue on GitHub.

### Building the Project

To build the project locally, clone the repository and run the following command:

```shell
mvn clean install
```

This will compile the project and execute all tests.

#### GPG Setup

The building will fail locally if GPG is not set up.

### Branching Strategy

We follow the [Gitflow branching model](https://nvie.com/posts/a-successful-git-branching-model/) for this project:

- **`master`**: The main branch, which should only receive merges from `develop` or hotfix branches.
- **`develop`**: The development branch, which should only receive merges from feature or enhancement branches.

All changes should be made in a feature or enhancement branch and merged into `develop` once they are ready. Changes in `develop` will be merged into `master` when a release is prepared.
