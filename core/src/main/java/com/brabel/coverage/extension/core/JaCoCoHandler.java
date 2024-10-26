package com.brabel.coverage.extension.core;

import org.jacoco.core.analysis.*;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class JaCoCoHandler {

    public static CodeCoverage getTotalCodeCoverage(File jacocoExecFile, File classPathDirectory) throws IOException {
        try {
            //TODO: move these to common settings for following methods
            FileInputStream execFile = new FileInputStream(jacocoExecFile);

            ExecutionDataStore executionData = new ExecutionDataStore();
            SessionInfoStore sessionInfo = new SessionInfoStore();
            ExecutionDataReader reader = new ExecutionDataReader(execFile);
            reader.setExecutionDataVisitor(executionData);
            reader.setSessionInfoVisitor(sessionInfo);
            reader.read();

            CoverageBuilder coverageBuilder = new CoverageBuilder();
            Analyzer analyzer = new Analyzer(executionData, coverageBuilder);

            // Analyze classes directory
            analyzer.analyzeAll(classPathDirectory);

            // Collect coverage data
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
        } catch (IOException e) {
            throw new IOException("Unable to read " + jacocoExecFile.getAbsolutePath() + ". Exception: " + e);
        }

    }




}
