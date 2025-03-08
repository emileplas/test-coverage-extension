package tech.linebyline.coverage.extension.core;

import tech.linebyline.coverage.extension.core.configuration.ConfigurationManager;
import tech.linebyline.coverage.extension.core.integration.GitInteractor;
import tech.linebyline.coverage.extension.core.integration.JaCoCoInteractor;
import tech.linebyline.coverage.extension.core.model.CodeCoverage;
import tech.linebyline.coverage.extension.core.model.Rule;
import tech.linebyline.coverage.extension.core.model.RuleValidationResult;
import tech.linebyline.coverage.extension.core.services.RuleManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static tech.linebyline.coverage.extension.core.integration.GitInteractor.getOverviewOfChangedFiles;

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
            if(rule.getType() == Rule.RuleType.OVERALL){
                RuleValidationResult ruleValidationResult = checkOverallCoverageRule(rule, jaCoCoInteractor);
                ruleValidationResults.put(rule, ruleValidationResult);
            } else if(rule.getType() == Rule.RuleType.PER_CLASS){
                RuleValidationResult ruleValidationResult = checkPerClassCoverageRule(rule, jaCoCoInteractor, getConfigurationManager().getBranchToCompare());
                ruleValidationResults.put(rule, ruleValidationResult);
            } else if(rule.getType() == Rule.RuleType.PER_CLASS_CHANGED_LINES){
                RuleValidationResult ruleValidationResult = checkPerClassChangedLinesCoverageRule(rule, jaCoCoInteractor, getConfigurationManager().getBranchToCompare());
                ruleValidationResults.put(rule, ruleValidationResult);
            } else if(rule.getType() == Rule.RuleType.TOTAL_CHANGED_LINES){
                RuleValidationResult ruleValidationResult = checkTotalChangedLinesCoverageRule(rule, jaCoCoInteractor, getConfigurationManager().getBranchToCompare());
                ruleValidationResults.put(rule, ruleValidationResult);
            }
        }

        return ruleValidationResults;
    }

    private HashMap<String, int[]> changedLines = null;

    private HashMap<String, int[]> getChangedLines(){
        return changedLines;
    }

    void setChangedLines(HashMap<String, int[]> changedLines){
        this.changedLines = changedLines;
    }

    private String doubleToString(double d){
        return String.format(Locale.US, "%.2f", d);
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

        if(getChangedFiles().size() == 0){
            return new RuleValidationResult(true, "No changed files found. No coverage to check.");
        }else if(getTotalCodeCoverageOfChangedLines().size() == 0){
            return new RuleValidationResult(true, "No changed lines found. No coverage to check.");
        }

        int totalLinesCovered = 0;
        int totalLinesMissed = 0;

        for(String className : getTotalCodeCoverageOfChangedLines().keySet()){
            CodeCoverage codeCoverage = getTotalCodeCoverageOfChangedLines().get(className);
            totalLinesCovered += codeCoverage.getLinesCovered();
            totalLinesMissed += codeCoverage.getLinesMissed();
        }

        double coverage;
        try{
            coverage = calculateCoverage(totalLinesCovered, totalLinesMissed);
        } catch (IllegalArgumentException e) {
            return new RuleValidationResult(!getConfigurationManager().getFailOnError(), "Unable to calculate the coverage of the changed lines: " + e.getMessage());
        }


        if(coverage < rule.getThreshold()) {
            return new RuleValidationResult(false, "The overall coverage of the changed lines is below the required percentage. Required: " +  doubleToString(rule.getThreshold()) + "% Actual: " + doubleToString(coverage) + "%");
        }else{
            return new RuleValidationResult(true, "The overall coverage of the changed lines is above the required percentage. Required: " + doubleToString(rule.getThreshold()) + "% Actual: " + doubleToString(coverage) + "%");
        }

    }

    /**
     * Calculates the coverage based on the lines covered and the lines missed.
     * @param linesCovered the number of lines that were covered by tests according to JaCoCo
     * @param linesMissed the number of lines that were not covered by tests according to JaCoCo
     * @return the coverage in percentage
     * @throws IllegalArgumentException if the lines covered or lines missed are negative
     */
    public static double calculateCoverage(int linesCovered, int linesMissed) throws IllegalArgumentException {
        if(linesCovered == 0 && linesMissed == 0){
            return 100;
        }else if(linesCovered < 0) {
            throw new IllegalArgumentException("Lines covered can not be negative.");
        }else if(linesMissed < 0){
            throw new IllegalArgumentException("Lines missed can not be negative.");
        }else {
            int totalLines = linesCovered + linesMissed;
            return (double) linesCovered / totalLines * 100;
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

        if(getChangedFiles().size() == 0) {
            return new RuleValidationResult(true, "No changed files found. No coverage to check.");
        }else if(getTotalCodeCoverageOfChangedLines().size() == 0){
            return new RuleValidationResult(true, "No changed lines found. No coverage to check.");
        }


        boolean success = true;

        LinkedHashMap<String, Double> sufficientCoverage = new LinkedHashMap<>();
        LinkedHashMap<String, Double> insufficientCoverage = new LinkedHashMap<>();
        LinkedHashMap<String, String> errorCoverage = new LinkedHashMap<>(); //map containing coverage calculations gone wrong

        for(String className : getTotalCodeCoverageOfChangedLines().keySet()){
            CodeCoverage codeCoverage = getTotalCodeCoverageOfChangedLines().get(className);

            Double coverage;

            try{
                coverage = calculateCoverage(codeCoverage.getLinesCovered(), codeCoverage.getLinesMissed());
            } catch (IllegalArgumentException e) {
                errorCoverage.put(className, "Unable to calculate the coverage of the changed lines: " + e.getMessage());
                continue;
            }

            if(coverage < rule.getThreshold()){
                success = false;
                insufficientCoverage.put(className, coverage);
            }else{
                sufficientCoverage.put(className, coverage);
            }
        }

        if(success) {
            StringBuilder message = new StringBuilder();
            message.append("All the changed lines meet the required coverage of " + doubleToString(rule.getThreshold()) + "% per class. \n");
            if(!errorCoverage.isEmpty()){
                message.append("The following classes had an error while calculating the coverage: \n");
                for(String className : errorCoverage.keySet()){
                    message.append(className + " with the following error: " + errorCoverage.get(className) + "\n");
                }
            }
            return new RuleValidationResult(isOutputSuccessfull(true, configurationManager.getFailOnError(), !errorCoverage.isEmpty()), message.toString());
        }else{
            StringBuilder message = new StringBuilder();
            message.append("The changed lines do not meet the required coverage of " + doubleToString(rule.getThreshold()) + "% per class: \n");
            message.append("The following classes were changed but those changes are not sufficiently covered: \n");
            for(String className : insufficientCoverage.keySet()){
                message.append(className + " with a coverage of the changed lines of " + doubleToString(insufficientCoverage.get(className)) + "%\n");
            }
            message.append("The following classes are sufficiently covered: \n");
            if(sufficientCoverage.isEmpty()) {
                message.append("None");
            }else{
                for(String className : sufficientCoverage.keySet()){
                    message.append(className + " with a coverage of the changed lines of " + doubleToString(sufficientCoverage.get(className)) + "%\n");
                }
            }
            if(!errorCoverage.isEmpty()){
                message.append("The following classes had an error while calculating the coverage: \n");
                for(String className : errorCoverage.keySet()){
                    message.append(className + " with the following error: " + errorCoverage.get(className) + "\n");
                }
            }
            return new RuleValidationResult(isOutputSuccessfull(false, configurationManager.getFailOnError(), !errorCoverage.isEmpty()), message.toString());
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

        if(getChangedFiles().size() == 0){
            return new RuleValidationResult(true, "No changed files found. No coverage to check.");
        }


        HashMap<String, CodeCoverage> overallClassCodeCoverage = jaCoCoInteractor.getOverallCodeCoverageForChangedFiles(getChangedFiles());

        boolean success = true;

        LinkedHashMap<String, Double> sufficientCoverage = new LinkedHashMap<>();
        LinkedHashMap<String, Double> insufficientCoverage = new LinkedHashMap<>();
        LinkedHashMap<String, String> errorCoverage = new LinkedHashMap<>(); //map containing coverage calculations gone wrong



        for(String className : overallClassCodeCoverage.keySet()){
            CodeCoverage codeCoverage = overallClassCodeCoverage.get(className);

            Double coverage;

            try{
                coverage = calculateCoverage(codeCoverage.getLinesCovered(), codeCoverage.getLinesMissed());
            } catch (IllegalArgumentException e) {
                errorCoverage.put(className, "Unable to calculate the coverage of the changed lines: " + e.getMessage());
                continue;
            }

            if(coverage < rule.getThreshold()){
                success = false;
                insufficientCoverage.put(className, coverage);
            }else{
                sufficientCoverage.put(className, coverage);
            }
        }

        if(success) {
            StringBuilder message = new StringBuilder();
            message.append("All the changed classes meet the required coverage of " + doubleToString(rule.getThreshold()) + "% per class. \n");


            if(!errorCoverage.isEmpty()){
                message.append("The following classes had an error while calculating the coverage: \n");
                for(String className : errorCoverage.keySet()){
                    message.append(className + " with the following error: " + errorCoverage.get(className) + "\n");
                }
            }

            return new RuleValidationResult(isOutputSuccessfull(true, configurationManager.getFailOnError(), !errorCoverage.isEmpty()), message.toString());
        }else{
            StringBuilder message = new StringBuilder();
            message.append("The changed classes do not meet the overall required coverage of " + doubleToString(rule.getThreshold()) + "%: \n");
            message.append("The following classes are not sufficiently covered: \n");
            for(String className : insufficientCoverage.keySet()){
                message.append(className + " with an overall coverage of " + doubleToString(insufficientCoverage.get(className)) + "%\n");
            }
            message.append("The following classes are sufficiently covered: \n");
            if(sufficientCoverage.isEmpty()){
                message.append("None");
            }else{
                for(String className : sufficientCoverage.keySet()){
                    message.append(className + " with an overall coverage of " + doubleToString(sufficientCoverage.get(className)) + "%\n");
                }
            }

            if(!errorCoverage.isEmpty()){
                message.append("The following classes had an error while calculating the coverage: \n");
                for(String className : errorCoverage.keySet()){
                    message.append(className + " with the following error: " + errorCoverage.get(className) + "\n");
                }
            }

            return new RuleValidationResult(isOutputSuccessfull(false, configurationManager.getFailOnError(), !errorCoverage.isEmpty()), message.toString());
        }
    }

    private boolean isOutputSuccessfull(boolean success, boolean failOnError, boolean hasErrors){
        if(success){
            if(failOnError && hasErrors) {
                return false;
            }else{
                return true;
            }
        }else{
            return false;
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

        Double coverage;

        try{
            coverage = calculateCoverage(totalCodeCoverage.getLinesCovered(), totalCodeCoverage.getLinesMissed());
        } catch (IllegalArgumentException e) {
            return new RuleValidationResult(!getConfigurationManager().getFailOnError(), "Unable to calculate the overall coverage because input was negative.");
        }

        if(coverage < rule.getThreshold()){
            return new RuleValidationResult(false, "The overall coverage is below the required percentage. Required: " + doubleToString(rule.getThreshold()) + "% Actual: " + doubleToString(coverage) + "%");
        }else{
            return new RuleValidationResult(true, "The overall coverage is above the required percentage. Required: " + doubleToString(rule.getThreshold()) + "% Actual: " + doubleToString(coverage) + "%");
        }
    }
}
