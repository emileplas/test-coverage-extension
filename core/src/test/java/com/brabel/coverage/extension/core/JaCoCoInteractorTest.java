package com.brabel.coverage.extension.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static com.brabel.coverage.extension.core.JaCoCoInteractor.getOverallCodeCoverageForChangedFiles;
import static com.brabel.coverage.extension.core.JaCoCoInteractor.getTotalCodeCoverage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JaCoCoInteractorTest {

    private static File singleModuleFile = new File("src/test/resources/jacoco-examples-exec/single-module-example-jacoco-output.exec");

    @Test
    public void testGetTotalCodeCoverageSingleModuleProject(){

        CodeCoverage expectedCodeCoverage = new CodeCoverage(null, CodeCoverage.CoverageType.TOTAL, 21, 51,  5, 10);

        try {
            CodeCoverage actualCodeCoverage = getTotalCodeCoverage(singleModuleFile, new File("../single-module-example/target/classes/com"));

            assertEquals(expectedCodeCoverage.getCoverageType(), actualCodeCoverage.getCoverageType());
            assertNull(actualCodeCoverage.getFilePath());
            assertEquals(expectedCodeCoverage.getInstructionsCovered(), actualCodeCoverage.getInstructionsCovered());
            assertEquals(expectedCodeCoverage.getInstructionsMissed(), actualCodeCoverage.getInstructionsMissed());
            assertEquals(expectedCodeCoverage.getLinesMissed(), actualCodeCoverage.getLinesMissed());
            assertEquals(expectedCodeCoverage.getLinesCovered(), actualCodeCoverage.getLinesCovered());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HashSet<File> getSampleChangedFiles(){
        HashSet<File> changedFiles = new HashSet<>();
        changedFiles.add(new File(".gitignore"));
        changedFiles.add(new File("core/src/main/java/com/brabel/coverage/extension/core/CodeCoverage.java"));
        changedFiles.add(new File("single-module-example/pom.xml"));
        changedFiles.add(new File("single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClass.java"));
        changedFiles.add(new File("core/pom.xml"));
        changedFiles.add(new File("core/src/main/java/com/brabel/coverage/extension/core/JaCoCoInteractor.java"));
        changedFiles.add(new File("core/src/main/java/com/brabel/coverage/extension/core/GitInteractor.java"));
        changedFiles.add(new File("core/src/test/resources/jacoco-examples-exec/single-module-example-jacoco-output.exec"));
        changedFiles.add(new File("pom.xml"));
        changedFiles.add(new File("single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java"));
        changedFiles.add(new File("single-module-example/src/test/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClassTest.java"));
        changedFiles.add(new File("core/src/test/java/com/brabel/coverage/extension/core/GitInteractorTest.java"));
        changedFiles.add(new File("single-module-example/src/test/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClassTest.java"));
        changedFiles.add(new File("core/src/test/java/com/brabel/coverage/extension/core/JaCoCoInteractorTest.java"));
        return changedFiles;
    }

    @Test
    public void testGetOverallCodeCoverageChangedFiles(){
        HashSet<File> sampleChangedFiles = getSampleChangedFiles();

        FileUtil.Filters filters = new FileUtil.Filters();
        filters.addPathIncludeFilter("single-module-example/src/main/java");

        Set<File> result = new FileUtil().filterFiles(getSampleChangedFiles(), filters);

        HashMap<String, CodeCoverage> codeCoveragePerChangedFile;
        try {
            codeCoveragePerChangedFile = getOverallCodeCoverageForChangedFiles(singleModuleFile, new File("../single-module-example/target/classes/com"), result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertEquals(2, codeCoveragePerChangedFile.size());


        CodeCoverage firstFile = codeCoveragePerChangedFile.get("single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClass.java");
        Assertions.assertNotNull(firstFile);
        Assertions.assertEquals(13, firstFile.getInstructionsMissed());
        Assertions.assertEquals(30, firstFile.getInstructionsCovered());
        Assertions.assertEquals(3, firstFile.getLinesMissed());
        Assertions.assertEquals(7, firstFile.getLinesCovered());
        Assertions.assertEquals(CodeCoverage.CoverageType.CLASS, firstFile.getCoverageType());

        CodeCoverage secondFile = codeCoveragePerChangedFile.get("single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java");
        Assertions.assertNotNull(secondFile);

        Assertions.assertEquals(8, secondFile.getInstructionsMissed());
        Assertions.assertEquals(21, secondFile.getInstructionsCovered());
        Assertions.assertEquals(2, secondFile.getLinesMissed());
        Assertions.assertEquals(3, secondFile.getLinesCovered());
        Assertions.assertEquals(CodeCoverage.CoverageType.CLASS, secondFile.getCoverageType());
    }

/*    public String getTestFilePath() {
        // Get the stack walker to capture the current class's file path
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        Class<?> callerClass = walker.getCallerClass();
        String className = callerClass.getName();

        // Get the file path based on the class name
        Path classFilePath = Paths.get(callerClass.getProtectionDomain().getCodeSource().getLocation().getPath(),
                className.replace('.', '/') + ".class");

        // Convert to the original source file path, if needed
        String sourceFilePath = classFilePath.toString().replace(".class", ".java");
        return sourceFilePath;
    }*/

/*    private File getClassPathDirectory() {
        String testFilePath = getTestFilePath();

    }*/
}
