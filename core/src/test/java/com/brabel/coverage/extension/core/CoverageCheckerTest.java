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

//FIXME: it seems that when we update the project, the tests fails. We need copy the new jacoco.exec to resources and use that one
//      We should find a better way of mocking this.
public class CoverageCheckerTest {

    private ConfigurationManager getConfigurationManager(){
        ConfigurationManager configurationManager = new ConfigurationManager();
        configurationManager.setBranchToCompare("develop");
        configurationManager.setJacocoExecFile(new File("src/test/resources/jacoco-examples-exec/jacoco.exec"));
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
        Assertions.assertEquals("The overall coverage is below the required percentage. Required: 80.00% Actual: 66.67%", ruleValidationResult.getMessage());
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
        Assertions.assertEquals("The overall coverage is above the required percentage. Required: 66.00% Actual: 66.67%", ruleValidationResult.getMessage());
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

        String expectedMessage = "The changed classes do not meet the overall required coverage of 80.00%: \n" +
                "The following classes are not sufficiently covered: \n" +
                "../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClass.java with an overall coverage of 70.00%\n" +
                "../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java with an overall coverage of 60.00%\n" +
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

        String expectedMessage = "The changed classes do not meet the overall required coverage of 65.00%: \n" +
                "The following classes are not sufficiently covered: \n" +
                "../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java with an overall coverage of 60.00%\n" +
                "The following classes are sufficiently covered: \n" +
                "../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClass.java with an overall coverage of 70.00%\n";

        RuleValidationResult ruleValidationResult = ruleRuleValidationResultHashMap.get(classCodeCoverageRule);
        Assertions.assertFalse(ruleValidationResult.isSuccessful());
        Assertions.assertEquals(expectedMessage, ruleValidationResult.getMessage());
    }

    @Test
    public void testCoverageCheckerClassCodeCoverageRuleSuccess() throws IOException, InterruptedException {
        Rule classCodeCoverageRule = new Rule(Rule.RuleType.PER_CLASS, 50);
        RuleManager ruleManager = new RuleManager();
        ruleManager.addRule(classCodeCoverageRule);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, getConfigurationManager());
        coverageChecker.setChangedFiles(getSampleChangedFiles());

        HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

        Assertions.assertEquals(1, ruleRuleValidationResultHashMap.size());

        String expectedMessage = "All changed classes meet the required overall test coverage of 50.00% per class";

