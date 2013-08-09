package com.xpert.audit.model;

/**
 *
 * @author Ayslan
 */
public enum AuditingType {

    INSERT("insert"),
    UPDATE("update"),
    DELETE("delete");
    
    private String description;

    AuditingType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
    
}
