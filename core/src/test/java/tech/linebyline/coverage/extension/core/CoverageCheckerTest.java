package tech.linebyline.coverage.extension.core;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tech.linebyline.coverage.extension.core.configuration.ConfigurationManager;
import tech.linebyline.coverage.extension.core.integration.GitInteractor;
import tech.linebyline.coverage.extension.core.integration.JaCoCoInteractor;
import tech.linebyline.coverage.extension.core.model.Rule;
import tech.linebyline.coverage.extension.core.model.RuleValidationResult;
import tech.linebyline.coverage.extension.core.services.RuleManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static tech.linebyline.coverage.extension.core.CoverageChecker.calculateCoverage;

//FIXME: it seems that when we update the project, the tests fails. We need copy the new jacoco.exec to resources and use that one
//      We should find a better way of mocking this.
public class CoverageCheckerTest {

    private ConfigurationManager getConfigurationManager(){
        ConfigurationManager configurationManager = new ConfigurationManager();
        configurationManager.setBranchToCompare("origin/develop");
        configurationManager.setJacocoExecFile(new File("src/test/resources/jacoco-examples-exec/jacoco.exec"));
        configurationManager.setProjectBaseDir(new File("../single-module-example/"));
        configurationManager.setClassPath(new File("../single-module-example/target/classes"));
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

        String expectedMessage = "All the changed classes meet the required coverage of 50.00% per class. \n";

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

    private HashMap<String, int[]> getChangedLinesOverviewErrors(){
        HashMap<String, int[]> changedLinesOverview = new HashMap<>();

        changedLinesOverview.put("diff --git a/single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java b/single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java", new int[]{-1, -1});
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
                "The following classes were changed but those changes are not sufficiently covered: \n" +
                "../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClass.java with a coverage of the changed lines of 50.00%\n" +
                "../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java with a coverage of the changed lines of 40.00%\n" +
                "The following classes are sufficiently covered: \n" +
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
                "The following classes were changed but those changes are not sufficiently covered: \n" +
                "../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java with a coverage of the changed lines of 40.00%\n" +
                "The following classes are sufficiently covered: \n" +
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

        String expectedMessage = "All the changed lines meet the required coverage of 5.00% per class. \n";

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

    @Test
    public void testChangedLinesOverallSuccessNoChangedFiles() throws IOException, InterruptedException {
        RuleManager ruleManager = new RuleManager();

        Rule classChangedLineRule = new Rule(Rule.RuleType.TOTAL_CHANGED_LINES, 40);
        ruleManager.addRule(classChangedLineRule);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, getConfigurationManager());
        coverageChecker.setChangedFiles(new HashSet<>());
        coverageChecker.setChangedLines(new HashMap<>());

        HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

        Assertions.assertEquals(1, ruleRuleValidationResultHashMap.size());

        String expectedMessage = "No changed files found. No coverage to check.";

        RuleValidationResult ruleValidationResult = ruleRuleValidationResultHashMap.get(classChangedLineRule);
        Assertions.assertTrue(ruleValidationResult.isSuccessful());
        Assertions.assertEquals(expectedMessage, ruleValidationResult.getMessage());

    }

    @Test
    public void testChangedLinesOverallSuccessNoChangedLines() throws IOException, InterruptedException {
        RuleManager ruleManager = new RuleManager();

        Rule classChangedLineRule = new Rule(Rule.RuleType.TOTAL_CHANGED_LINES, 40);
        ruleManager.addRule(classChangedLineRule);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, getConfigurationManager());
        coverageChecker.setChangedFiles(getSampleChangedFiles());
        coverageChecker.setChangedLines(new HashMap<>());

        HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

        Assertions.assertEquals(1, ruleRuleValidationResultHashMap.size());

        String expectedMessage = "No changed lines found. No coverage to check.";

        RuleValidationResult ruleValidationResult = ruleRuleValidationResultHashMap.get(classChangedLineRule);
        Assertions.assertTrue(ruleValidationResult.isSuccessful());
        Assertions.assertEquals(expectedMessage, ruleValidationResult.getMessage());

    }

