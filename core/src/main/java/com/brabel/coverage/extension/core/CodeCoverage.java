package com.brabel.coverage.extension.core;

public class CodeCoverage {

    private String filePath;
    private int instructionsMissed;
    private int instructionsCovered;

    private int linesMissed;
    private int linesCovered;



    public void setLinesCovered(int linesCovered) {
        this.linesCovered = linesCovered;
    }


    public static enum CoverageType {
        CLASS,
        TOTAL
    }

    private CoverageType coverageType;

    public CodeCoverage(String filePath, CoverageType coverageType, int instructionsMissed, int instructionsCovered, int linesMissed, int linesCovered) {
        this.filePath = filePath;
        this.instructionsMissed = instructionsMissed;
        this.instructionsCovered = instructionsCovered;
        this.linesMissed = linesMissed;
        this.linesCovered = linesCovered;
        this.coverageType = coverageType;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getInstructionsMissed() {
        return instructionsMissed;
    }

    public int getInstructionsCovered() {
        return instructionsCovered;
    }

    public CoverageType getCoverageType() {
        return coverageType;
    }

    public int getLinesMissed() {
        return linesMissed;
    }

    public int getLinesCovered() {
        return linesCovered;
    }
}
