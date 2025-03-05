package tech.linebyline.coverage.extension.core.configuration;

import java.io.File;

/**
 * Manages the configuration of the coverage extension.
 */
public class ConfigurationManager {

    File projectBaseDir;

    /**
     * Sets the project base directory. In Maven this is the project.getBasedir()
     * @param projectBaseDir the project base directory
     */
    public void setProjectBaseDir(File projectBaseDir) {
        this.projectBaseDir = projectBaseDir;
    }

    /**
     * Returns the project base directory.
     * In Maven this is the project.getBasedir()
     * @return the project base directory
     */
    public File getProjectBaseDir() {
        return projectBaseDir;
    }

    File classPath;

    /**
     * Sets the class path. In Maven this is 'target/classes'
     * @param classPath the class path
     */
    public void setClassPath(File classPath) {
        this.classPath = classPath;
    }

    /**
     * Returns the class path.
     * In Maven this is 'target/classes'
     * @return the class path
     */
    public File getClassPath() {
        return classPath;
    }

    String[] sourcePaths;

    /**
     * Sets the source paths. In Maven this is usually 'src/main/java'
     * @param sourcePaths the source paths
     */
    public void setSourcePaths(String[] sourcePaths) {
        this.sourcePaths = sourcePaths;
    }

    /**
     * Returns the source paths.
     * In Maven this is 'src/main/java'
     * @return the source paths
     */
    public String[] getSourcePaths() {
        return sourcePaths;
    }

    File jacocoExecFile;

    /**
     * Sets the JaCoCo exec file. In Maven, this is usually 'target/jacoco.exec'
     * @param jacocoExecFile the JaCoCo exec file
     */
    public void setJacocoExecFile(File jacocoExecFile) {
        this.jacocoExecFile = jacocoExecFile;
    }

    /**
     * Returns the JaCoCo exec file.
     * This is usually 'target/jacoco.exec'
     * @return the JaCoCo exec file
     */
    public File getJacocoExecFile() {
        return jacocoExecFile;
    }

    String branchToCompare;

    /**
     * Sets the branch to compare the current branch to
     * @param branchToCompare the branch to compare the current branch to
     */
    public void setBranchToCompare(String branchToCompare) {
        this.branchToCompare = branchToCompare;
    }

    /**
     * Returns the branch to compare the current branch to
     * @return the branch to compare the current branch to
     */
    public String getBranchToCompare() {
        return branchToCompare;
    }


}
