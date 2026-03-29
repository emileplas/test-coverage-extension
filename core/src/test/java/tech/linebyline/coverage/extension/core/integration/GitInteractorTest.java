package tech.linebyline.coverage.extension.core.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static tech.linebyline.coverage.extension.core.integration.GitInteractor.*;

public class GitInteractorTest {

    @Test
    public void getOverviewOfChangedFilesTest(){
        Set<File> overviewOfChangedFiles = getOverviewOfChangedFiles("origin/develop");
        Assertions.assertNotNull(overviewOfChangedFiles);
    }

    @Test
    public void isLineDiffGitLineTest(){
        String[] testLines = {
                "diff --git a/core/pom.xml b/core/pom.xml",
                "diff --git a/single-module-example/src/test/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClassTest.java b/single-module-example/src/test/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClassTest.java",

                "diff --git a/another/file.txt b/another/file.txt",

        };

        String[] assertFalseLines = {
                "some other text",
                "if(line.contains(\"diff --git\"))"
        };

        for(String line : testLines){
            Assertions.assertTrue(isLineDiffGitLine(line));
        }

        for(String line : assertFalseLines){
            Assertions.assertFalse(isLineDiffGitLine(line));
        }
    }

    @Test
    public void getOverviewOfChangedLinesTest(){
        try {
            getChangedLines("origin/develop");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void parseChangedLinesHunkWithTrailingContextTest() {
        // Real-world hunk header: @@ -10,5 +10,7 @@ public class Foo
        // This was the bug in #56 — endsWith("@@") would not match this
        List<String> diff = Arrays.asList(
                "diff --git a/src/main/java/com/example/Foo.java b/src/main/java/com/example/Foo.java",
                "index abc1234..def5678 100644",
                "--- a/src/main/java/com/example/Foo.java",
                "+++ b/src/main/java/com/example/Foo.java",
                "@@ -10,5 +10,7 @@ public class Foo {",
                "     int x = 1;",
                "+    int y = 2;",
                "+    int z = 3;",
                "     return x;"
        );

        HashMap<String, int[]> result = parseChangedLines(diff);

        assertEquals(1, result.size(), "Should contain one file");
        assertTrue(result.containsKey("diff --git a/src/main/java/com/example/Foo.java b/src/main/java/com/example/Foo.java"));
        int[] changedLines = result.get("diff --git a/src/main/java/com/example/Foo.java b/src/main/java/com/example/Foo.java");
        assertEquals(2, changedLines.length, "Should have 2 changed lines");
        assertArrayEquals(new int[]{11, 12}, changedLines, "Lines should be absolute line numbers from hunk header");
    }

    @Test
    public void parseChangedLinesHunkWithoutTrailingContextTest() {
        // Hunk header ending with @@ (no trailing context)
        List<String> diff = Arrays.asList(
                "diff --git a/src/main/java/com/example/Bar.java b/src/main/java/com/example/Bar.java",
                "--- a/src/main/java/com/example/Bar.java",
                "+++ b/src/main/java/com/example/Bar.java",
                "@@ -1,3 +1,4 @@",
                " public class Bar {",
                "     int x = 1;",
                "+    int y = 2;",
                " }"
        );

        HashMap<String, int[]> result = parseChangedLines(diff);

        assertEquals(1, result.size());
        int[] changedLines = result.values().iterator().next();
        assertEquals(1, changedLines.length, "Should have 1 changed line");
        assertArrayEquals(new int[]{3}, changedLines, "Line should be absolute line number");
    }

    @Test
    public void parseChangedLinesMultipleFilesTest() {
        List<String> diff = Arrays.asList(
                "diff --git a/src/main/java/com/example/First.java b/src/main/java/com/example/First.java",
                "--- a/src/main/java/com/example/First.java",
                "+++ b/src/main/java/com/example/First.java",
                "@@ -5,3 +5,4 @@ public class First {",
                "     int a = 1;",
                "+    int b = 2;",
                "     return a;",
                "diff --git a/src/main/java/com/example/Second.java b/src/main/java/com/example/Second.java",
                "--- a/src/main/java/com/example/Second.java",
                "+++ b/src/main/java/com/example/Second.java",
                "@@ -10,2 +10,3 @@ public class Second {",
                "     String s = \"hello\";",
                "+    String t = \"world\";",
                "+    String u = \"!\";",
                "     return s;"
        );

        HashMap<String, int[]> result = parseChangedLines(diff);

        assertEquals(2, result.size(), "Should contain two files");

        int[] firstLines = result.get("diff --git a/src/main/java/com/example/First.java b/src/main/java/com/example/First.java");
        assertArrayEquals(new int[]{6}, firstLines, "First file: +5 start, added line at 6");

        int[] secondLines = result.get("diff --git a/src/main/java/com/example/Second.java b/src/main/java/com/example/Second.java");
        assertArrayEquals(new int[]{11, 12}, secondLines, "Second file: +10 start, added lines at 11 and 12");
    }

    @Test
    public void parseChangedLinesLastFileNotDroppedTest() {
        // This was the bug in #58 — the last file in a diff was never saved
        List<String> diff = Arrays.asList(
                "diff --git a/src/main/java/com/example/Only.java b/src/main/java/com/example/Only.java",
                "--- a/src/main/java/com/example/Only.java",
                "+++ b/src/main/java/com/example/Only.java",
                "@@ -1,4 +1,5 @@ public class Only {",
                "     int x = 1;",
                "+    int y = 2;",
                "     return x;"
        );

        HashMap<String, int[]> result = parseChangedLines(diff);

        assertEquals(1, result.size(), "Single file should not be dropped");
        int[] changedLines = result.values().iterator().next();
        assertEquals(1, changedLines.length);
        assertArrayEquals(new int[]{2}, changedLines);
    }

    @Test
    public void parseChangedLinesMultipleHunksInOneFileTest() {
        List<String> diff = Arrays.asList(
                "diff --git a/src/main/java/com/example/Multi.java b/src/main/java/com/example/Multi.java",
                "--- a/src/main/java/com/example/Multi.java",
                "+++ b/src/main/java/com/example/Multi.java",
                "@@ -5,3 +5,4 @@ public class Multi {",
                "     int a = 1;",
                "+    int b = 2;",
                "     return a;",
                "@@ -20,3 +21,4 @@ public class Multi {",
                "     String s = \"x\";",
                "+    String t = \"y\";",
                "     return s;"
        );

        HashMap<String, int[]> result = parseChangedLines(diff);

        assertEquals(1, result.size(), "Should be one file with two hunks");
        int[] changedLines = result.values().iterator().next();
        assertEquals(2, changedLines.length, "Should have 2 changed lines across both hunks");
        assertArrayEquals(new int[]{6, 22}, changedLines, "Each hunk should use its own absolute start line");
    }

    @Test
    public void parseChangedLinesDeletedLinesDontShiftNumbersTest() {
        // Deleted lines (- prefix) only exist in the old file and should not increment the line counter
        List<String> diff = Arrays.asList(
                "diff --git a/src/main/java/com/example/Del.java b/src/main/java/com/example/Del.java",
                "--- a/src/main/java/com/example/Del.java",
                "+++ b/src/main/java/com/example/Del.java",
                "@@ -5,6 +5,5 @@ public class Del {",
                "     int a = 1;",
                "-    int old = 0;",
                "-    int stale = 0;",
                "+    int b = 2;",
                "     return a;"
        );

        HashMap<String, int[]> result = parseChangedLines(diff);

        assertEquals(1, result.size());
        int[] changedLines = result.values().iterator().next();
        // "int a = 1;" is line 5, deleted lines don't count, so "int b = 2;" is line 6
        assertArrayEquals(new int[]{6}, changedLines, "Deleted lines should not shift line numbers");
    }

    @Test
    public void parseHunkStartLineVariousFormatsTest() {
        assertEquals(15, parseHunkStartLine("@@ -10,5 +15,7 @@"));
        assertEquals(15, parseHunkStartLine("@@ -10,5 +15,7 @@ public class Foo {"));
        assertEquals(1, parseHunkStartLine("@@ -1,3 +1,4 @@"));
        assertEquals(100, parseHunkStartLine("@@ -90 +100 @@"));
    }

    @Test
    public void parseChangedLinesEmptyDiffTest() {
        List<String> diff = List.of();

        HashMap<String, int[]> result = parseChangedLines(diff);

        assertTrue(result.isEmpty(), "Empty diff should produce empty result");
    }
}
