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
import java.util.Set;
import java.util.stream.Collectors;

public class JaCoCoInteractor {


    public static CodeCoverage getTotalCodeCoverage(File jacocoExecFile, File classPathDirectory) throws IOException {
        try {
            CoverageBuilder coverageBuilder = new CoverageBuilder();

            analyzeClassesDirectory(jacocoExecFile, coverageBuilder, classPathDirectory);

            return getTotalCoverageData(coverageBuilder);
        } catch (IOException e) {
            throw new IOException("Unable to read " + jacocoExecFile.getAbsolutePath() + ". Exception: " + e);
        }
    }

    public static String replaceFromTargetOnward(String input, String target) {
        if (input == null || target == null) {
            return input;
        }

        int targetIndex = input.indexOf(target);

        // Check if the target substring is found
        if (targetIndex != -1) {
            return input.substring(0, targetIndex);
        }

        // Return the original string if the target is not found
        return input;
    }

    public static String extractModulePath(File classPathDirectory) {
        Path path = classPathDirectory.toPath().normalize();
        String pathToString = path.toString();

        return pathToString.replace("../", "").replace("target/classes", "src/main/java"); // Return null if "target" is not found
    }

    //TODO: we will need to fix how it gets the source dir etc, since that will depend on the module settings as passed to maven
    public static HashMap<String, CodeCoverage> getOverallCodeCoverageForChangedFiles(File jacocoExecFile, File classPathDirectory, Set<File> changedFiles) throws IOException {
        try {
            CoverageBuilder coverageBuilder = new CoverageBuilder();
            analyzeClassesDirectory(jacocoExecFile, coverageBuilder, classPathDirectory);

            HashMap<String, CodeCoverage> codeCoveragePerFile = new HashMap<>(changedFiles.size());

            String modulePath = extractModulePath(classPathDirectory);


            if (modulePath == null) {
                throw new IOException("Invalid class path directory; 'target' not found: " + classPathDirectory.getAbsolutePath());
            }

            String moduleDir = modulePath.split("src/main/java")[0] + "src/main/java";

            FileUtil.Filters filters = new FileUtil.Filters();
            filters.addPathIncludeFilter(modulePath);

            Set<File> sourceFilesOfModule = new FileUtil().filterFiles(changedFiles, filters);

            Set<String> classFilesOfModule = sourceFilesOfModule.stream()
                    .map(file -> file.getPath().replace(".java", ""))
                    .collect(Collectors.toSet());


            // Collect coverage data
            for (IClassCoverage classCoverage : coverageBuilder.getClasses()) {

                boolean found = classFilesOfModule.stream().anyMatch(filePath -> filePath.contains(classCoverage.getName()));

                if (found) {
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
            throw new IOException("Unable to read " + jacocoExecFile.getAbsolutePath() + ". Exception: " + e);
        }
    }


    private static File getFileFromSet(File targetFile, Set<File> fileSet) {
        for (File file : fileSet) {
            if (file.equals(targetFile)) {
                return file;
            }
        }
        return null;
    }

    private static void analyzeClassesDirectory(File jacocoExecFile, CoverageBuilder coverageBuilder, File classPathDirectory) throws IOException {
        Analyzer analyzer = new Analyzer(getExecutionDataStore(jacocoExecFile), coverageBuilder);
        analyzer.analyzeAll(classPathDirectory);
    }

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
