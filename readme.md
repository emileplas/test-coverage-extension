# Test coverage extension

## Introduction

Test coverage extension for extending JaCoCo test coverage with rules based on Git branches.

## Goal

Code bases with sufficient test coverage are more reliable.

However, the projects we work on don't always have the test coverage we would like. Legacy projects
may have no tests at all, or the tests may be outdated or incomplete. Sometimes, working on a proof 
of concept requires us to write code quickly, and we may not have time to write tests sufficiently.

“To me, legacy code is simply code without tests. I’ve gotten some grief for this definition.”

— Working Effectively with Legacy Code by Michael Feathers

Although we have the JaCoCo plugin to measure test coverage, it doesn't provide a way to enforce test coverage rules based on Git branches.
It is often not doable to set a fixed test coverage threshold for the entire project. Especially on legacy projects. That would slow 
development down too much. Therefor, we need a way to enforce test coverage rules based on the changes that are made in a branch compared 
to another branch (e.g. develop).

This plugin provides a way to enforce test coverage rules based on the changes that are made in a branch compared to another branch (e.g. develop).
This way developers are encouraged to write tests for the code they change, while not being slowed down by a fixed test coverage threshold for the entire project.

## Usage

First make sure you have the JaCoCo plugin configured in your project. Since this is an extension of the JaCoCo plugin.

E.g:

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

Add the test-coverage-extension-plugin to your project:

```xml
<plugin>
    <groupId>com.brabel</groupId>
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

The plugin will check the test coverage based on the rules you have configured in the plugin configuration. The plugin will fail the build if the test coverage is below the configured threshold.

### Configuration

The plugin has the following configuration options:

Here’s a table for your README file based on the provided code. The table includes the configuration parameters, their descriptions, and default values (if any):

| Configuration Parameter | Description                                                                 | Default Value                                      |
|-------------------------|-----------------------------------------------------------------------------|---------------------------------------------------|
| `project`               | The Maven project object, injected by Maven.                               | `${project}` (read-only, required)                |
| `basedir`               | The base directory of the project.                                          | `${project.basedir}` (read-only)                  |
| `classpath`             | The output directory for compiled classes.                                  | `${project.build.outputDirectory}`                |
| `sourcepaths`           | A list of source paths to be analyzed.                                      | (No default value)                                |
| `jacocoExecFile`        | The path to the JaCoCo execution data file.                                 | `${project.build.directory}/jacoco.exec`          |
| `branchToCompare`       | The branch to compare for code coverage analysis.                           | `develop`                                         |
| `rules`                 | A list of rules defining thresholds for code coverage validation.           | (No default value)                                |

### Rules

The plugin supports the following rule types:

- `OVERALL`: The overall test coverage threshold for the entire project.
- `PER_CLASS`: The test coverage threshold per changed class compared to the `branchToCompare`.
- `TOTAL_CHANGED_LINES`: The test coverage threshold for the changed lines in the entire project compared to the `branchToCompare`.
- `PER_CLASS_CHANGED_LINES`: The test coverage threshold per changed line in a class compared to the `branchToCompare`.

## Contributions

Contributions are welcome! Please read our [contributing guidelines](CONTRIBUTING.md) for more information.

For issues or feature requests, please open an issue on GitHub.

Once you have cloned the repository, you can build the project using Maven:

```shell
mvn clean install
```

This will build the project and run the tests.

Please work on issues. If you want to work on something that isn't in the issues list, please create an issue first.

### Branching strategy

We use the [Gitflow branching model](https://nvie.com/posts/a-successful-git-branching-model/) for this project.
- `master` is the main branch and should only receive merges from `develop` or hotfixes.
- `develop` is the development branch and should only receive merges from feature, enhancement branches.

This means that all changes should be made in a feature or enhancement branch and then merged into `develop` when ready.

Changes in `develop` will be merged into `master` when a release is ready.

