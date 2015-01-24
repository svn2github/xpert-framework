package com.xpert.faces.function;

import com.xpert.core.conversion.NumberUtils;
import com.xpert.faces.primefaces.LazyDataModelImpl;
import java.math.BigDecimal;
import java.util.Collection;
import javax.faces.FacesException;
import javax.faces.model.DataModel;

/**
 *
 * @author Ayslan
 */
public class NumberFunctions {

    /**
     * Sum implmentantation for LazyDataModelImpl, this method create a query
     * que the field calling lazyDataModel.sum()
     *
     * @param lazyDataModel
     * @param field
     * @return
     */
    public static Object sumAll(LazyDataModelImpl lazyDataModel, String field) {
        return lazyDataModel.sum(field);
    }

    /**
     * If the object is a instanceof DataModel (example: standart DataModel,
     * primefaces LazyDataModel, xpert-framework LazyDataModelImpl ), return
     * getWrappedData()
     *
     * @param objects
     * @return
     */
    public static Collection getCollection(Object objects) {
        Collection collection = null;
        if (objects != null) {
            if (objects instanceof DataModel) {
                collection = (Collection) ((DataModel) objects).getWrappedData();
            } else if (objects instanceof Collection) {
                collection = (Collection) objects;
            } else {
                throw new FacesException("Type " + objects.getClass().getName() + " not supported in sum");
            }
        }
        return collection;
    }

    public static BigDecimal sum(Object objects, String field) {
        Collection collection = getCollection(objects);
        try {
            return NumberUtils.sum(collection, field);
        } catch (Exception ex) {
            throw new FacesException(ex);
        }
    }

    public static Integer sumInteger(Object objects, String field) {
        Collection collection = getCollection(objects);
        try {
            return NumberUtils.sumInteger(collection, field);
        } catch (Exception ex) {
            throw new FacesException(ex);
        }
    }

    public static Long sumLong(Object objects, String field) {
        Collection collection = getCollection(objects);
        try {
            return NumberUtils.sumLong(collection, field);
        } catch (Exception ex) {
            throw new FacesException(ex);
        }
    }

}