    @Test
    public void testChangedLinesPerClassSuccessNoChangedFiles() throws IOException, InterruptedException {
        RuleManager ruleManager = new RuleManager();

        Rule classChangedLineRule = new Rule(Rule.RuleType.PER_CLASS_CHANGED_LINES, 40);
        ruleManager.addRule(classChangedLineRule);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, getConfigurationManager());
        coverageChecker.setChangedFiles(new HashSet<>());
        coverageChecker.setChangedLines(new HashMap<>());

        HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

        Assertions.assertEquals(1, ruleRuleValidationResultHashMap.size());

        String expectedMessage = "No changed files found. No coverage to check.";

        RuleValidationResult ruleValidationResult = ruleRuleValidationResultHashMap.get(classChangedLineRule);
        Assertions.assertTrue(ruleValidationResult.isSuccessful());
        Assertions.assertEquals(expectedMessage, ruleValidationResult.getMessage());

    }

    @Test
    public void testChangedLinesPerClassSuccessNoChangedLines() throws IOException, InterruptedException {
        RuleManager ruleManager = new RuleManager();

        Rule classChangedLineRule = new Rule(Rule.RuleType.PER_CLASS_CHANGED_LINES, 40);
        ruleManager.addRule(classChangedLineRule);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, getConfigurationManager());
        coverageChecker.setChangedFiles(getSampleChangedFiles());
        coverageChecker.setChangedLines(new HashMap<>());

        HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

        Assertions.assertEquals(1, ruleRuleValidationResultHashMap.size());

        String expectedMessage = "No changed lines found. No coverage to check.";

        RuleValidationResult ruleValidationResult = ruleRuleValidationResultHashMap.get(classChangedLineRule);
        Assertions.assertTrue(ruleValidationResult.isSuccessful());
        Assertions.assertEquals(expectedMessage, ruleValidationResult.getMessage());

    }

    @Test
    public void testPerClassSuccessNoChangedFiles() throws IOException, InterruptedException {
        RuleManager ruleManager = new RuleManager();

        Rule classChangedLineRule = new Rule(Rule.RuleType.PER_CLASS, 40);
        ruleManager.addRule(classChangedLineRule);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, getConfigurationManager());
        coverageChecker.setChangedFiles(new HashSet<>());
        coverageChecker.setChangedLines(new HashMap<>());

        HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

        Assertions.assertEquals(1, ruleRuleValidationResultHashMap.size());

        String expectedMessage = "No changed files found. No coverage to check.";

        RuleValidationResult ruleValidationResult = ruleRuleValidationResultHashMap.get(classChangedLineRule);
        Assertions.assertTrue(ruleValidationResult.isSuccessful());
        Assertions.assertEquals(expectedMessage, ruleValidationResult.getMessage());

    }

    @Test
    public void testCalculateCoverage(){
        double coverage = calculateCoverage(10, 10);
        Assertions.assertEquals(50.0, coverage);
    }

    @Test
    public void testCalculateCoverageZero(){
        double coverage = calculateCoverage(0, 10);
        Assertions.assertEquals(0.0, coverage);
    }

    @Test
    public void testCalculateCoverageZeroDivisor(){
        double coverage = calculateCoverage(10, 0);
        Assertions.assertEquals(100.0, coverage);
    }

    @Test
    public void testCalculateCoverageZeroBoth(){
        double coverage = calculateCoverage(0, 0);
        Assertions.assertEquals(100.0, coverage);
    }

    @Test
    public void testCalculateCoverageNegativeLinesCovered(){
        try{
            double coverage = calculateCoverage(-10, 10);
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof IllegalArgumentException);
            Assertions.assertEquals("Lines covered can not be negative.", e.getMessage());
        }
    }

    @Test
    public void testCalculateCoverageNegativeLinesMissed(){
        try{
            double coverage = calculateCoverage(10, -10);
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof IllegalArgumentException);
            Assertions.assertEquals("Lines missed can not be negative.", e.getMessage());
        }
    }

    @Test
    public void testCheckCoverageNotFailOnError(){

        RuleManager ruleManager = new RuleManager();

        Rule classChangedLineRule = new Rule(Rule.RuleType.PER_CLASS, 40);
        ruleManager.addRule(classChangedLineRule);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, getConfigurationManager());

        try (MockedStatic<CoverageChecker> mock = Mockito.mockStatic(CoverageChecker.class)) {
            mock.when(() -> CoverageChecker.calculateCoverage(anyInt(), anyInt()))
                    .thenThrow(new IllegalArgumentException("TEST EXCEPTION"));

            HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

            Assertions.assertTrue(ruleRuleValidationResultHashMap.values().stream()
                    .anyMatch(result -> {
                        return result.isSuccessful() &&
                                result.getMessage().contains("All the changed classes meet the required coverage of 40.00% per class.") &&
                                result.getMessage().contains("The following classes had an error while calculating the coverage: ") &&
                                result.getMessage().contains("../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClass.java with the following error: Unable to calculate the coverage of the changed lines: TEST EXCEPTION") &&
                                result.getMessage().contains("./single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java with the following error: Unable to calculate the coverage of the changed lines: TEST EXCEPTION");
                    }
                    ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCheckCoverageFailOnErrorTrue(){

        RuleManager ruleManager = new RuleManager();

        Rule classChangedLineRule = new Rule(Rule.RuleType.PER_CLASS, 40);
        ruleManager.addRule(classChangedLineRule);

        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.setFailOnError(true);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, configurationManager);

        try (MockedStatic<CoverageChecker> mock = Mockito.mockStatic(CoverageChecker.class)) {
            mock.when(() -> CoverageChecker.calculateCoverage(anyInt(), anyInt()))
                    .thenThrow(new IllegalArgumentException("TEST EXCEPTION"));

            HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

            Assertions.assertTrue(ruleRuleValidationResultHashMap.values().stream()
                    .anyMatch(result -> {
                                return !result.isSuccessful() &&
                                        result.getMessage().contains("All the changed classes meet the required coverage of 40.00% per class.") &&
                                        result.getMessage().contains("The following classes had an error while calculating the coverage:") &&
                                        result.getMessage().contains("../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClass.java with the following error: Unable to calculate the coverage of the changed lines: TEST EXCEPTION") &&
                                        result.getMessage().contains("../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java with the following error: Unable to calculate the coverage of the changed lines: TEST EXCEPTION");
                            }
                    ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCheckCoverageNotFailOnErrorOverall(){

        RuleManager ruleManager = new RuleManager();

        Rule classChangedLineRule = new Rule(Rule.RuleType.OVERALL, 40);
        ruleManager.addRule(classChangedLineRule);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, getConfigurationManager());

        try (MockedStatic<CoverageChecker> mock = Mockito.mockStatic(CoverageChecker.class)) {
            mock.when(() -> CoverageChecker.calculateCoverage(anyInt(), anyInt()))
                    .thenThrow(new IllegalArgumentException("Test exception"));

            HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

            Assertions.assertTrue(ruleRuleValidationResultHashMap.values().stream()
                    .anyMatch(result -> {
                                return result.isSuccessful() && result.getMessage().contains("Unable to calculate the overall coverage because input was negative.");
                            }
                    ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCheckCoverageFailOnErrorTrueOverall(){

        RuleManager ruleManager = new RuleManager();

        Rule classChangedLineRule = new Rule(Rule.RuleType.OVERALL, 40);
        ruleManager.addRule(classChangedLineRule);

        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.setFailOnError(true);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, configurationManager);

        try (MockedStatic<CoverageChecker> mock = Mockito.mockStatic(CoverageChecker.class)) {
            mock.when(() -> CoverageChecker.calculateCoverage(anyInt(), anyInt()))
                    .thenThrow(new IllegalArgumentException("Test exception"));

            HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

            Assertions.assertTrue(ruleRuleValidationResultHashMap.values().stream()
                    .anyMatch(result -> {
                                return !result.isSuccessful() && result.getMessage().contains("Unable to calculate the overall coverage because input was negative.");
                            }
                    ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCheckCoverageNotFailOnErrorPerClassChangedLines(){

        RuleManager ruleManager = new RuleManager();

        Rule classChangedLineRule = new Rule(Rule.RuleType.PER_CLASS_CHANGED_LINES, 40);
        ruleManager.addRule(classChangedLineRule);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, getConfigurationManager());
        coverageChecker.setChangedFiles(getSampleChangedFiles());
        coverageChecker.setChangedLines(getChangedLinesOverviewErrors());

        try (MockedStatic<CoverageChecker> mock = Mockito.mockStatic(CoverageChecker.class)) {
            mock.when(() -> CoverageChecker.calculateCoverage(anyInt(), anyInt()))
                    .thenThrow(new IllegalArgumentException("TEST EXCEPTION"));

            HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

            Assertions.assertTrue(ruleRuleValidationResultHashMap.values().stream()
                    .anyMatch(result -> {
                                return result.isSuccessful() &&
                                        result.getMessage().contains("All the changed lines meet the required coverage of 40.00% per class.") &&
                                        result.getMessage().contains("The following classes had an error while calculating the coverage:") &&
                                        result.getMessage().contains("../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClass.java with the following error: Unable to calculate the coverage of the changed lines: TEST EXCEPTION") &&
                                        result.getMessage().contains("../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java with the following error: Unable to calculate the coverage of the changed lines: TEST EXCEPTION");
                            }
                    ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCheckCoverageFailOnErrorTruePerClassChangedLines(){

        RuleManager ruleManager = new RuleManager();

        Rule classChangedLineRule = new Rule(Rule.RuleType.PER_CLASS_CHANGED_LINES, 40);
        ruleManager.addRule(classChangedLineRule);

        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.setFailOnError(true);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, configurationManager);
        coverageChecker.setChangedFiles(getSampleChangedFiles());
        coverageChecker.setChangedLines(getChangedLinesOverview());

        try (MockedStatic<CoverageChecker> mock = Mockito.mockStatic(CoverageChecker.class)) {
            mock.when(() -> CoverageChecker.calculateCoverage(anyInt(), anyInt()))
                    .thenThrow(new IllegalArgumentException("TEST EXCEPTION"));

            HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

            Assertions.assertTrue(ruleRuleValidationResultHashMap.values().stream()
                    .anyMatch(result -> {
                                //since fail on error is set to true the validation result will be false for isSuccessful()
                                return !result.isSuccessful() &&
                                        result.getMessage().contains("All the changed lines meet the required coverage of 40.00% per class. \n") &&
                                        result.getMessage().contains("The following classes had an error while calculating the coverage: \n") &&
                                        result.getMessage().contains("../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClass.java with the following error: Unable to calculate the coverage of the changed lines: TEST EXCEPTION") &&
                                        result.getMessage().contains("../single-module-example/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java with the following error: Unable to calculate the coverage of the changed lines: TEST EXCEPTION");
                            }
                    ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCheckCoverageNotFailOnErrorOverallChangedLines(){

        RuleManager ruleManager = new RuleManager();

        Rule classChangedLineRule = new Rule(Rule.RuleType.TOTAL_CHANGED_LINES, 40);
        ruleManager.addRule(classChangedLineRule);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, getConfigurationManager());
        coverageChecker.setChangedFiles(getSampleChangedFiles());
        coverageChecker.setChangedLines(getChangedLinesOverviewErrors());

        try (MockedStatic<CoverageChecker> mock = Mockito.mockStatic(CoverageChecker.class)) {
            mock.when(() -> CoverageChecker.calculateCoverage(anyInt(), anyInt()))
                    .thenThrow(new IllegalArgumentException("TEST EXCEPTION"));

            HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

            Assertions.assertTrue(ruleRuleValidationResultHashMap.values().stream()
                    .anyMatch(result -> {
                                return result.isSuccessful() && result.getMessage().contains("Unable to calculate the coverage of the changed lines: TEST EXCEPTION");
                            }
                    ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCheckCoverageFailOnErrorTrueOverallChangedLines(){

        RuleManager ruleManager = new RuleManager();

        Rule classChangedLineRule = new Rule(Rule.RuleType.TOTAL_CHANGED_LINES, 40);
        ruleManager.addRule(classChangedLineRule);

        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.setFailOnError(true);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, configurationManager);
        coverageChecker.setChangedFiles(getSampleChangedFiles());
        coverageChecker.setChangedLines(getChangedLinesOverview());

        try (MockedStatic<CoverageChecker> mock = Mockito.mockStatic(CoverageChecker.class)) {
            mock.when(() -> CoverageChecker.calculateCoverage(anyInt(), anyInt()))
                    .thenThrow(new IllegalArgumentException("TEST EXCEPTION"));

            HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

            Assertions.assertTrue(ruleRuleValidationResultHashMap.values().stream()
                    .anyMatch(result -> {
                                //since fail on error is set to true the validation result will be false for isSuccessful()
                                return !result.isSuccessful() && result.getMessage().contains("Unable to calculate the coverage of the changed lines: TEST EXCEPTION");
                            }
                    ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
