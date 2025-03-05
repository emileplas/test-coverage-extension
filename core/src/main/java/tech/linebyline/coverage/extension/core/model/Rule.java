package tech.linebyline.coverage.extension.core.model;



/**
 * Model for the rules that can be applied to the coverage.
 * Rules define the type of coverage requirement and the minimum coverage threshold.
 */
public class Rule {

    /**
     * Enum for the different types of rules that can be applied.
     */
    public enum RuleType {
        /**
         * Ensures that the entire project meets a minimum coverage threshold.
         */
        OVERALL,

        /**
         * Ensures that each modified class meets a minimum coverage threshold.
         */
        PER_CLASS,

        /**
         * Ensures that all modified lines across the project meet a minimum coverage threshold.
         */
        TOTAL_CHANGED_LINES,

        /**
         * Ensures that all modified lines within each modified class meet a minimum coverage threshold.
         */
        PER_CLASS_CHANGED_LINES
    }

    private RuleType type;


    private double threshold;

    public Rule() {
        // Default constructor
    }

    /**
     * Constructs a new Rule with the specified type and minimum coverage.
     *
     * @param type         the type of rule
     * @param threshold  the minimum coverage required (must be between 0 and 100)
     * @throws IllegalArgumentException if minimumCoverage is not between 0 and 100
     */
    public Rule(RuleType type, double threshold) {
        if (threshold < 0 || threshold > 100) {
            throw new IllegalArgumentException("Minimum coverage must be between 0 and 100");
        }
        this.type = type;
        this.threshold = threshold;
    }

    /**
     * Returns the type of the rule.
     *
     * @return the rule type
     */
    public RuleType getType() {
        return type;
    }

    /**
     * Returns the minimum coverage required for the rule.
     *
     * @return the minimum coverage
     */
    public double getThreshold() {
        return threshold;
    }
}