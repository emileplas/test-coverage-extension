package tech.linebyline.coverage.extension.core;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ExampleModuleFixture {

    public static final File JACOCO_EXEC = new File("../single-module-example/target/jacoco.exec");

    private ExampleModuleFixture() {
    }

    public static void requireCoverageRecording() {
        assertTrue(JACOCO_EXEC.isFile(), "Missing " + JACOCO_EXEC.getPath()
                + ". It is written when the single-module-example tests run with the JaCoCo agent. "
                + "Run `mvn verify` from the project root, or `mvn test -pl core -am` "
                + "without -Dtest, -DskipTests or -Djacoco.skip.");
    }
}
