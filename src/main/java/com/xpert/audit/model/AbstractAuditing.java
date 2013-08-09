package com.xpert.audit.model;

import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Ayslan
 */
@MappedSuperclass
public abstract class AbstractAuditing {

    private String entity;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventDate;
    
    private Long identifier;
    
    @Column(length = 10)
    @Enumerated(EnumType.STRING)
    private AuditingType auditingType;

    public abstract Object getId();

    public abstract List getMetadatas();
    
    public abstract String getUserName();
    
    public abstract void setMetadatas(List metadatas);
    
    public Long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Long identifier) {
        this.identifier = identifier;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }
    
    
    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }
    
    public AuditingType getAuditingType() {
        return auditingType;
    }

    public void setAuditingType(AuditingType auditingType) {
        this.auditingType = auditingType;
    }
    
    
}
