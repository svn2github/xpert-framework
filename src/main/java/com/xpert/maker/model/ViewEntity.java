package com.xpert.maker.model;

import com.xpert.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ayslan
 */
public class ViewEntity {

    private String name;
    private String idFieldName;
    private List<ViewField> fields = new ArrayList<ViewField>();
    
    
    public String getNameLower() {
        if (name != null) {
            return StringUtils.getLowerFirstLetter(name);
        }
        return name;
    }

    public String getIdFieldName() {
        return idFieldName;
    }

    public void setIdFieldName(String idFieldName) {
        this.idFieldName = idFieldName;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ViewField> getFields() {
        return fields;
    }

    public void setFields(List<ViewField> fields) {
        this.fields = fields;
    }
    
}
