package com.brabel.coverage.extension.core;

import com.brabel.coverage.extension.core.configuration.ConfigurationManager;
import com.brabel.coverage.extension.core.integration.GitInteractor;
import com.brabel.coverage.extension.core.integration.JaCoCoInteractor;
import com.brabel.coverage.extension.core.model.CodeCoverage;
import com.brabel.coverage.extension.core.model.Rule;
import com.brabel.coverage.extension.core.model.RuleValidationResult;
import com.brabel.coverage.extension.core.services.RuleManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static com.brabel.coverage.extension.core.integration.GitInteractor.getOverviewOfChangedFiles;

/**
 * Main class that will check the code coverage.
 */
public class CoverageChecker {

    private final RuleManager ruleManager;

    private RuleManager getRuleManager() {
        return ruleManager;
    }

    private final ConfigurationManager configurationManager;

    private ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    /**
     * Constructs a new CoverageChecker with the specified RuleManager and ConfigurationManager.
     * @param ruleManager the rule manager is responsible for wrapping the different rules that are created by the
     *                    end user and with which the code coverage will need to comply.
     * @param configurationManager the configuration manager holds the different configurations for the coverage extension.
     */
    public CoverageChecker(RuleManager ruleManager, ConfigurationManager configurationManager){
        this.ruleManager = ruleManager;
        this.configurationManager = configurationManager;
    }

    public HashMap<Rule, RuleValidationResult> runChecks() throws IOException, InterruptedException {
        List<Rule> rules = getRuleManager().getRules();

        JaCoCoInteractor jaCoCoInteractor = new JaCoCoInteractor(getConfigurationManager().getJacocoExecFile(), getConfigurationManager().getClassPath(),
                getConfigurationManager().getSourcePaths(), getConfigurationManager().getProjectBaseDir());

        HashMap<Rule, RuleValidationResult> ruleValidationResults = new HashMap<>();

        for (Rule rule : rules) {
            if(rule.getRuleType() == Rule.RuleType.OVERALL){
                RuleValidationResult ruleValidationResult = checkOverallCoverageRule(rule, jaCoCoInteractor);
                ruleValidationResults.put(rule, ruleValidationResult);
            } else if(rule.getRuleType() == Rule.RuleType.PER_CLASS){
                RuleValidationResult ruleValidationResult = checkPerClassCoverageRule(rule, jaCoCoInteractor, getConfigurationManager().getBranchToCompare());
                ruleValidationResults.put(rule, ruleValidationResult);
            } else if(rule.getRuleType() == Rule.RuleType.PER_CLASS_CHANGED_LINES){
                RuleValidationResult ruleValidationResult = checkPerClassChangedLinesCoverageRule(rule, jaCoCoInteractor, getConfigurationManager().getBranchToCompare());
                ruleValidationResults.put(rule, ruleValidationResult);
            } else if(rule.getRuleType() == Rule.RuleType.TOTAL_CHANGED_LINES){
                RuleValidationResult ruleValidationResult = checkTotalChangedLinesCoverageRule(rule, jaCoCoInteractor, getConfigurationManager().getBranchToCompare());
                ruleValidationResults.put(rule, ruleValidationResult);
            }
        }

        return ruleValidationResults;
    }

    HashMap<String, int[]> changedLines = null;

    private HashMap<String, int[]> getChangedLines(){
        return changedLines;
    }

    void setChangedLines(HashMap<String, int[]> changedLines){
        this.changedLines = changedLines;
    }

