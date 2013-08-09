package com.xpert.core.crud;

import com.xpert.core.exception.BusinessException;
import com.xpert.core.exception.UniqueFieldException;
import com.xpert.persistence.dao.BaseDAO;
import com.xpert.persistence.exception.DeleteException;
import com.xpert.core.validation.UniqueField;
import com.xpert.core.validation.UniqueFieldsValidation;
import com.xpert.persistence.utils.EntityUtils;
import java.util.List;

/**
 *
 * @author Ayslan
 */
public abstract class AbstractBusinessObject<T> {

    public abstract BaseDAO getDAO();

    public abstract List<UniqueField> getUniqueFields();

    public abstract boolean isAudit();

    public abstract void validate(T object) throws BusinessException;

    public void validateUniqueFields(Object object) throws UniqueFieldException {
        if (getUniqueFields() != null && !getUniqueFields().isEmpty()) {
            UniqueFieldsValidation.validateUniqueFields(getUniqueFields(), object, getDAO());
        }
    }

    public void save(T object) throws BusinessException {

        BusinessException exception = new BusinessException();
        try {
            validate(object);
            validateUniqueFields(object);
        } catch (BusinessException ex) {
            exception.add(ex);
        }
        exception.check();
        if (!EntityUtils.isPersisted(object)) {
            getDAO().save(object, isAudit());
        } else {
            getDAO().merge(object, isAudit());
        }
    }

    public void delete(Long id) throws DeleteException {
        getDAO().delete(id);
    }
}
