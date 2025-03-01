package com.brabel.coverage.extension.core.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static com.brabel.coverage.extension.core.integration.GitInteractor.*;

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


}
