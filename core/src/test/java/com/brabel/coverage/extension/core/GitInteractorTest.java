package com.brabel.coverage.extension.core;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;

import static com.brabel.coverage.extension.core.GitInteractor.getOverviewOfChangedFiles;

public class GitInteractorTest {

    @Test
    public void getOverviewOfChangedFilesTest(){
        Set<File> overviewOfChangedFiles = getOverviewOfChangedFiles("origin/develop");

    }
}
