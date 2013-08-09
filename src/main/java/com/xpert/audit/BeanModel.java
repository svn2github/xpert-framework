/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xpert.audit;

/**
 *
 * @author Ayslan
 */
public class BeanModel {
    
    private Object id;
    private String entity;

    public BeanModel(Object id, String entity) {
        this.id = id;
        this.entity = entity;
    }

    public BeanModel() {
    }

    
    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BeanModel other = (BeanModel) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        if ((this.entity == null) ? (other.entity != null) : !this.entity.equals(other.entity)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 29 * hash + (this.entity != null ? this.entity.hashCode() : 0);
        return hash;
    }


    @Override
    public String toString() {
        return "BeanModel{" + "id=" + id + ", entity=" + entity + '}';
    }
    
}