        RuleValidationResult ruleValidationResult = ruleRuleValidationResultHashMap.get(classCodeCoverageRule);
        Assertions.assertTrue(ruleValidationResult.isSuccessful());
        Assertions.assertEquals(expectedMessage, ruleValidationResult.getMessage());
    }

    private HashMap<String, int[]> getChangedLinesOverview(){
        HashMap<String, int[]> changedLinesOverview = new HashMap<>();

        changedLinesOverview.put("diff --git a/single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java b/single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java", new int[]{3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34});
        changedLinesOverview.put("diff --git a/single-module-example/src/test/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClassTest.java b/single-module-example/src/test/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClassTest.java",  new int[]{3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18});

        //should not be included in the result
        changedLinesOverview.put("diff --git a/core/pom.xml b/core/pom.xml", new int[]{-2, -2});

        return changedLinesOverview;
    }

    @Test
    public void testChangedLinesPerClassFail() throws IOException, InterruptedException {
        RuleManager ruleManager = new RuleManager();
        Rule classChangedLineRule = new Rule(Rule.RuleType.PER_CLASS_CHANGED_LINES, 80);
        ruleManager.addRule(classChangedLineRule);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, getConfigurationManager());
        coverageChecker.setChangedFiles(getSampleChangedFiles());
        coverageChecker.setChangedLines(getChangedLinesOverview());

        HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

        Assertions.assertEquals(1, ruleRuleValidationResultHashMap.size());

        String expectedMessage = "The changed lines do not meet the required coverage of 80.00% per class: \n" +
                "The following classes were changed but those changes are not sufficently covered: \n" +
                "../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClass.java with a coverage of for the changed lines of 50.00%\n" +
                "../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java with a coverage of for the changed lines of 40.00%\n" +
                "The following classes are sufficently covered: \n" +
                "None";

        RuleValidationResult ruleValidationResult = ruleRuleValidationResultHashMap.get(classChangedLineRule);
        Assertions.assertFalse(ruleValidationResult.isSuccessful());
        Assertions.assertEquals(expectedMessage, ruleValidationResult.getMessage());

    }


    @Test
    public void testChangedLinesPerClassPartlySuccess() throws IOException, InterruptedException {
        RuleManager ruleManager = new RuleManager();
        Rule classChangedLineRule = new Rule(Rule.RuleType.PER_CLASS_CHANGED_LINES, 45);
        ruleManager.addRule(classChangedLineRule);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, getConfigurationManager());
        coverageChecker.setChangedFiles(getSampleChangedFiles());
        coverageChecker.setChangedLines(getChangedLinesOverview());

        HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

        Assertions.assertEquals(1, ruleRuleValidationResultHashMap.size());

        String expectedMessage = "The changed lines do not meet the required coverage of 45.00% per class: \n" +
                "The following classes were changed but those changes are not sufficently covered: \n" +
                "../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java with a coverage of for the changed lines of 40.00%\n" +
                "The following classes are sufficently covered: \n" +
                "../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClass.java with a coverage of the changed lines of 50.00%\n";

        RuleValidationResult ruleValidationResult = ruleRuleValidationResultHashMap.get(classChangedLineRule);
        Assertions.assertFalse(ruleValidationResult.isSuccessful());
        Assertions.assertEquals(expectedMessage, ruleValidationResult.getMessage());

    }

    @Test
    public void testChangedLinesPerClassSuccess() throws IOException, InterruptedException {
        RuleManager ruleManager = new RuleManager();
        Rule classChangedLineRule = new Rule(Rule.RuleType.PER_CLASS_CHANGED_LINES, 5);
        ruleManager.addRule(classChangedLineRule);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, getConfigurationManager());
        coverageChecker.setChangedFiles(getSampleChangedFiles());
        coverageChecker.setChangedLines(getChangedLinesOverview());

        HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

        Assertions.assertEquals(1, ruleRuleValidationResultHashMap.size());

        String expectedMessage = "All the changed lines meet the required coverage of 5.00% per class.";

        RuleValidationResult ruleValidationResult = ruleRuleValidationResultHashMap.get(classChangedLineRule);
        Assertions.assertTrue(ruleValidationResult.isSuccessful());
        Assertions.assertEquals(expectedMessage, ruleValidationResult.getMessage());

    }

    @Test
    public void testChangedLinesOverallFail() throws IOException, InterruptedException {
        RuleManager ruleManager = new RuleManager();
        Rule classChangedLineRule = new Rule(Rule.RuleType.TOTAL_CHANGED_LINES, 80);
        ruleManager.addRule(classChangedLineRule);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, getConfigurationManager());
        coverageChecker.setChangedFiles(getSampleChangedFiles());
        coverageChecker.setChangedLines(getChangedLinesOverview());

        HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

        Assertions.assertEquals(1, ruleRuleValidationResultHashMap.size());

        String expectedMessage = "The overall coverage of the changed lines is below the required percentage. Required: 80.00% Actual: 42.86%";

        RuleValidationResult ruleValidationResult = ruleRuleValidationResultHashMap.get(classChangedLineRule);
        Assertions.assertFalse(ruleValidationResult.isSuccessful());
        Assertions.assertEquals(expectedMessage, ruleValidationResult.getMessage());

    }


    @Test
    public void testChangedLinesOverallSuccess() throws IOException, InterruptedException {
        RuleManager ruleManager = new RuleManager();
        Rule classChangedLineRule = new Rule(Rule.RuleType.TOTAL_CHANGED_LINES, 40);
        ruleManager.addRule(classChangedLineRule);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, getConfigurationManager());
        coverageChecker.setChangedFiles(getSampleChangedFiles());
        coverageChecker.setChangedLines(getChangedLinesOverview());

        HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

        Assertions.assertEquals(1, ruleRuleValidationResultHashMap.size());

        String expectedMessage = "The overall coverage of the changed lines is above the required percentage. Required: 40.00% Actual: 42.86%";

        RuleValidationResult ruleValidationResult = ruleRuleValidationResultHashMap.get(classChangedLineRule);
        Assertions.assertTrue(ruleValidationResult.isSuccessful());
        Assertions.assertEquals(expectedMessage, ruleValidationResult.getMessage());

    }


}
