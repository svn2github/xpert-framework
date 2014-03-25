package com.xpert.persistence.query;

/**
 *
 * @author Ayslan
 */
public enum RestrictionType {

    EQUALS("="), 
    NOT_EQUALS("!="), 
    GREATER_THAN(">"), 
    LESS_THAN("<"), 
    GREATER_EQUALS_THAN(">="), 
    LESS_EQUALS_THAN("<="), 
    LIKE("LIKE"), 
    NOT_LIKE("NOT LIKE"), 
    IN("IN"),
    NOT_IN("NOT IN"), 
    NULL("IS NULL", true),
    NOT_NULL("IS NOT NULL", true),
    DATA_TABLE_FILTER("LIKE"),
    OR("OR", true),
    START_GROUP("(", true),
    QUERY_STRING("", true),
    END_GROUP(")", true);
    
    private String symbol;
    private boolean ignoreParameter;

    private RestrictionType(String symbol) {
        this.symbol = symbol;
    }

    private RestrictionType(String symbol, boolean ignoreParameter) {
        this.symbol = symbol;
        this.ignoreParameter = ignoreParameter;
    }
    
    public String getSymbol() {
        return symbol;
    }

    public boolean isIgnoreParameter() {
        return ignoreParameter;
    }
    
    
}
