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
        validateRules(rules);
        this.rules = rules;
    }

    private void validateRules(List<Rule> rules) {
        if(rules == null){
            throw new IllegalArgumentException("Rules cannot be null.");
        }
        if(rules.isEmpty()){
            throw new IllegalArgumentException("Rules cannot be empty. At least one rule must be set.");
        }
        //check if there are no 2 rules of the same type
        for (int i = 0; i < rules.size(); i++) {
            for (int j = i + 1; j < rules.size(); j++) {
                if(rules.get(i).getRuleType() == rules.get(j).getRuleType()){
                    throw new IllegalArgumentException("There are 2 rules of the same type: " + rules.get(i).getRuleType());
                }
            }
        }
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
        validateRules(rules);
        this.rules = rules;
    }

    /**
     * Adds a rule to the RuleManager.
     * @param rule the rule to add
     */
    public void addRule(Rule rule) {
        if(rule == null){
            throw new IllegalArgumentException("Rule cannot be null.");
        }
        if(rules.contains(rule)){
            throw new IllegalArgumentException("Rule already exists.");
        }
        //check if there are no 2 rules of the same type
        for (Rule r : rules) {
            if(r.getRuleType() == rule.getRuleType()){
                throw new IllegalArgumentException("Rule could not be added since there is already a rule of the same type: " + rule.getRuleType());
            }
        }
        rules.add(rule);

    }
}
