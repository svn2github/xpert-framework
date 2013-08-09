package com.xpert.persistence.query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.persistence.TemporalType;

/**
 *
 * @author Ayslan
 */
public class Restrictions extends ArrayList<Restriction> {
    
    public Restrictions add(String property, RestrictionType restrictionType) {
        this.add(new Restriction(property, restrictionType));
        return this;
    }

    public Restrictions add(String property, Object value) {
        this.add(new Restriction(property, value));
        return this;
    }

    public Restrictions add(String property, RestrictionType restrictionType, Date value, TemporalType temporalType) {
        this.add(new Restriction(property, restrictionType, value, temporalType));
        return this;
    }

    public Restrictions add(String property, RestrictionType restrictionType, Calendar value, TemporalType temporalType) {
        this.add(new Restriction(property, restrictionType, value, temporalType));
        return this;
    }

    public Restrictions add(String property, RestrictionType restrictionType, Object value) {
        this.add(new Restriction(property, restrictionType, value));
        return this;
    }

    public Restrictions add(String property, RestrictionType restrictionType, Object value, LikeType likeType) {
        this.add(new Restriction(property, restrictionType, value, likeType));
        return this;
    }
}
