package com.xpert.audit;

import com.xpert.faces.primefaces.LazyDataModelImpl;
import com.xpert.audit.model.AbstractAuditing;
import com.xpert.persistence.dao.BaseDAO;
import com.xpert.persistence.query.JoinBuilder;
import com.xpert.persistence.query.Restriction;
import com.xpert.persistence.utils.EntityUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.primefaces.model.LazyDataModel;

/**
 *
 * @author Ayslan
 */
public class DetailAuditBean {

    private static final Logger logger = Logger.getLogger(AuditBean.class.getName());
    private BeanModel beanModel;
    private LazyDataModel<AbstractAuditing> auditings;
    private BaseDAO baseDAO;

    public DetailAuditBean() {
    }

    public DetailAuditBean(BeanModel beanModel, BaseDAO baseDAO) {
        this.beanModel = beanModel;
        this.baseDAO = baseDAO;
    }

    public void load() {
        if (beanModel != null) {
            auditings = new LazyDataModelImpl<AbstractAuditing>("eventDate DESC", getRestrictions(), baseDAO);
        }
    }

    private List<Restriction> getRestrictions() {

        List<Restriction> restrictions = new ArrayList<Restriction>();

        restrictions.add(new Restriction("identifier", beanModel.getId()));
        restrictions.add(new Restriction("entity", beanModel.getEntity()));

        return restrictions;
    }

    public boolean isPersisted(Object object) {
        if (EntityUtils.getId(object) != null) {
            return true;
        }
        return false;
    }

    public LazyDataModel<AbstractAuditing> getAuditings() {
        return auditings;
    }

    public void setAuditings(LazyDataModel<AbstractAuditing> auditings) {
        this.auditings = auditings;
    }

    public BeanModel getBeanModel() {
        return beanModel;
    }

    public void setBeanModel(BeanModel beanModel) {
        this.beanModel = beanModel;
    }
}
