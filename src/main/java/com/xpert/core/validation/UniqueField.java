/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xpert.core.validation;

/**
 *
 * @author Ayslan
 */
public class UniqueField {

    private String message;
    private String[] constraints;

    public UniqueField(String... fields) {
        this.constraints = fields;
    }

    public String[] getConstraints() {
        return constraints;
    }

    public void setConstraints(String[] constraints) {
        this.constraints = constraints;
    }

    public String getMessage() {
        return message;
    }
    
    public UniqueField setMessage(String message) {
        this.message = message;
        return this;
    }
}
