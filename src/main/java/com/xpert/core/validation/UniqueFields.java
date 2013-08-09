package com.xpert.core.validation;

import java.util.ArrayList;

/**
 *
 * @author Ayslan
 */
public class UniqueFields extends ArrayList<UniqueField> {

    public UniqueFields(String... fields) {
        add(new UniqueField(fields));
    }

    public UniqueFields add(String... fields) {
        add(new UniqueField(fields));
        return this;
    }
    
    public UniqueFields add(UniqueField uniqueField, String message) {
        add(uniqueField.setMessage(message));
        return this;
    }
    
}
