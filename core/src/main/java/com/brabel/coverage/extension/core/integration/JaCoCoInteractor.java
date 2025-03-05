package com.brabel.coverage.extension.core.integration;

import com.brabel.coverage.extension.core.model.CodeCoverage;
import org.jacoco.core.analysis.*;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utility class to interact with JaCoCo
 */
public class JaCoCoInteractor {

    private CoverageBuilder coverageBuilder;

    private CoverageBuilder getCoverageBuilder() {
        return coverageBuilder;
    }

    //TODO: will come from project.getCompileSourceRoots();
    String[] sourceCodePaths;

    /**
     * The paths to the source code directories. This is usually the 'src/main/java' directory
     * but can differ per project. In Maven, this will come from project.getCompileSourceRoots();
     * @return the paths to the source code directories
     */
    private String[] getSourceCodePaths() {
        return sourceCodePaths;
    }

    File baseDir;

    /**
     * The basedir is the base directory of the project
     * In Maven it should come frome project.getBasedir()
     * @return the base directory of the project
     */
    private File getBaseDir() {
        return baseDir;
    }

    /**
     * Constructor
     * @param jacocoExecFile the Jacoco.exec file
     * @param classPathDirectory the class path directory. This is the directory where the compiled classes are located. This is usually the 'target/classes' directory
     * @param sourceCodePaths the source code paths. This is the path to the source code directory. This is usually the 'src/main/java' directory but can differ per project
     *                        In Maven this comes from project.getCompileSourceRoots();
     * @param baseDir the base directory of the project. In maven this comes from project.getBasedir()
     * @throws IOException if the Jacoco.exec file cannot be read
     */
    public JaCoCoInteractor(File jacocoExecFile, File classPathDirectory, String[] sourceCodePaths, File baseDir) throws IOException {
        if(jacocoExecFile == null){
            throw new IllegalArgumentException("The Jacoco.exec file cannot be null.");
        }

        if(classPathDirectory == null){
            throw new IllegalArgumentException("The class path directory cannot be null.");
        }

        if(sourceCodePaths == null){
            throw new IllegalArgumentException("The source code paths cannot be null.");
        }

        if(baseDir == null){
            throw new IllegalArgumentException("The base directory cannot be null.");
        }


        this.coverageBuilder = new CoverageBuilder();
        this.sourceCodePaths = sourceCodePaths;
        this.baseDir = baseDir;

        //File classPathDir = new File(baseDir, classPathDirectory);

        analyzeClassesDirectory(jacocoExecFile, getCoverageBuilder(), classPathDirectory);
    }

    /**
     * Gets the total code coverage for the project

     * @return the total code coverage for the project
     * @throws IOException if the Jacoco.exec file cannot be read
     */
    public CodeCoverage getTotalCodeCoverage() throws IOException {
        return getTotalCoverageData(getCoverageBuilder());
    }

    /**
     * Gets the overall code coverage for the changed files.
     * This method is more detailed than the {@link #getTotalCodeCoverage()} method but less detailed than the {@link #getCodeCoverageForChangedLinesOfChangedFiles(Set, HashMap)} method.
     * This method can be used to target overall code coverage per changed file and not just for the actual code that was changed in these files (on a line by line basis).
     * @param changedFiles the changed files. This can be retrieved using the {@link GitInteractor#getOverviewOfChangedFiles(String)} method
     * @return a map containing the code coverage for each changed file. The key is the file path and the value is the code coverage object (see {@link CodeCoverage})
     */
    public HashMap<String, CodeCoverage> getOverallCodeCoverageForChangedFiles(Set<File> changedFiles){

        //create a map to store the code coverage per file
        HashMap<String, CodeCoverage> codeCoveragePerFile = new HashMap<>(changedFiles.size());

        // Collect coverage data
        //TODO: improve performance.
        for (String sourceRoot : getSourceCodePaths()) {
            File sourceDir = new File(getBaseDir(), sourceRoot);
            if (sourceDir.exists()) {
                for (IClassCoverage classCoverage : getCoverageBuilder().getClasses()) {

                    //if the class coverage of the jacoco report is found in the source files of the module
                    File sourceFile = new File(sourceDir, classCoverage.getName() + ".java");

                    if (sourceFile.exists()) {
                        //we create a code coverage object for the class file
                        ICounter lineCounter = classCoverage.getLineCounter();
                        ICounter instructionsCounter = classCoverage.getInstructionCounter();

                        int instructionsMissedCount = instructionsCounter.getMissedCount();
                        int instructionsCoveredCount = instructionsCounter.getCoveredCount();
                        int lineCoveredCount = lineCounter.getCoveredCount();
                        int lineMissedCount = lineCounter.getMissedCount();

                        String filePath = sourceFile.getPath();
                        CodeCoverage codeCoverageForFile = new CodeCoverage(filePath, CodeCoverage.CoverageType.CLASS, instructionsMissedCount, instructionsCoveredCount, lineMissedCount, lineCoveredCount);

                        codeCoveragePerFile.put(filePath, codeCoverageForFile);
                    }
                }
            }
        }

        return codeCoveragePerFile;

    }

