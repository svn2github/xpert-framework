package com.xpert.maker.model;

import com.xpert.utils.StringUtils;

/**
 *
 * @author ayslan
 */
public class ViewField {

    private String name;
    private String typeName;
    private boolean required;
    private boolean id;
    private boolean oneToOne;
    private boolean oneToMany;
    private boolean manyToOne;
    private boolean manyToMany;
    private boolean lazy;
    private boolean decimal;
    private boolean integer;
    private boolean date;
    private boolean time;
    private boolean yesNo;
    private boolean enumeration;
    private boolean string;
    private String maxlength;
    
    public boolean isRenderFieldInFormCreate(){
        return id == false && oneToOne == false && oneToMany == false;
    }

    public String getTypeNameLower() {
        if (typeName != null) {
            return StringUtils.getLowerFirstLetter(typeName);
        }
        return typeName;
    }

    public boolean isTime() {
        return time;
    }

    public void setTime(boolean time) {
        this.time = time;
    }
    
    public boolean isString() {
        return string;
    }

    public void setString(boolean string) {
        this.string = string;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isEnumeration() {
        return enumeration;
    }

    public void setEnumeration(boolean enumeration) {
        this.enumeration = enumeration;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public boolean isManyToMany() {
        return manyToMany;
    }

    public void setManyToMany(boolean manyToMany) {
        this.manyToMany = manyToMany;
    }

    public boolean isManyToOne() {
        return manyToOne;
    }

    public void setManyToOne(boolean manyToOne) {
        this.manyToOne = manyToOne;
    }

    public boolean isOneToMany() {
        return oneToMany;
    }

    public void setOneToMany(boolean oneToMany) {
        this.oneToMany = oneToMany;
    }

    public boolean isOneToOne() {
        return oneToOne;
    }

    public void setOneToOne(boolean oneToOne) {
        this.oneToOne = oneToOne;
    }

    public boolean isCollection() {
        return oneToMany || manyToMany;
    }

    public boolean isId() {
        return id;
    }

    public void setId(boolean id) {
        this.id = id;
    }

    public boolean isDate() {
        return date;
    }

    public void setDate(boolean date) {
        this.date = date;
    }

    public boolean isDecimal() {
        return decimal;
    }

    public void setDecimal(boolean decimal) {
        this.decimal = decimal;
    }

    public boolean isInteger() {
        return integer;
    }

    public void setInteger(boolean integer) {
        this.integer = integer;
    }

    public boolean isLazy() {
        return lazy;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isYesNo() {
        return yesNo;
    }

    public void setYesNo(boolean yesNo) {
        this.yesNo = yesNo;
    }

    public String getMaxlength() {
        return maxlength;
    }

    public void setMaxlength(String maxlength) {
        this.maxlength = maxlength;
    }

  
}
