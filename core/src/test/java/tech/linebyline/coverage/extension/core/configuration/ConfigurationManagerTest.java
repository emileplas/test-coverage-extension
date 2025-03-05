package tech.linebyline.coverage.extension.core.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

public class ConfigurationManagerTest {

    @Test
    public void setGetProjectBaseDir() {
        ConfigurationManager configurationManager = new ConfigurationManager();
        File projectBaseDir = new File("single-module");
        configurationManager.setProjectBaseDir(projectBaseDir);
        Assertions.assertEquals(projectBaseDir, configurationManager.getProjectBaseDir());
    }

    @Test
    public void setGetClassPath() {
        ConfigurationManager configurationManager = new ConfigurationManager();
        File classPathDir = new File("target/classes");
        configurationManager.setClassPath(classPathDir);
        Assertions.assertEquals(classPathDir, configurationManager.getClassPath());
    }

    @Test
    public void setGetSourcePaths() {
        ConfigurationManager configurationManager = new ConfigurationManager();
        String[] sourcePaths = {"src/main/java", "src/gen/java"};
        configurationManager.setSourcePaths(sourcePaths);
        Assertions.assertArrayEquals(sourcePaths, configurationManager.getSourcePaths());
    }

    @Test
    public void setJacocoExecFile() {
        ConfigurationManager configurationManager = new ConfigurationManager();
        File jacocoExecFile = new File("target/jacoco.exec");
        configurationManager.setJacocoExecFile(jacocoExecFile);
        Assertions.assertEquals(jacocoExecFile, configurationManager.getJacocoExecFile());
    }

    @Test
    public void setBranchToCompare() {
        ConfigurationManager configurationManager = new ConfigurationManager();
        String branchToCompare = "origin/develop";
        configurationManager.setBranchToCompare(branchToCompare);
        Assertions.assertEquals(branchToCompare, configurationManager.getBranchToCompare());
    }
}
