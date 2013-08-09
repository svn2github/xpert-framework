package com.xpert.persistence.query;

/**
 *
 * @author Ayslan
 */
public enum RestrictionType {

    EQUALS("="), NOT_EQUALS("!="), GREATER_THAN(">"), LESS_THAN("<"), GREATER_EQUALS_THAN(">="), 
    LESS_EQUALS_THAN("<="), LIKE("LIKE"), NOT_LIKE("NOT LIKE"), IN("IN"), NOT_IN("NOT IN"), NULL("IS NULL"),NOT_NULL("IS NOT NULL"),
    DATA_TABLE_FILTER("LIKE");
    
    private String symbol;

    private RestrictionType(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
