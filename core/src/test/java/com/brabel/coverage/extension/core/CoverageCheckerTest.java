package com.brabel.coverage.extension.core;

import com.brabel.coverage.extension.core.configuration.ConfigurationManager;
import com.brabel.coverage.extension.core.model.Rule;
import com.brabel.coverage.extension.core.model.RuleValidationResult;
import com.brabel.coverage.extension.core.services.RuleManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class CoverageCheckerTest {

    private ConfigurationManager getConfigurationManager(){
        ConfigurationManager configurationManager = new ConfigurationManager();
        configurationManager.setBranchToCompare("develop");
        configurationManager.setJacocoExecFile(new File("src/test/resources/jacoco-examples-exec/single-module-example-jacoco-output.exec"));
        configurationManager.setProjectBaseDir(new File("../single-module-example/"));
        configurationManager.setClassPath("target/classes");
        configurationManager.setSourcePaths(new String[]{"src/main/java"});
        return configurationManager;
    }

    @Test
    public void testCoverageCheckerOverallCodeCoverageRuleValidationFail() throws IOException, InterruptedException {
        Rule overallCodeCoverageRule = new Rule(Rule.RuleType.OVERALL, 80);
        RuleManager ruleManager = new RuleManager();
        ruleManager.addRule(overallCodeCoverageRule);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, getConfigurationManager());
        HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

        Assertions.assertEquals(1, ruleRuleValidationResultHashMap.size());

        RuleValidationResult ruleValidationResult = ruleRuleValidationResultHashMap.get(overallCodeCoverageRule);
        Assertions.assertFalse(ruleValidationResult.isSuccessful());
        Assertions.assertEquals("The overall coverage is below the required percentage. Required: 80.0% Actual: 66,67%", ruleValidationResult.getMessage());
    }

    @Test
    public void testCoverageCheckerOverallCodeCoverageRuleValidationSuccess() throws IOException, InterruptedException {
        Rule overallCodeCoverageRule = new Rule(Rule.RuleType.OVERALL, 66);
        RuleManager ruleManager = new RuleManager();
        ruleManager.addRule(overallCodeCoverageRule);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, getConfigurationManager());
        HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

        Assertions.assertEquals(1, ruleRuleValidationResultHashMap.size());

        RuleValidationResult ruleValidationResult = ruleRuleValidationResultHashMap.get(overallCodeCoverageRule);
        Assertions.assertTrue(ruleValidationResult.isSuccessful());
        Assertions.assertEquals("The overall coverage is above the required percentage. Required: 66.0% Actual: 66,67%", ruleValidationResult.getMessage());
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
    public void testCoverageCheckerClassCodeCoverageRuleFail() throws IOException, InterruptedException {
        Rule classCodeCoverageRule = new Rule(Rule.RuleType.PER_CLASS, 80);
        RuleManager ruleManager = new RuleManager();
        ruleManager.addRule(classCodeCoverageRule);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, getConfigurationManager());
        coverageChecker.setChangedFiles(getSampleChangedFiles());

        HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

        Assertions.assertEquals(1, ruleRuleValidationResultHashMap.size());

        String expectedMessage = "The changed classes do not meet the overall required coverage of 80.0%: \n" +
                "The following classes are not sufficiently covered: \n" +
                "../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClass.java with an overall coverage of 70.0%\n" +
                "../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java with an overall coverage of 60.0%\n" +
                "The following classes are sufficiently covered: \n" +
                "None";

        RuleValidationResult ruleValidationResult = ruleRuleValidationResultHashMap.get(classCodeCoverageRule);
        Assertions.assertFalse(ruleValidationResult.isSuccessful());
        Assertions.assertEquals(expectedMessage, ruleValidationResult.getMessage());
    }

    @Test
    public void testCoverageCheckerClassCodeCoverageRulePartlySuccess() throws IOException, InterruptedException {
        Rule classCodeCoverageRule = new Rule(Rule.RuleType.PER_CLASS, 65);
        RuleManager ruleManager = new RuleManager();
        ruleManager.addRule(classCodeCoverageRule);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, getConfigurationManager());
        coverageChecker.setChangedFiles(getSampleChangedFiles());

        HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

        Assertions.assertEquals(1, ruleRuleValidationResultHashMap.size());

        String expectedMessage = "The changed classes do not meet the overall required coverage of 65.0%: \n" +
                "The following classes are not sufficiently covered: \n" +
                "../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java with an overall coverage of 60.0%\n" +
                "The following classes are sufficiently covered: \n" +
                "../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClass.java with an overall coverage of 70.0%\n";

        RuleValidationResult ruleValidationResult = ruleRuleValidationResultHashMap.get(classCodeCoverageRule);
        Assertions.assertFalse(ruleValidationResult.isSuccessful());
        Assertions.assertEquals(expectedMessage, ruleValidationResult.getMessage());
    }
}
