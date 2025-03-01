package com.brabel.coverage.extension.core.services;

import com.brabel.coverage.extension.core.model.Rule;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the rules that can be applied to the coverage.
 */
public class RuleManager {

    List<Rule> rules;

    /**
     * Constructs a new RuleManager with no rules.
     */
    public RuleManager() {
        this.rules = new ArrayList<>();
    }

    /**
     * Constructs a new RuleManager with the specified rules.
     * @param rules the rules to manage
     */
    public RuleManager(List<Rule> rules) {
        this.rules = rules;
    }

    /**
     * Returns the rules managed by this RuleManager.
     * @return the rules
     */
    public List<Rule> getRules() {
        return rules;
    }

    /**
     * Sets the rules managed by this RuleManager.
     * @param rules the rules to manage
     */
    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    /**
     * Adds a rule to the RuleManager.
     * @param rule the rule to add
     */
    public void addRule(Rule rule) {
        rules.add(rule);
    }
}
