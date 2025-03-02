package com.brabel.coverage.extension.core.model;

/**
 * Represents the code coverage of a file/class
 */
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
        CLASS, //Just an overall coverage for the class
        TOTAL, //Total coverage for the project
        PER_CHANGED_LINE, //Coverage difference per changed line for the class //TODO: this could probably be split up in total and per class
    }

    private CoverageType coverageType;

    /**
     * Constructor for the CodeCoverage class. Represents the code coverage of a file/class
     * @param filePath The path to the file/class
     * @param coverageType The type of coverage see {@link CoverageType}
     * @param instructionsMissed The number of instructions missed
     * @param instructionsCovered The number of instructions covered
     * @param linesMissed The number of lines missed
     * @param linesCovered The number of lines covered
     */
    public CodeCoverage(String filePath, CoverageType coverageType, int instructionsMissed, int instructionsCovered, int linesMissed, int linesCovered) {
        this.filePath = filePath;
        this.instructionsMissed = instructionsMissed;
        this.instructionsCovered = instructionsCovered;
        this.linesMissed = linesMissed;
        this.linesCovered = linesCovered;
        this.coverageType = coverageType;
    }

    /**
     *
     * @return the path to the file/class for which the code coverage object was generated
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     *
     * @return the instructions missed
     */
    public int getInstructionsMissed() {
        return instructionsMissed;
    }

    /**
     *
     * @return the instructions covered
     */
    public int getInstructionsCovered() {
        return instructionsCovered;
    }

    /**
     *
     * @return the coverage type
     */
    public CoverageType getCoverageType() {
        return coverageType;
    }

    /**
     *
     * @return the lines missed
     */
    public int getLinesMissed() {
        return linesMissed;
    }

    /**
     *
     * @return the lines covered
     */
    public int getLinesCovered() {
        return linesCovered;
    }
}
