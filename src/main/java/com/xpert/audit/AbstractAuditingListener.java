package com.xpert.audit;

import com.xpert.audit.model.AbstractAuditing;

/**
 *
 * @author Ayslan
 */
public interface AbstractAuditingListener {
    
    public void onSave(AbstractAuditing abstractAuditing);
    
}