    /**
     * Checks whether the overall code coverage of the changed lines is sufficient, taking into account the minimum coverage
     * set in the rule.
     * <br></br>
     * E.g: the overall code coverage of the changed lines should be 70% or higher. This means that only the lines
     * that were changed in the branch compared to the target branch are taken into account.
     * If in our example, I have a class that has 5 lines covered and 5 lines not covered. Additionally, I have another class that has 10 lines out of 10 are covered.
     * Since both of the changed lines of the classes together are 15 out of 20, the overall coverage of the changed lines is 75%. Thus meeting the requirement.
     *
     * @param rule the rule that specifies the minimum coverage
     * @param jaCoCoInteractor the JaCoCoInteractor that will be used to get the code coverage
     * @param branchToCompare the branch to compare the current branch to
     * @return a RuleValidationResult that indicates whether the overall code coverage is sufficient and containing a message
     * @throws IOException if the JaCoCoInteractor fails to get the code coverage
     * @throws InterruptedException if the JaCoCoInteractor fails to get the code coverage
     */
    private RuleValidationResult checkTotalChangedLinesCoverageRule(Rule rule, JaCoCoInteractor jaCoCoInteractor, String branchToCompare) throws IOException, InterruptedException {
        if(getChangedFiles() == null){
            setChangedFiles(getOverviewOfChangedFiles(branchToCompare));
        }
        if(getChangedLines() == null){
            setChangedLines(GitInteractor.getChangedLines(branchToCompare));
        }
        if(getTotalCodeCoverageOfChangedLines() == null){
            setTotalCodeCoverageOfChangedLines(jaCoCoInteractor.getCodeCoverageForChangedLinesOfChangedFiles(getChangedFiles(), getChangedLines()));
        }

        boolean success = true;

        int totalLinesCovered = 0;
        int totalLinesMissed = 0;

        for(String className : getTotalCodeCoverageOfChangedLines().keySet()){
            CodeCoverage codeCoverage = getTotalCodeCoverageOfChangedLines().get(className);
            totalLinesCovered += codeCoverage.getLinesCovered();
            totalLinesMissed += codeCoverage.getLinesMissed();
        }

        int totalLines = totalLinesCovered + totalLinesMissed;

        double coverage = (double) totalLinesCovered / totalLines * 100;

        if(coverage < rule.getMinimumCoverage()) {
            return new RuleValidationResult(false, "The overall coverage of the changed lines is below the required percentage. Required: " + rule.getMinimumCoverage() + "% Actual: " + String.format("%.2f", coverage) + "%");
        }else{
            return new RuleValidationResult(true, "The overall coverage of the changed lines is above the required percentage. Required: " + rule.getMinimumCoverage() + " Actual: " + String.format("%.2f", coverage) + "%");
        }

    }

    private Set<File> changedFiles = null;

    private Set<File> getChangedFiles(){
        return changedFiles;
    }

    void setChangedFiles(Set<File> changedFiles){
        this.changedFiles = changedFiles;
    }

    HashMap<String, CodeCoverage> totalCodeCoverageOfChangedLines = null;

    private void setTotalCodeCoverageOfChangedLines(HashMap<String, CodeCoverage> totalCodeCoverageOfChangedLines){
        this.totalCodeCoverageOfChangedLines = totalCodeCoverageOfChangedLines;
    }

    private HashMap<String, CodeCoverage> getTotalCodeCoverageOfChangedLines(){
        return totalCodeCoverageOfChangedLines;
    }



    /**
     * Checks per class that was changed if the lines that were changed in that class meet the minimum coverage set in the rule.
     * <br></br>
     * E.g: if a class was changed and the rule specifies that each changed line should be covered for at least 80%, this rule will
     * check if that is the case. E.g: lets say if I have a class that has 5 lines covered and 5 lines not covered, and the requirement is 80% changed
     * lines per class. This class won't meet that requirement.
     * Now lets say if I have another class in addition to that class and the coverage is there 10/10 lines. This class will meet the requirement.
     * But because if the first class the overall coverage is below 80%, the rule will fail.
     * @param rule the rule that specifies the minimum coverage
     * @param jaCoCoInteractor the JaCoCoInteractor that will be used to get the code coverage
     * @param branchToCompare the branch to compare the current branch to
     * @return a RuleValidationResult that indicates whether the overall code coverage is sufficient and containing a message
     * @throws IOException if the JaCoCoInteractor fails to get the code coverage
     * @throws InterruptedException if the JaCoCoInteractor fails to get the code coverage
     */
    private RuleValidationResult checkPerClassChangedLinesCoverageRule(Rule rule, JaCoCoInteractor jaCoCoInteractor, String branchToCompare) throws IOException, InterruptedException {
        if(getChangedFiles() == null){
            setChangedFiles(getOverviewOfChangedFiles(branchToCompare));
        }
        if(getChangedLines() == null){
            setChangedLines(GitInteractor.getChangedLines(branchToCompare));
        }
        if(getTotalCodeCoverageOfChangedLines() == null){
            setTotalCodeCoverageOfChangedLines(jaCoCoInteractor.getCodeCoverageForChangedLinesOfChangedFiles(getChangedFiles(), getChangedLines()));
        }


        boolean success = true;

        LinkedHashMap<String, Double> sufficientCoverage = new LinkedHashMap<>();
        LinkedHashMap<String, Double> insufficientCoverage = new LinkedHashMap<>();

        for(String className : getTotalCodeCoverageOfChangedLines().keySet()){
            CodeCoverage codeCoverage = getTotalCodeCoverageOfChangedLines().get(className);
            int totalLines = codeCoverage.getLinesCovered() + codeCoverage.getLinesMissed();
            double coverage = (double) codeCoverage.getLinesCovered() / totalLines * 100;
            if(coverage < rule.getMinimumCoverage()){
                success = false;
                insufficientCoverage.put(className, coverage);
            }else{
                sufficientCoverage.put(className, coverage);
            }
        }

        if(success) {
            return new RuleValidationResult(true, "All the changed lines meet the required coverage of " + rule.getMinimumCoverage() + "% per class.");
        }else{
            StringBuilder message = new StringBuilder();
            message.append("The changed lines do not meet the required coverage of " + rule.getMinimumCoverage() + "% per class: \n");
            message.append("The following classes were changed but those changes are not sufficently covered: \n");
            for(String className : insufficientCoverage.keySet()){
                message.append(className + " with a coverage of for the changed lines of " + insufficientCoverage.get(className) + "%\n");
            }
            message.append("The following classes are sufficently covered: \n");
            if(sufficientCoverage.isEmpty()) {
                message.append("None");
            }else{
                for(String className : sufficientCoverage.keySet()){
                    message.append(className + " with a coverage of the changed lines of " + sufficientCoverage.get(className) + "%\n");
                }
            }
            return new RuleValidationResult(false, message.toString());
        }
    }

