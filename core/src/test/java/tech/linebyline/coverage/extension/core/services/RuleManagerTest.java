package tech.linebyline.coverage.extension.core.services;

import tech.linebyline.coverage.extension.core.model.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class RuleManagerTest {

    @Test
    public void testRuleManagerConstructor(){
        RuleManager ruleManager = new RuleManager();
        Assertions.assertNotNull(ruleManager.getRules());
        Assertions.assertTrue(ruleManager.getRules().isEmpty());
    }

    @Test
    public void testConstructorWithRules(){
        Rule rule = new Rule(Rule.RuleType.OVERALL, 80.50);
        RuleManager ruleManager = new RuleManager(List.of(rule));
        Assertions.assertEquals(1, ruleManager.getRules().size());
        Assertions.assertEquals(rule, ruleManager.getRules().get(0));
    }

    @Test
    public void testAddRule(){
        Rule rule = new Rule(Rule.RuleType.OVERALL, 80.50);
        RuleManager ruleManager = new RuleManager();
        ruleManager.addRule(rule);
        Assertions.assertEquals(1, ruleManager.getRules().size());
        Assertions.assertEquals(rule, ruleManager.getRules().get(0));
    }
}
