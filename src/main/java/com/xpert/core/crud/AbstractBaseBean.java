package com.xpert.core.crud;

import com.xpert.core.exception.BusinessException;
import com.xpert.faces.primefaces.FilterByHandler;
import com.xpert.faces.primefaces.LazyCountType;
import com.xpert.faces.utils.FacesMessageUtils;
import com.xpert.faces.primefaces.PrimeFacesUtils;
import com.xpert.i18n.XpertResourceBundle;
import com.xpert.faces.primefaces.LazyDataModelImpl;
import com.xpert.faces.primefaces.OrderByHandler;
import com.xpert.utils.StringUtils;
import com.xpert.faces.utils.FacesUtils;
import com.xpert.persistence.dao.BaseDAO;
import com.xpert.persistence.exception.DeleteException;
import com.xpert.persistence.query.JoinBuilder;
import com.xpert.persistence.query.Restriction;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

/**
 *
 * @author Ayslan
 * @param <T> type of entity
 */
public abstract class AbstractBaseBean<T> {

    private static final Logger logger = Logger.getLogger(AbstractBaseBean.class.getName());
    private static final String ID = "id";
    private Long id;
    private String dialog;
    private LazyDataModelImpl<T> dataModel;
    private T entity;
    private boolean loadEntityOnPostConstruct = true;
    private static final String ENTITY_CLASS_TO_LOAD = "xpert.entityClassToLoad";

    /**
     * @return a AbstractBusinessObject instance to be used in operations
     */
    public abstract AbstractBusinessObject getBO();

    /**
     * @return Default order od LazyDatamodel
     */
    public abstract String getDataModelOrder();

    public OrderByHandler getOrderByHandler() {
        return null;
    }

    public JoinBuilder getDataModelJoinBuilder() {
        return null;
    }

    public FilterByHandler getFilterByHandler() {
        return null;
    }

    /**
     * Called after "@PostConstruct"
     */
    public void init() {
    }

    public AbstractBaseBean() {
        if (isLoadEntityOnPostConstruct()) {
            Map<String, Object> requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
            if (!requestMap.containsKey(ENTITY_CLASS_TO_LOAD)) {
                FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put(ENTITY_CLASS_TO_LOAD, this.getClass());
            }
        }
    }

    /**
     * Method called on "@PostConstruct" event
     */
    @PostConstruct
    public void postConstruct() {
        Long entityId = null;
        if (isLoadEntityOnPostConstruct()) {
            entityId = getIdFromParameter();
        }
        if (entityId != null) {
            Map<String, Object> requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
            Class entityClassToLoad = (Class) requestMap.get(ENTITY_CLASS_TO_LOAD);
            if (entityClassToLoad != null && entityClassToLoad.equals(this.getClass())) {
                entity = findById(entityId);
            }
        } else {
            create();
        }
        init();
        createDataModel();
    }