    /**
     * Checks whether each class that has been CHANGED meets the minimum coverage set in the rule. E.g: each class that was changed compared
     * to the branchToCompare should have been covered for at least 80%. This means that also lines that were not per se changed in the branch
     * compared to the target branch are taken into account. This is usefull if the code coverage of the project should be increased, independent
     * of the specific work that the developer has done.
     *
     * @param rule the rule that specifies the minimum coverage
     * @param jaCoCoInteractor the JaCoCoInteractor that will be used to get the code coverage
     * @param branchToCompare the branch to compare the current branch to
     * @return a RuleValidationResult that indicates whether the overall code coverage is sufficient and containing a message
     */
    private RuleValidationResult checkPerClassCoverageRule(Rule rule, JaCoCoInteractor jaCoCoInteractor, String branchToCompare) {
        if(getChangedFiles() == null){
            setChangedFiles(getOverviewOfChangedFiles(branchToCompare));
        }
        HashMap<String, CodeCoverage> overallClassCodeCoverage = jaCoCoInteractor.getOverallCodeCoverageForChangedFiles(getChangedFiles());

        boolean success = true;

        LinkedHashMap<String, Double> sufficientCoverage = new LinkedHashMap<>();
        LinkedHashMap<String, Double> insufficientCoverage = new LinkedHashMap<>();



        for(String className : overallClassCodeCoverage.keySet()){
            CodeCoverage codeCoverage = overallClassCodeCoverage.get(className);
            int totalLines = codeCoverage.getLinesCovered() + codeCoverage.getLinesMissed();
            double coverage = (double) codeCoverage.getLinesCovered() / totalLines * 100;
            if(coverage < rule.getMinimumCoverage()){
                success = false;
                insufficientCoverage.put(className, coverage);
            }else{
                sufficientCoverage.put(className, coverage);
            }
        }

        if(success) {
            return new RuleValidationResult(true, "All changed classes meet the required overall test coverage of " + rule.getMinimumCoverage() + "% per class");
        }else{
            StringBuilder message = new StringBuilder();
            message.append("The changed classes do not meet the overall required coverage of " + rule.getMinimumCoverage() + "%: \n");
            message.append("The following classes are not sufficiently covered: \n");
            for(String className : insufficientCoverage.keySet()){
                message.append(className + " with an overall coverage of " + insufficientCoverage.get(className) + "%\n");
            }
            message.append("The following classes are sufficiently covered: \n");
            if(sufficientCoverage.isEmpty()){
                message.append("None");
            }else{
                for(String className : sufficientCoverage.keySet()){
                    message.append(className + " with an overall coverage of " + sufficientCoverage.get(className) + "%\n");
                }
            }

            return new RuleValidationResult(false, message.toString());
        }
    }

    /**
     * Checks whether the overall code coverage of the project is sufficient, taking into account the minimum coverage
     * set in the rule. E.g: the overall code coverage should be 80%
     * @param rule the rule that specifies the minimum coverage
     * @param jaCoCoInteractor the JaCoCoInteractor that will be used to get the code coverage
     * @return a RuleValidationResult that indicates whether the overall code coverage is sufficient and containing a message
     * @throws IOException if the JaCoCoInteractor fails to get the code coverage
     */
    private RuleValidationResult checkOverallCoverageRule(Rule rule, JaCoCoInteractor jaCoCoInteractor) throws IOException {
        CodeCoverage totalCodeCoverage = jaCoCoInteractor.getTotalCodeCoverage();

        int totalLines = totalCodeCoverage.getLinesCovered() + totalCodeCoverage.getLinesMissed();

        double coverage = (double) totalCodeCoverage.getLinesCovered() / totalLines * 100;

        if(coverage < rule.getMinimumCoverage()){
            return new RuleValidationResult(false, "The overall coverage is below the required percentage. Required: " + rule.getMinimumCoverage() + "% Actual: " + String.format("%.2f", coverage) + "%");
        }else{
            return new RuleValidationResult(true, "The overall coverage is above the required percentage. Required: " + rule.getMinimumCoverage() + "% Actual: " + String.format("%.2f", coverage) + "%");
        }
    }
}
