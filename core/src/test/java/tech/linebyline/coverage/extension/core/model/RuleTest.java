package tech.linebyline.coverage.extension.core.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RuleTest {

    @Test
    public void testRule(){
        Rule rule = new Rule(Rule.RuleType.OVERALL, 80.50);
        Assertions.assertEquals(Rule.RuleType.OVERALL, rule.getType());
        Assertions.assertEquals(80.50, rule.getThreshold());
    }

    @Test
    public void testRulePerClass(){
        Rule rule = new Rule(Rule.RuleType.PER_CLASS, 80.50);
        Assertions.assertEquals(Rule.RuleType.PER_CLASS, rule.getType());
        Assertions.assertEquals(80.50, rule.getThreshold());
    }

    @Test
    public void testRulePerClassChangedLines(){
        Rule rule = new Rule(Rule.RuleType.PER_CLASS_CHANGED_LINES, 80.50);
        Assertions.assertEquals(Rule.RuleType.PER_CLASS_CHANGED_LINES, rule.getType());
        Assertions.assertEquals(80.50, rule.getThreshold());
    }

    @Test
    public void testRulePerClassTotalChangedLines(){
        Rule rule = new Rule(Rule.RuleType.TOTAL_CHANGED_LINES, 80.50);
        Assertions.assertEquals(Rule.RuleType.TOTAL_CHANGED_LINES, rule.getType());
        Assertions.assertEquals(80.50, rule.getThreshold());
    }

    @Test
    public void invalidMinimumCoverage(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Rule(Rule.RuleType.OVERALL, -1);
        });
    }


}
