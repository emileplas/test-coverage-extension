package com.brabel.coverage.extension.core;

import org.jacoco.core.analysis.*;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class to interact with JaCoCo
 */
public class JaCoCoInteractor {

    private final File jacocoExecFile;


    private File getJacocoExecFile() {
        return jacocoExecFile;
    }

    private final File classPathDirectory;

    private File getClassPathDirectory() {
        return classPathDirectory;
    }

    private CoverageBuilder coverageBuilder;

    private CoverageBuilder getCoverageBuilder() {
        return coverageBuilder;
    }
    
    //TODO: tests that use the source directory will probably need a String parameter to pass the source directory. This can differ per project. We can use src/main/java as a default?
    //TODO: class path directory will probably also need to be set by the user. We can use target/classes as a default?

    /**
     * Constructor
     * @param jacocoExecFile the Jacoco.exec file
     * @param classPathDirectory the class path directory. This is the directory where the compiled classes are located. This is usually the 'target/classes' directory
     * @throws IOException if the Jacoco.exec file cannot be read
     */
    JaCoCoInteractor(File jacocoExecFile, File classPathDirectory) throws IOException {
        this.jacocoExecFile = jacocoExecFile;
        this.classPathDirectory = classPathDirectory;
        this.coverageBuilder = new CoverageBuilder();

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
     *
     * @param classPathDirectory the class path directory where the compiled classes are stored. This is usually the 'target/classes' directory
     * @return the path to the source code directory
     */
    private static String extractModuleSourceCodePathPath(File classPathDirectory) {
        Path path = classPathDirectory.toPath().normalize();

        String pathToString = path.toString();

        return pathToString.replace("../", "").replace("target/classes", "src/main/java"); // Return null if "target" is not found
    }

    //TODO: we will need to fix how it gets the source dir etc, since that will depend on the module settings as passed to maven

    /**
     * Gets the overall code coverage for the changed files.
     * This method is more granular than the {@link #getTotalCodeCoverage()} method but less granular than the {@link #getCodeCoverageForChangedLinesOfChangedFiles(File, File, Set, HashMap)} method.
     * This method can be used to target code coverage for changed files and not just for the actual code that was changed in these files.
     * @param changedFiles the changed files. This can be retrieved using the {@link GitInteractor#getOverviewOfChangedFiles(String)} method
     * @return a map containing the code coverage for each changed file. The key is the file path and the value is the code coverage object (see {@link CodeCoverage})
     * @throws IOException
     */
    public HashMap<String, CodeCoverage> getOverallCodeCoverageForChangedFiles(Set<File> changedFiles) throws IOException {
        try {

            //create a map to store the code coverage per file
            HashMap<String, CodeCoverage> codeCoveragePerFile = new HashMap<>(changedFiles.size());

            //we retrieve the path where the source code is stored, based on the class path directory where the compiled classes are stored
            String sourceCodePath = extractModuleSourceCodePathPath(getClassPathDirectory());

            if (sourceCodePath == null) {
                throw new IOException("Invalid class path directory; 'target' not found: " + getClassPathDirectory().getAbsolutePath());
            }

            //we retrieve the source code path of the module directory. Was this important for multi module projects?
            String moduleDir = sourceCodePath.split("src/main/java")[0] + "src/main/java";

            //we create a filter to only include the source files of the module
            FileUtil.Filters filters = new FileUtil.Filters();
            filters.addPathIncludeFilter(sourceCodePath);

            //we filter the changed files to only include the source files of the module
            Set<File> sourceFilesOfModule = new FileUtil().filterFiles(changedFiles, filters);

            //we create a set of strings that are the paths of the source files of the module without the .java extension
            Set<String> sourceFilesOfModulePathToStringWithoutJavaExtension = sourceFilesOfModule.stream()
                    .map(file -> file.getPath().replace(".java", ""))
                    .collect(Collectors.toSet());


            // Collect coverage data
            for (IClassCoverage classCoverage : getCoverageBuilder().getClasses()) {

                //if the class coverage of the jacoco report is found in the source files of the module
                boolean found = sourceFilesOfModulePathToStringWithoutJavaExtension.stream().anyMatch(filePath -> filePath.contains(classCoverage.getName()));

                if (found) {
                    //we create a code coverage object for the class file
                    ICounter lineCounter = classCoverage.getLineCounter();
                    ICounter instructionsCounter = classCoverage.getInstructionCounter();

                    int instructionsMissedCount = instructionsCounter.getMissedCount();
                    int instructionsCoveredCount = instructionsCounter.getCoveredCount();
                    int lineCoveredCount = lineCounter.getCoveredCount();
                    int lineMissedCount = lineCounter.getMissedCount();

                    // Adjust to get the full file path within the module
                    File sourceFile = new File(moduleDir + "/"+ classCoverage.getName() + ".java");
                    if (sourceFile != null) {
                        String filePath = sourceFile.getPath();
                        CodeCoverage codeCoverageForFile = new CodeCoverage(filePath, CodeCoverage.CoverageType.CLASS, instructionsMissedCount, instructionsCoveredCount, lineMissedCount, lineCoveredCount);

                        codeCoveragePerFile.put(filePath, codeCoverageForFile);
                    }
                }
            }

            return codeCoveragePerFile;
        } catch (IOException e) {
            throw new IOException("Unable to read " + getJacocoExecFile().getAbsolutePath() + ". Exception: " + e);
        }
    }

    /**
     * Gets a code coverage overview based on the lines that were changed for the changed files
     * So if a file has 10 lines and 5 of those lines were changed, this method will return the coverage for those 5 lines.
     * The coverage will return a {@link CodeCoverage} object with the {@link CodeCoverage.CoverageType} set to {@link CodeCoverage.CoverageType#PER_CHANGED_LINE}
     * It is important (!) to notice that this method will set the instructionsMissed and instructionsCovered of {@link CodeCoverage#} to -1. This method only focuses on the lines covered.
     * This code coverage overview is more granular than the {@link #getOverallCodeCoverageForChangedFiles(Set)} method.
     * @param changedFiles the changed files. Thise can be retrieved using the {@link GitInteractor#getOverviewOfChangedFiles(String)} method
     * @param changedLinesOverview the changed lines overview. This can be retrieved using the {@link GitInteractor#getChangedLines(String)} method
     * @return a map containing the code coverage for each changed file. The key is the file path and the value is the code coverage object (see {@link CodeCoverage})
     * @throws IOException if the Jacoco.exec file cannot be read
     */
    public HashMap<String, CodeCoverage> getCodeCoverageForChangedLinesOfChangedFiles(Set<File> changedFiles, HashMap<String, int[]> changedLinesOverview) throws IOException {
        try {
            HashMap<String, CodeCoverage> codeCoveragePerFile = new HashMap<>(changedFiles.size());

            String modulePath = extractModuleSourceCodePathPath(getClassPathDirectory());


            if (modulePath == null) {
                throw new IOException("Invalid class path directory; 'target' not found: " + getClassPathDirectory().getAbsolutePath());
            }

            String moduleDir = modulePath.split("src/main/java")[0] + "src/main/java";

            FileUtil.Filters filters = new FileUtil.Filters();
            filters.addPathIncludeFilter(modulePath);

            Set<File> sourceFilesOfModule = new FileUtil().filterFiles(changedFiles, filters);

            Set<String> classFilesOfModule = sourceFilesOfModule.stream()
                    .map(file -> file.getPath().replace(".java", ""))
                    .collect(Collectors.toSet());


            // Collect coverage data
            for (IClassCoverage classCoverage : getCoverageBuilder().getClasses()) {

                boolean found = classFilesOfModule.stream().anyMatch(filePath -> filePath.contains(classCoverage.getName()));

                if (found) {
                    int[] changedLinesOfFile = getLinesForPath(classCoverage.getName(), changedLinesOverview);

                    int totalLinesThatAreCovered = 0;
                    int totalLinesThatAreNotCovered = 0;

                    for(int i=0;i<changedLinesOfFile.length;i++){
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



                    // Adjust to get the full file path within the module
                    File sourceFile = new File(moduleDir + "/"+ classCoverage.getName() + ".java");
                    if (sourceFile != null) {
                        String filePath = sourceFile.getPath();
                        CodeCoverage codeCoverageForFile = new CodeCoverage(filePath, CodeCoverage.CoverageType.PER_CHANGED_LINE, -1, -1, totalLinesThatAreNotCovered, totalLinesThatAreCovered);

                        codeCoveragePerFile.put(filePath, codeCoverageForFile);
                    }
                }
            }

            return codeCoveragePerFile;
        } catch (IOException e) {
            throw new IOException("Unable to read " + getJacocoExecFile().getAbsolutePath() + ". Exception: " + e);
        }
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