    /**
     * Gets a code coverage overview based on the lines that were changed for the changed files
     * So if a file has 10 lines and 5 of those lines were changed, this method will return the coverage for those 5 lines.
     * The coverage will return a {@link CodeCoverage} object with the {@link CodeCoverage.CoverageType} set to {@link CodeCoverage.CoverageType#PER_CHANGED_LINE}
     * It is important (!) to notice that this method will set the instructionsMissed and instructionsCovered of {@link CodeCoverage} to -1. This method only focuses on the lines covered.
     * This code coverage overview is more detailed than the {@link #getOverallCodeCoverageForChangedFiles(Set)} method.
     * @param changedFiles the changed files. Thise can be retrieved using the {@link GitInteractor#getOverviewOfChangedFiles(String)} method
     * @param changedLinesOverview the changed lines overview. This can be retrieved using the {@link GitInteractor#getChangedLines(String)} method
     * @return a map containing the code coverage for each changed file. The key is the file path and the value is the code coverage object (see {@link CodeCoverage})
     */
    public HashMap<String, CodeCoverage> getCodeCoverageForChangedLinesOfChangedFiles(Set<File> changedFiles, HashMap<String, int[]> changedLinesOverview){
        HashMap<String, CodeCoverage> codeCoveragePerFile = new HashMap<>(changedFiles.size());

        for (String sourceRoot : getSourceCodePaths()) {
            File sourceDir = new File(getBaseDir(), sourceRoot);
            if (sourceDir.exists()) {
                for (IClassCoverage classCoverage : getCoverageBuilder().getClasses()) {

                    File sourceFile = new File(sourceDir, classCoverage.getName() + ".java");
                    //boolean found = classFilesOfModule.stream().anyMatch(filePath -> filePath.contains(classCoverage.getName()));

                    if (sourceFile.exists()) {
                        int[] changedLinesOfFile = getLinesForPath(classCoverage.getName(), changedLinesOverview);

                        int totalLinesThatAreCovered = 0;
                        int totalLinesThatAreNotCovered = 0;

                        for (int i = 0; i < changedLinesOfFile.length; i++) {
                            ILine line = classCoverage.getLine(changedLinesOfFile[i]);
                            int status = line.getStatus();

                            if ((status & ICounter.NOT_COVERED) != 0) {
                                totalLinesThatAreNotCovered++;
                            } else if ((status & ICounter.FULLY_COVERED) != 0) {
                                totalLinesThatAreCovered++;
                            } else if ((status & ICounter.PARTLY_COVERED) != 0) {
                                //TODO: for now we will treat this as not covered.
                                //      In the future this can probably come from a configuration by the end-user.
                                totalLinesThatAreNotCovered++;
                            }
                            //TODO: leaving commented for now, in the future, we might want to generate a nice overview
                            //      per class file. We could than use the status to generate a nice overview. Unknown means that this is
                            //      probably a line that is not code. Can be comment, whitespace etc.
                            /*else {
                                //skip
                                System.out.println("Line " + changedLinesOfFile[i]+ " has unknown coverage status.");
                            }*/
                        }

                        String filePath = sourceFile.getPath();
                        CodeCoverage codeCoverageForFile = new CodeCoverage(filePath, CodeCoverage.CoverageType.PER_CHANGED_LINE, -1, -1, totalLinesThatAreNotCovered, totalLinesThatAreCovered);

                        codeCoveragePerFile.put(filePath, codeCoverageForFile);

                    }
                }
            }
        }

        return codeCoveragePerFile;

    }

    /**
     * Utility method to get the integer array for a specific file from the changed lines overview
     * @param path the path of the file
     * @param changedLinesOverview the changed lines overview
     * @return the integer array for the file
     */
    protected static int[] getLinesForPath(String path, HashMap<String, int[]> changedLinesOverview) {
        for (Map.Entry<String, int[]> entry : changedLinesOverview.entrySet()) {
            String key = entry.getKey();
            if (key.contains(path)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Analyzes the classes directory based on the Jacoco.exec file and the coverage builder
     * @param jacocoExecFile the Jacoco.exec file
     * @param coverageBuilder the coverage builder
     * @param classPathDirectory the class path directory that needs to be analyzed. This is usually the 'target/classes' directory
     * @throws IOException if the classes directory cannot be analyzed
     */
    private static void analyzeClassesDirectory(File jacocoExecFile, CoverageBuilder coverageBuilder, File classPathDirectory) throws IOException {
        Analyzer analyzer = new Analyzer(getExecutionDataStore(jacocoExecFile), coverageBuilder);
        analyzer.analyzeAll(classPathDirectory);
    }

    /**
     * Gets the total coverage data for the project
     * @param coverageBuilder the coverage builder as generated by the JaCoCo analyzer
     * @return the total coverage data
     */
    private static CodeCoverage getTotalCoverageData(CoverageBuilder coverageBuilder){
        int totalInstructionsMissed = 0;
        int totalInstructionsCovered = 0;
        int totalLinesCovered = 0;
        int totalLinesMissed = 0;


        for (IClassCoverage classCoverage : coverageBuilder.getClasses()) {
            ICounter lineCounter = classCoverage.getLineCounter();
            ICounter instructionsCounter = classCoverage.getInstructionCounter();
            totalInstructionsMissed += instructionsCounter.getMissedCount();
            totalInstructionsCovered +=instructionsCounter.getCoveredCount();
            totalLinesCovered += lineCounter.getCoveredCount();
            totalLinesMissed += lineCounter.getMissedCount();
        }

        return new CodeCoverage(null,CodeCoverage.CoverageType.TOTAL, totalInstructionsMissed, totalInstructionsCovered, totalLinesMissed, totalLinesCovered);
    }


    /**
     * Gets the ExecutionDataStore based on the Jacoco.exec file
     * @param jacocoExecFile the Jacoco.exec file
     * @return the ExecutionDataStore
     */
    private static ExecutionDataStore getExecutionDataStore(File jacocoExecFile) throws IOException {
        FileInputStream execFile = new FileInputStream(jacocoExecFile);

        ExecutionDataStore executionData = new ExecutionDataStore();
        SessionInfoStore sessionInfo = new SessionInfoStore();
        ExecutionDataReader reader = new ExecutionDataReader(execFile);
        reader.setExecutionDataVisitor(executionData);
        reader.setSessionInfoVisitor(sessionInfo);
        reader.read();

        return executionData;
    }

}
