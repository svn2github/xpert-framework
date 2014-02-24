/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xpert.persistence.query;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.TemporalType;

/**
 *
 * @author Ayslan
 */
public class Restriction {

    private String property;
    private Object value;
    private RestrictionType restrictionType;
    private LikeType likeType;
    private TemporalType temporalType;
    private List<Restriction> or;

    public Restriction() {
    }

    public Restriction(String property, RestrictionType restrictionType) {
        this.property = property;
        this.restrictionType = restrictionType;
    }

    public Restriction(String property, Object value) {
        this.property = property;
        this.value = value;
    }

    public Restriction(String property, RestrictionType restrictionType, Date value, TemporalType temporalType) {
        this.property = property;
        this.value = value;
        this.temporalType = temporalType;
        this.restrictionType = restrictionType;
    }

    public Restriction(String property, RestrictionType restrictionType, Calendar value, TemporalType temporalType) {
        this.property = property;
        this.value = value;
        this.temporalType = temporalType;
        this.restrictionType = restrictionType;
    }

    public Restriction(String property, RestrictionType restrictionType, Object value) {
        this.property = property;
        this.value = value;
        this.restrictionType = restrictionType;
    }

    public Restriction(String property, RestrictionType restrictionType, Object value, LikeType likeType) {
        this.property = property;
        this.value = value;
        this.restrictionType = restrictionType;
        this.likeType = likeType;
    }

    public TemporalType getTemporalType() {
        return temporalType;
    }

    public void setTemporalType(TemporalType temporalType) {
        this.temporalType = temporalType;
    }

    public LikeType getLikeType() {
        return likeType;
    }

    public void setLikeType(LikeType likeType) {
        this.likeType = likeType;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public RestrictionType getRestrictionType() {
        return restrictionType;
    }

    public void setRestrictionType(RestrictionType restrictionType) {
        this.restrictionType = restrictionType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public List<Restriction> getOr() {
        return or;
    }

    public void setOr(List<Restriction> or) {
        this.or = or;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Restriction other = (Restriction) obj;
        if ((this.property == null) ? (other.property != null) : !this.property.equals(other.property)) {
            return false;
        }
        if (this.value != other.value && (this.value == null || !this.value.equals(other.value))) {
            return false;
        }
        if (this.restrictionType != other.restrictionType) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.property != null ? this.property.hashCode() : 0);
        hash = 53 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 53 * hash + (this.restrictionType != null ? this.restrictionType.hashCode() : 0);
        return hash;
    }
}
