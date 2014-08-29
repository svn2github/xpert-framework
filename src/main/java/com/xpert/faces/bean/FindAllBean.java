package com.xpert.faces.bean;

import com.xpert.DAO;
import com.xpert.persistence.dao.BaseDAO;
import com.xpert.persistence.query.Restriction;
import com.xpert.persistence.utils.EntityUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.model.SelectItem;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author Ayslan
 */
public abstract class FindAllBean {

    private static final Logger logger = Logger.getLogger(FindAllBean.class.getName());

    /**
     * Define de default order for the Map Key (Class), also set the default
     * attribute to itemLabel in SelectItem
     */
    public abstract Map<Class, ClassModel> getClassModel();
    private Map<Class, List> values = new HashMap<Class, List>();
    private boolean reload = false;
    private BaseDAO baseDAO;

    @PostConstruct
    public void init() {
        baseDAO = new DAO();
    }

    public List get(Class clazz, String order) {

        List objects = values.get(clazz);
        if (objects == null || objects.isEmpty() || reload) {
            List<Restriction> restrictions = null;
            ClassModel classModel = getClassModel().get(clazz);
            if (classModel != null) {
                restrictions = classModel.getRestrictions();
            }
            objects = baseDAO.list(clazz, restrictions, order);
            values.put(clazz, objects);
        }

        return objects;
    }

    public List getFromComponent(UIComponent component) {
        ValueExpression valueExpression = component.getValueExpression("value");
        Class type = valueExpression.getExpectedType();
        return get(type);
    }

    public List get(Class clazz) {
        if (clazz.isEnum()) {
            return Arrays.asList(clazz.getEnumConstants());
        }
        if (getClassModel() != null) {
            ClassModel classModel = getClassModel().get(clazz);
            if (classModel != null) {
                return get(clazz, classModel.getOrder());
            } else {
                return get(clazz, null);
            }
        } else {
            return get(clazz, null);
        }
    }

    public SelectItem[] getSelect(Class clazz) {

        List objects = get(clazz);
        ClassModel classModel = getClassModel().get(clazz);
        SelectItem[] options = new SelectItem[objects.size() + 1];

        Integer count = 1;
        boolean isEnum = clazz.isEnum();
        try {
            options[0] = new SelectItem("", "");
            for (Object bean : objects) {
                String itemLabel = getItemLabel(classModel, bean);
                if (!isEnum) {
                    Object id = PropertyUtils.getProperty(bean, EntityUtils.getIdFieldName(clazz));
                    options[count] = new SelectItem(id, itemLabel);
                } else {
                    options[count] = new SelectItem(((Enum) bean).name(), itemLabel);
                }

                count++;
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error converting class: {0}: {1}", new Object[]{clazz, ex.getMessage()});
        }
        return options;
    }

    private String getItemLabel(ClassModel classModel, Object bean) throws Exception {
        if (classModel == null) {
            return bean.toString();
        }
        //ClassModel itemLabel null then use order
        if (classModel.getItemLabel() != null && !classModel.getItemLabel().isEmpty()) {
            return (String) PropertyUtils.getProperty(bean, classModel.getItemLabel());
        } else if (classModel.getOrder() != null && !classModel.getOrder().isEmpty()) {
            return (String) PropertyUtils.getProperty(bean, classModel.getOrder());
        } else {
            return bean.toString();
        }
    }

    public boolean isReload() {
        return reload;
    }

    public void setReload(boolean reload) {
        this.reload = reload;
    }
}