    /**
     * @return A Long instance of id passed in parameter
     */
    private Long getIdFromParameter() {
        String parameter = FacesUtils.getParameter(ID);
        if (parameter == null || parameter.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(StringUtils.getOnlyIntegerNumbers(parameter));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Persists entity:
     *
     * 1 - calls "preSave()" 
     * 2 - calls " getBO().save()" 
     * 3 - calls "postSave()" 
     * 4 - close dialog (if is defined)
     *
     */
    public void save() {
        try {
            preSave();
            getBO().save(entity);
            postSave();
            PrimeFacesUtils.closeDialog(dialog);
            FacesMessageUtils.sucess();
        } catch (BusinessException ex) {
            FacesMessageUtils.error(ex);
        }
    }

    /**
     * @return BaseDAO defined in "getBO()"
     */
    public BaseDAO<T> getDAO() {
        return getBO().getDAO();
    }

    /**
     * @return A List of Restrictions, the Restrictions are used in
     * LazyDataModel
     */
    public List<Restriction> getDataModelRestrictions() {
        return null;
    }

    /**
     * Method called before save entity
     */
    public void preSave() {
    }

    /**
     * Method called after asve entity (only if save is successful)
     */
    public void postSave() {
    }

    /**
     * Creates a new entity instance
     */
    public void create() {
        entity = getEntityNewInstance();
    }

    /**
     * @return The class in generic Type
     */
    public Class getEntityClass() {
        if (getClass().getGenericSuperclass() != null && !getClass().getGenericSuperclass().equals(Object.class)) {
            if (getClass().getGenericSuperclass() instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
                if (parameterizedType != null && parameterizedType.getActualTypeArguments() != null && parameterizedType.getActualTypeArguments().length > 0) {
                    return (Class<T>) parameterizedType.getActualTypeArguments()[0];
                }
            }
        }
        return null;
    }

    /**
     * @return A new instance of Type T
     */
    public final T getEntityNewInstance() {
        try {
            return (T) getEntityClass().newInstance();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Creates a LazyDataModelImpl to entity
     */
    public void createDataModel() {
        dataModel = new LazyDataModelImpl<T>(getDataModelOrder(), getDataModelRestrictions(), getDAO());
        OrderByHandler orderByHandler = getOrderByHandler();
        if (orderByHandler != null) {
            dataModel.setOrderByHandler(orderByHandler);
        }
        FilterByHandler filterByHandler = getFilterByHandler();
        if (filterByHandler != null) {
            dataModel.setFilterByHandler(filterByHandler);
        }
        dataModel.setLazyCountType(getDataModelLazyCountType());
        dataModel.setJoinBuilder(getDataModelJoinBuilder());
    }

   /**
     *  This method will delete the current entity calling "bo.delete()":
     * 
     *  1 - calls "preDelete()"
     *  2 - calls "getBO().remove"
     *  2 - calls "postDelete()" if deletion is successful
     *  
     */
    public void delete() {
        try {
            preDelete();
            if (id != null) {
                getBO().delete(id);
                FacesMessageUtils.sucess();
                postDelete();
                id = null;
            }
        } catch (DeleteException ex) {
            FacesMessageUtils.error(XpertResourceBundle.get("objectCannotBeDeleted"));
        }
    }

    /**
     * This method will delete the current entity calling "bo.remove()":
     * 
     *  1 - calls "preDelete()"
     *  2 - calls "getBO().remove"
     *  2 - calls "postDelete()" if deletion is successful
     *  
     */
    public void remove() {
        try {
            preDelete();
            getBO().remove(getEntity());
            FacesMessageUtils.sucess();
            postDelete();
        } catch (DeleteException ex) {
            FacesMessageUtils.error(XpertResourceBundle.get("objectCannotBeDeleted"));
        }
    }

    public T findById(Object id) {
        if (id != null) {
            Object object = (T) getDAO().find(id);
            if (object != null) {
                return (T) object;
            }
        }
        if (entity != null) {
            return entity;
        }
        try {
            return getEntityNewInstance();
        } catch (Exception ex) {
            FacesMessageUtils.error(ex.getMessage());
        }
        return null;
    }

    /**
     *
     * @return LazyCountType used in LazyDataModel, defaul is ALWAYS
     */
    public LazyCountType getDataModelLazyCountType() {
        return LazyCountType.ALWAYS;
    }

    public final String getEntitySimpleName() {
        if (entity != null) {
            return entity.getClass().getSimpleName();
        } else {
            return ((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]).getSimpleName();
        }
    }

    /**
     * Called before "delete()" and "remove()" methods
     */
    public void preDelete() {
    }

    /**
     * Called after "delete()" and "remove()" methods (only if deletion is successful)
     */
    public void postDelete() {
    }

    public String getDialog() {
        return dialog;
    }

    public void setDialog(String dialog) {
        this.dialog = dialog;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LazyDataModelImpl<T> getDataModel() {
        return dataModel;
    }

    public void setDataModel(LazyDataModelImpl<T> dataModel) {
        this.dataModel = dataModel;
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    /**
     * @return true if the entity must be loaded from parameter "id", default
     * value is true
     */
    public boolean isLoadEntityOnPostConstruct() {
        return loadEntityOnPostConstruct;
    }

    public void setLoadEntityOnPostConstruct(boolean loadEntityOnPostConstruct) {
        this.loadEntityOnPostConstruct = loadEntityOnPostConstruct;
    }
}
