package com.brabel.coverage.extension.core.integration;

import com.brabel.coverage.extension.core.util.FileUtil;
import com.brabel.coverage.extension.core.model.CodeCoverage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static com.brabel.coverage.extension.core.integration.JaCoCoInteractor.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JaCoCoInteractorTest {

    private static File singleModuleFile = new File("src/test/resources/jacoco-examples-exec/jacoco.exec");

    @Test
    public void testGetTotalCodeCoverageSingleModuleProject(){

        CodeCoverage expectedCodeCoverage = new CodeCoverage(null, CodeCoverage.CoverageType.TOTAL, 21, 51,  5, 10);

        try {
            JaCoCoInteractor jaCoCoInteractor = new JaCoCoInteractor(singleModuleFile, "target/classes", new String[]{"src/main/java"}, new File("../single-module-example/"));
            CodeCoverage actualCodeCoverage = jaCoCoInteractor.getTotalCodeCoverage();

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
            JaCoCoInteractor jaCoCoInteractor = new JaCoCoInteractor(singleModuleFile, "target/classes", new String[]{"src/main/java"}, new File("../single-module-example/"));
            codeCoveragePerChangedFile = jaCoCoInteractor.getOverallCodeCoverageForChangedFiles(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertEquals(2, codeCoveragePerChangedFile.size());


        CodeCoverage firstFile = codeCoveragePerChangedFile.get("../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClass.java");
        Assertions.assertNotNull(firstFile);
        Assertions.assertEquals(13, firstFile.getInstructionsMissed());
        Assertions.assertEquals(30, firstFile.getInstructionsCovered());
        Assertions.assertEquals(3, firstFile.getLinesMissed());
        Assertions.assertEquals(7, firstFile.getLinesCovered());
        Assertions.assertEquals(CodeCoverage.CoverageType.CLASS, firstFile.getCoverageType());

        CodeCoverage secondFile = codeCoveragePerChangedFile.get("../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java");
        Assertions.assertNotNull(secondFile);

        Assertions.assertEquals(8, secondFile.getInstructionsMissed());
        Assertions.assertEquals(21, secondFile.getInstructionsCovered());
        Assertions.assertEquals(2, secondFile.getLinesMissed());
        Assertions.assertEquals(3, secondFile.getLinesCovered());
        Assertions.assertEquals(CodeCoverage.CoverageType.CLASS, secondFile.getCoverageType());
    }

    //single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass

    /**
     * This method returns a map with the changed lines for each file
     * Includes all the lines in the Jacoco overview
     * @return
     */
    private HashMap<String, int[]> getChangedLinesOverview(){
        HashMap<String, int[]> changedLinesOverview = new HashMap<>();

        changedLinesOverview.put("diff --git a/single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java b/single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java", new int[]{3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34});
        changedLinesOverview.put("diff --git a/single-module-example/src/test/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClassTest.java b/single-module-example/src/test/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClassTest.java",  new int[]{3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18});

        //should not be included in the result
        changedLinesOverview.put("diff --git a/core/pom.xml b/core/pom.xml", new int[]{-2, -2});

        return changedLinesOverview;
    }

    @Test
    public void testGetLinesFromPath(){
        int[] expectedLines = new int[]{3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};
        int[] linesForPath = getLinesForPath("single-module-example/src/test/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClassTest.java", getChangedLinesOverview());
        Assertions.assertArrayEquals(expectedLines, linesForPath);

        int[] expectedLines2 = new int[]{3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34};
        int[] linesForPath2 = getLinesForPath("single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java", getChangedLinesOverview());
        Assertions.assertArrayEquals(expectedLines2, linesForPath2);
    }

    /**
     * In this case the changed lines overview includes all the lines in the Jacoco overview so Jacoco = changed lines
     */
    @Test
    public void testCodeCoverageChangedFilesPerChangedLinesLineAllLines(){
        HashSet<File> sampleChangedFiles = getSampleChangedFiles();
        HashMap<String, int[]> changedLiensOverview = getChangedLinesOverview();


        FileUtil.Filters filters = new FileUtil.Filters();
        filters.addPathIncludeFilter("single-module-example/src/main/java");

        Set<File> result = new FileUtil().filterFiles(getSampleChangedFiles(), filters);

        HashMap<String, CodeCoverage> codeCoveragePerChangedFile;
        try {
            JaCoCoInteractor jaCoCoInteractor = new JaCoCoInteractor(singleModuleFile, "target/classes", new String[]{"src/main/java"}, new File("../single-module-example/"));
            codeCoveragePerChangedFile = jaCoCoInteractor.getCodeCoverageForChangedLinesOfChangedFiles(result, changedLiensOverview);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertEquals(2, codeCoveragePerChangedFile.size());


        CodeCoverage firstFile = codeCoveragePerChangedFile.get("../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClass.java");
        Assertions.assertNotNull(firstFile);
        Assertions.assertEquals(-1, firstFile.getInstructionsMissed());
        Assertions.assertEquals(-1, firstFile.getInstructionsCovered());
        Assertions.assertEquals(1, firstFile.getLinesMissed());
        Assertions.assertEquals(1, firstFile.getLinesCovered());
        Assertions.assertEquals(CodeCoverage.CoverageType.PER_CHANGED_LINE, firstFile.getCoverageType());

        CodeCoverage secondFile = codeCoveragePerChangedFile.get("../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java");
        Assertions.assertNotNull(secondFile);

        Assertions.assertEquals(-1, secondFile.getInstructionsMissed());
        Assertions.assertEquals(-1, secondFile.getInstructionsCovered());
        Assertions.assertEquals(3, secondFile.getLinesMissed());
        Assertions.assertEquals(2, secondFile.getLinesCovered());
        Assertions.assertEquals(CodeCoverage.CoverageType.PER_CHANGED_LINE, secondFile.getCoverageType());
    }

    /**
     *
     * @return a changed line overview, except that we removed some of the changed lines versus the previous example that included all the lines tracked by Jacoco and git
     */
    private HashMap<String, int[]> getChangedLinesOverviewNotAll(){
        HashMap<String, int[]> changedLinesOverview = new HashMap<>();

        changedLinesOverview.put("diff --git a/single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java b/single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java", new int[]{4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34});
        changedLinesOverview.put("diff --git a/single-module-example/src/test/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClassTest.java b/single-module-example/src/test/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClassTest.java",  new int[]{4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18});

        //should not be included in the result
        changedLinesOverview.put("diff --git a/core/pom.xml b/core/pom.xml", new int[]{-2, -2});

        return changedLinesOverview;
    }

    /**
     * Here we filtered some lines, just to verify that these are not part of the code coverage overview
     */
    @Test
    public void testCodeCoverageChangedFilesPerChangedLinesLineSelectedLines(){
        HashSet<File> sampleChangedFiles = getSampleChangedFiles();

        FileUtil.Filters filters = new FileUtil.Filters();
        filters.addPathIncludeFilter("single-module-example/src/main/java");

        Set<File> result = new FileUtil().filterFiles(getSampleChangedFiles(), filters);

        HashMap<String, CodeCoverage> codeCoveragePerChangedFile;
        try {
            JaCoCoInteractor jaCoCoInteractor = new JaCoCoInteractor(singleModuleFile, "target/classes", new String[]{"src/main/java"}, new File("../single-module-example/"));
            codeCoveragePerChangedFile = jaCoCoInteractor.getCodeCoverageForChangedLinesOfChangedFiles(result, getChangedLinesOverviewNotAll());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertEquals(2, codeCoveragePerChangedFile.size());


        CodeCoverage firstFile = codeCoveragePerChangedFile.get("../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClass.java");
        Assertions.assertNotNull(firstFile);
        Assertions.assertEquals(-1, firstFile.getInstructionsMissed());
        Assertions.assertEquals(-1, firstFile.getInstructionsCovered());
        Assertions.assertEquals(0, firstFile.getLinesMissed());
        Assertions.assertEquals(1, firstFile.getLinesCovered());
        Assertions.assertEquals(CodeCoverage.CoverageType.PER_CHANGED_LINE, firstFile.getCoverageType());

        CodeCoverage secondFile = codeCoveragePerChangedFile.get("../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java");
        Assertions.assertNotNull(secondFile);

        Assertions.assertEquals(-1, secondFile.getInstructionsMissed());
        Assertions.assertEquals(-1, secondFile.getInstructionsCovered());
        Assertions.assertEquals(2, secondFile.getLinesMissed());
        Assertions.assertEquals(1, secondFile.getLinesCovered());
        Assertions.assertEquals(CodeCoverage.CoverageType.PER_CHANGED_LINE, secondFile.getCoverageType());
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
