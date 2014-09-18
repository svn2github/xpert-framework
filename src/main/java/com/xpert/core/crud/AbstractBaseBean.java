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
import com.xpert.persistence.utils.EntityUtils;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

/**
 * Generic Managed Bean to create CRUD
 * 
 * @author Ayslan
 * @param <T> type of entity
 */
public abstract class AbstractBaseBean<T> {

    private static final Logger logger = Logger.getLogger(AbstractBaseBean.class.getName());
    private static final String ID = "id";
    private Object id;
    private String dialog;
    private LazyDataModelImpl<T> dataModel;
    private T entity;
    private Class entityClass;
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
        if (getClass().getGenericSuperclass() != null && !getClass().getGenericSuperclass().equals(Object.class)) {
            if (getClass().getGenericSuperclass() instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
                if (parameterizedType != null && parameterizedType.getActualTypeArguments() != null && parameterizedType.getActualTypeArguments().length > 0) {
                    entityClass = (Class<T>) parameterizedType.getActualTypeArguments()[0];
                }
            }
        }
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
        loadEntityFromParameter();
        if (entity == null) {
            create();
        }
        init();
        createDataModel();
    }

    /**
     * Load the entity from parameter "id"
     */
    public void loadEntityFromParameter() {
        Object entityId = null;
        if (isLoadEntityOnPostConstruct()) {
            entityId = getIdFromParameter();
        }
        if (entityId != null) {
            Map<String, Object> requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
            Class entityClassToLoad = (Class) requestMap.get(ENTITY_CLASS_TO_LOAD);
            if (entityClassToLoad != null && entityClassToLoad.equals(this.getClass())) {
                entity = findById(entityId);
            }
        }
    }

    /**
     * @return A Number instance of id passed in parameter
     */
    private Object getIdFromParameter() {
        String parameter = FacesUtils.getParameter(ID);
        if (parameter == null || parameter.isEmpty()) {
            return null;
        }
        try {
            Class idType = EntityUtils.getIdType(entityClass);
            if (idType.equals(Long.class)) {
                return Long.parseLong(StringUtils.getOnlyIntegerNumbers(parameter));
            } else if (idType.equals(Integer.class)) {
                return Integer.parseInt(StringUtils.getOnlyIntegerNumbers(parameter));
            }else if (idType.equals(BigDecimal.class)) {
                return new BigDecimal(StringUtils.getOnlyIntegerNumbers(parameter));
            } else {
                logger.log(Level.SEVERE, "Type {0} from entity {1} is not mapped in generic base bean", new Object[]{idType.getName(), entityClass.getName()});
                return null;
            }
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Persists entity:
     * <ol>
     * <li>calls "preSave()"</li>
     * <li>calls " getBO().save()"</li>
     * <li>calls "postSave()"</li>
     * <li>close dialog (if is defined)</li>
     * </ol>
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
        return entityClass;
    }

    /**
     * @return A new instance of Type T
     */
    public T getEntityNewInstance() {
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
        dataModel = new LazyDataModelImpl<T>(getDataModelOrder(), getDAO());
        dataModel.setRestrictions(getDataModelRestrictions());
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
     * This method will delete the current entity calling "bo.delete()":
     * <ol>
     * <li>calls "preDelete()"</li>
     * <li>calls "getBO().remove"</li>
     * <li>calls "postDelete()" if deletion is successful</li>
     * </ol>
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
     * <ol>
     * <li>calls "preDelete()"</li>
     * <li>calls "getBO().remove"</li>
     * <li>calls "postDelete()" if deletion is successful</li>
     * </ol>
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

    /**
     * Reload a entity calling "dao.find(id)"
     */
    public void reloadEntity() {
        if (getEntity() != null) {
            Object entityId = EntityUtils.getId(getEntity());
            if (entityId != null) {
                setEntity(getDAO().find(entityId));
            }
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

    public String getEntitySimpleName() {
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
     * Called after "delete()" and "remove()" methods (only if deletion is
     * successful)
     */
    public void postDelete() {
    }

    public String getDialog() {
        return dialog;
    }

    public void setDialog(String dialog) {
        this.dialog = dialog;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
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
