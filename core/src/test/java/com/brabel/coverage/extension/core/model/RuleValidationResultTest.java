package com.brabel.coverage.extension.core.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RuleValidationResultTest {

    @Test
    public void testRuleValidationResultConstructor(){
        RuleValidationResult ruleValidationResult = new RuleValidationResult(true, "Test message");
        Assertions.assertTrue(ruleValidationResult.isSuccessful());
        Assertions.assertEquals("Test message", ruleValidationResult.getMessage());
    }

    @Test
    public void testRuleValidationResultConstructor2(){
        RuleValidationResult ruleValidationResult = new RuleValidationResult(false, "Test message 2");
        Assertions.assertFalse(ruleValidationResult.isSuccessful());
        Assertions.assertEquals("Test message 2", ruleValidationResult.getMessage());
    }



}
