
package com.brabel;

import com.brabel.coverage.extension.core.CoverageChecker;
import com.brabel.coverage.extension.core.configuration.ConfigurationManager;
import com.brabel.coverage.extension.core.model.CodeCoverage;
import com.brabel.coverage.extension.core.model.Rule;
import com.brabel.coverage.extension.core.model.RuleValidationResult;
import com.brabel.coverage.extension.core.services.RuleManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Mojo(name = "report", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class CoverageCheckMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File basedir;

    @Parameter(defaultValue = "${project.build.outputDirectory}")
    private File classpath;

    @Parameter
    private List<String> sourcepaths;

    @Parameter(defaultValue = "${project.build.directory}/jacoco.exec")
    private File jacocoExecFile;

    @Parameter(defaultValue = "develop")
    private String branchToCompare;

    @Parameter
    private List<Rule> rules;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Project Base Directory: " + basedir);
        getLog().info("Classpath: " + classpath);
        getLog().info("Source Paths: " + sourcepaths);
        getLog().info("JaCoCo Execution File: " + jacocoExecFile);
        getLog().info("Branch to Compare: " + branchToCompare);

        for (Rule rule : rules) {
            getLog().info("Rule Type: " + rule.getType() + ", Threshold: " + rule.getThreshold());
        }



        ConfigurationManager configurationManager = new ConfigurationManager();
        configurationManager.setClassPath(classpath);
        configurationManager.setSourcePaths(sourcepaths.toArray(new String[0]));
        configurationManager.setJacocoExecFile(jacocoExecFile);
        configurationManager.setBranchToCompare(branchToCompare);
        configurationManager.setProjectBaseDir(project.getBasedir());


        RuleManager ruleManager = new RuleManager();
        ruleManager.setRules(rules);

        CoverageChecker coverageChecker = new CoverageChecker(ruleManager, configurationManager);

        try {
            HashMap<Rule, RuleValidationResult> ruleRuleValidationResultHashMap = coverageChecker.runChecks();

            StringBuilder totalMessage = new StringBuilder();

            totalMessage.append("\n");

            boolean isFailed = false;


            for (Rule rule : ruleRuleValidationResultHashMap.keySet()) {
                RuleValidationResult ruleValidationResult = ruleRuleValidationResultHashMap.get(rule);
                if(!ruleValidationResult.isSuccessful()){
                    isFailed = true;

                }
                totalMessage.append(ruleValidationResult.getMessage() + "\n");
                totalMessage.append("--------------------------------------------------\n");
            }


            if(isFailed){
                throw new MojoFailureException(totalMessage.toString());
            }else{
                getLog().info(totalMessage.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}