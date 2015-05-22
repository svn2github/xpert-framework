package com.xpert.persistence.dao;

import com.xpert.audit.Audit;
import com.xpert.Configuration;
import com.xpert.persistence.exception.DeleteException;
import com.xpert.persistence.query.QueryBuilder;
import com.xpert.persistence.query.Restriction;
import com.xpert.persistence.utils.EntityUtils;
import com.xpert.utils.StringUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OrderBy;
import javax.persistence.Query;
import javax.validation.ConstraintViolationException;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.collection.internal.PersistentSet;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

public abstract class BaseDAOImpl<T> implements BaseDAO<T> {

    private Class entityClass;
    private static final Logger logger = Logger.getLogger(BaseDAOImpl.class.getName());
    private static final Map<ClassField, String> ORDER_BY_MAP = new HashMap<ClassField, String>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public BaseDAOImpl() {
        try {
            Type genericSuperclass = getClass().getGenericSuperclass();
            if (genericSuperclass != null && genericSuperclass instanceof ParameterizedType) {
                Type[] arguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
                if (arguments != null && arguments.length > 0) {
                    Object object = arguments[0];
                    if (object instanceof Class<?>) {
                        entityClass = (Class<T>) object;
                    } else {
                        if (object instanceof Class) {
                            entityClass = (Class) object;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public Class getEntityClass() {
        return entityClass;
    }

    @Override
    public void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public abstract EntityManager getEntityManager();

    @Override
    public Connection getConnection() throws SQLException {
        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) getSession().getSessionFactory();
        return sessionFactoryImpl.getConnectionProvider().getConnection();
    }

    @Override
    public Session getSession() {
        return getEntityManager().unwrap(Session.class);
    }

    @Override
    public QueryBuilder getQueryBuilder() {
        return new QueryBuilder(getEntityManager());
    }

    @Override
    public Query getNativeQueryFromFile(String path, Class daoClass, Class resultClass) {
        return QueryBuilder.createNativeQueryFromFile(getEntityManager(), path, daoClass, resultClass);
    }

    @Override
    public Query getNativeQueryFromFile(String path, Class daoClass) {
        return QueryBuilder.createNativeQueryFromFile(getEntityManager(), path, daoClass);
    }

    private Audit getNewAudit() {
        return new Audit(getEntityManager());
    }

    @Override
    public void save(T object) {
        save(object, Configuration.isAudit());
    }

    @Override
    public void save(T object, boolean audit) {
        getEntityManager().persist(object);
        if (audit) {
            getNewAudit().insert(object);
        }
    }

    @Override
    public void update(T object) {
        update(object, Configuration.isAudit());
    }

    @Override
    public void update(T object, boolean audit) {
        if (audit) {
            getNewAudit().update(object);
        }
        getSession().update(object);
    }

    @Override
    public void saveOrUpdate(T object) {
        saveOrUpdate(object, Configuration.isAudit());
    }

    @Override
    public void saveOrUpdate(T object, boolean audit) {
        boolean persisted = EntityUtils.isPersisted(object);
        if (persisted && audit) {
            getNewAudit().update(object);
        }
        getSession().saveOrUpdate(object);

        if (!persisted && audit) {
            getNewAudit().insert(object);
        }
    }

    @Override
    public void saveOrMerge(T object) {
        saveOrMerge(object, Configuration.isAudit());
    }

    @Override
    public void saveOrMerge(T object, boolean audit) {
        boolean persisted = EntityUtils.isPersisted(object);
        if (persisted == false) {
            getEntityManager().persist(object);
            if (audit) {
                getNewAudit().insert(object);
            }
        } else {
            if (audit) {
                getNewAudit().update(object);
            }
            getEntityManager().merge(object);
        }
    }

    @Override
    public T merge(T object) {
        return merge(object, Configuration.isAudit());
    }

    @Override
    public T merge(T object, boolean audit) {
        boolean persisted = EntityUtils.getId(object) != null;
        if (persisted && audit) {
            getNewAudit().update(object);
        }

        object = (T) getEntityManager().merge(object);

        if (!persisted && audit) {
            getNewAudit().insert(object);
        }
        return object;
    }

    @Override
    public void delete(Object id) throws DeleteException {
        delete(id, Configuration.isAudit());
    }

    @Override
    public void delete(Object id, boolean audit) throws DeleteException {

        try {

            if (audit) {
                getNewAudit().delete(id, getEntityClass());
            }

            Query query = getEntityManager().createQuery("DELETE FROM " + getEntityClass().getName() + " WHERE " + EntityUtils.getIdFieldName(getEntityClass()) + " = ?1 ");
            query.setParameter(1, id);
            query.executeUpdate();
        } catch (Exception ex) {
            if (ex instanceof ConstraintViolationException || ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                throw new DeleteException("Object from class " + getEntityClass() + " with ID: " + id + " cannot be deleted");
            } else {
                throw new RuntimeException(ex);
            }
        }

    }

    @Override
    public void remove(Object object) throws DeleteException {
        remove(object, Configuration.isAudit());
    }

    @Override
    public void remove(Object object, boolean audit) throws DeleteException {
        try {
            if (!getEntityManager().contains(object)) {
                object = getEntityManager().merge(object);
            }
            if (audit) {
                getNewAudit().delete(object);
            }
            getEntityManager().remove(object);
            getEntityManager().flush();
        } catch (Exception ex) {
            if (ex instanceof ConstraintViolationException || ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                throw new DeleteException("Object from class " + getEntityClass() + " with ID: " + EntityUtils.getId(object) + " cannot be deleted");
            } else {
                throw new RuntimeException(ex);
            }
        }

    }

    @Override
    public T find(Object id) {
        return (T) getEntityManager().find(getEntityClass(), id);
    }

    @Override
    public T find(Class entityClass, Object id) {
        return (T) getEntityManager().find(entityClass, id);
    }

    @Override
    public List<T> listAll() {
        return listAll(null);
    }

    @Override
    public List<T> listAll(String order) {
        return listAll(getEntityClass(), order);
    }

    @Override
    public List<T> listAll(Class clazz, String order) {
        Query query = getQueryBuilder().from(clazz).orderBy(order).createQuery();
        return query.getResultList();
    }

    @Override
    public Object findAttribute(String attributeName, Number id) {

        QueryBuilder builder = getQueryBuilder();

        return builder.select("o." + attributeName)
                .from(getEntityClass(), "o")
                .add("o." + EntityUtils.getIdFieldName(getEntityClass()), id)
                .getSigleResult();

    }

    @Override
    public Object findAttribute(String attributeName, Object object) {
        return findAttribute(attributeName, (Number) EntityUtils.getId(object));
    }

    @Override
    public Object findList(String attributeName, Number id) {

        QueryBuilder builder = getQueryBuilder();

        return builder.select("o." + attributeName)
                .from(getEntityClass(), "o")
                .add("o." + EntityUtils.getIdFieldName(getEntityClass()), id)
                .getResultList();

    }

    @Override
    public Object findList(String attributeName, Object object) {
        return findAttribute(attributeName, (Number) EntityUtils.getId(object));
    }

    @Override
    public T unique(Map<String, Object> args) {
        Query query = getQueryBuilder().from(getEntityClass()).add(args).createQuery().setMaxResults(1);
        try {
            return (T) query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public T unique(Restriction restriction) {
        return unique(getRestrictions(restriction), getEntityClass());
    }

    @Override
    public T unique(List<Restriction> restrictions) {
        return unique(restrictions, getEntityClass());
    }

    @Override
    public T unique(Restriction restrictions, Class clazz) {
        return unique(getRestrictions(restrictions), clazz);
    }

    @Override
    public T unique(String property, Object value) {
        return unique(new Restriction(property, value), getEntityClass());
    }

    @Override
    public T unique(List<Restriction> restrictions, Class clazz) {
        Query query = getQueryBuilder().from(clazz).add(restrictions).createQuery();
        query.setMaxResults(1);
        try {
            return (T) query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public List<T> list(Map<String, Object> restrictions, String order) {
        return list(restrictions, order, null, null);
    }

    @Override
    public List<T> list(Map<String, Object> args) {
        return list(args, null);
    }

    @Override
    public List<T> list(Map<String, Object> restrictions, String order, Integer firstResult, Integer maxResults) {
        Query query = getQueryBuilder().from(getEntityClass()).add(restrictions).orderBy(order).createQuery();

        if (firstResult != null) {
            query.setFirstResult(firstResult);
        }
        if (maxResults != null) {
            query.setMaxResults(maxResults);
        }

        return query.getResultList();
    }

    @Override
    public List<T> list(Class clazz, List<Restriction> restrictions) {
        return list(clazz, restrictions, null);
    }

    @Override
    public List<T> list(List<Restriction> restrictions) {
        return list(getEntityClass(), restrictions);
    }

    @Override
    public List<T> list(Class clazz, Restriction restriction) {
        return list(clazz, getRestrictions(restriction), null);
    }

    @Override
    public List<T> list(String property, Object value) {
        return list(getEntityClass(), new Restriction(property, value));
    }

    @Override
    public List<T> list(String property, Object value, String order) {
        return list(getEntityClass(), new Restriction(property, value), order);
    }

    @Override
    public List<T> list(Restriction restriction) {
        return list(getEntityClass(), getRestrictions(restriction));
    }

    @Override
    public List<T> listAttributes(String attributes) {
        return listAttributes(attributes, null);
    }

    @Override
    public List<T> listAttributes(String attributes, String order) {
        return list(getEntityClass(), (List) null, order, null, null, attributes);
    }

    @Override
    public List<T> listAttributes(Map<String, Object> args, String attributes, String order) {
        return list(getEntityClass(), getRestrictionsFromMap(args), order, null, null, attributes);
    }

    @Override
    public List<T> listAttributes(Map<String, Object> args, String attributes) {
        return list(getEntityClass(), getRestrictionsFromMap(args), null, null, null, attributes);
    }

    @Override
    public List<T> listAttributes(List<Restriction> restrictions, String attributes, String order) {
        return list(getEntityClass(), restrictions, order, null, null, attributes);
    }

    @Override
    public List<T> listAttributes(List<Restriction> restrictions, String attributes) {
        return list(getEntityClass(), restrictions, null, null, null, attributes);
    }

    @Override
    public List<T> listAttributes(Restriction restriction, String attributes, String order) {
        return list(getEntityClass(), getRestrictions(restriction), order, null, null, attributes);
    }

    @Override
    public List<T> listAttributes(Restriction restriction, String attributes) {
        return list(getEntityClass(), getRestrictions(restriction), null, null, null, attributes);
    }

    public List listAttributes(String property, Object value, String attributes) {
        return listAttributes(new Restriction(property, value), attributes);
    }

    public List listAttributes(String property, Object value, String attributes, String order) {
        return listAttributes(new Restriction(property, value), attributes, order);
    }

    @Override
    public List<T> list(List<Restriction> restrictions, String order, Integer firstResult, Integer maxResults) {
        return list(getEntityClass(), restrictions, order, firstResult, maxResults);
    }

    @Override
    public List<T> list(Class clazz, List<Restriction> restrictions, String order, Integer firstResult, Integer maxResults) {
        return list(clazz, restrictions, order, firstResult, maxResults, null);
    }

    @Override
    public List<T> list(Restriction restriction, String order, Integer firstResult, Integer maxResults) {
        return list(getEntityClass(), getRestrictions(restriction), order, firstResult, maxResults);
    }

    @Override
    public List<T> list(Class clazz, Restriction restriction, String order, Integer firstResult, Integer maxResults) {
        return list(clazz, getRestrictions(restriction), order, firstResult, maxResults, null);
    }

    @Override
    public List<T> list(List<Restriction> restrictions, String order) {
        return list(getEntityClass(), restrictions, order);
    }

    @Override
    public List<T> list(Class clazz, List<Restriction> restrictions, String order) {
        return list(clazz, restrictions, order, null, null);
    }

    @Override
    public List<T> list(Restriction restriction, String order) {
        return list(getEntityClass(), getRestrictions(restriction), order);
    }

    @Override
    public List<T> list(Class clazz, Restriction restriction, String order) {
        return list(clazz, getRestrictions(restriction), order, null, null);
    }

    @Override
    public List<T> list(Class clazz, Restriction restriction, String order, Integer firstResult, Integer maxResults, String attributes) {
        return list(clazz, getRestrictions(restriction), order, firstResult, maxResults, attributes);
    }

    @Override
    public List<T> list(Class clazz, List<Restriction> restrictions, String order, Integer firstResult, Integer maxResults, String attributes) {

        return getQueryBuilder()
                .select(attributes)
                .from(clazz)
                .add(restrictions)
                .orderBy(order)
                .setFirstResult(firstResult)
                .setMaxResults(maxResults)
                .getResultList(clazz);

    }

    @Override
    public Long count(Map<String, Object> restrictions) {
        return getQueryBuilder().from(getEntityClass()).add(restrictions).count();
    }

    @Override
    public Long count(String property, Object value) {
        return count(new Restriction(property, value));
    }

    @Override
    public Long count(Restriction restriction) {
        return count(getRestrictions(restriction));
    }

    @Override
    public Long count(List<Restriction> restrictions) {
        return getQueryBuilder().from(getEntityClass()).add(restrictions).count();
    }

    @Override
    public Long count() {
        return count(getEntityClass());
    }

    @Override
    public Long count(Class clazz) {
        return count((List) null);
    }

    @Override
    public <U> U getInitialized(U object) {
        if (object != null) {
            if (object instanceof HibernateProxy || object instanceof PersistentBag || object instanceof PersistentSet) {

                if (object instanceof HibernateProxy) {

                    LazyInitializer lazyInitializer = ((HibernateProxy) object).getHibernateLazyInitializer();

                    if (Hibernate.isInitialized(object)) {
                        return (U) lazyInitializer.getImplementation();
                    }
                    Object initilized = getEntityManager().find(lazyInitializer.getPersistentClass(), lazyInitializer.getIdentifier());
                    //if find returns a proxy, then call getImplementation() to return the real object
                    if (initilized instanceof HibernateProxy) {
                        lazyInitializer = ((HibernateProxy) initilized).getHibernateLazyInitializer();
                        return (U) lazyInitializer.getImplementation();
                    } else {
                        return (U) initilized;
                    }
                }

                if (object instanceof PersistentCollection) {

                    String role = ((PersistentCollection) object).getRole();
                    Object owner = ((PersistentCollection) object).getOwner();

                    Collection collection = null;
                    if (object instanceof PersistentBag) {
                        collection = new ArrayList();
                        if (Hibernate.isInitialized(object)) {
                            collection.addAll((PersistentBag) object);
                            return (U) collection;
                        }
                    }
                    if (object instanceof PersistentSet) {
                        collection = new HashSet();
                        if (Hibernate.isInitialized(object)) {
                            collection.addAll((PersistentSet) object);
                            return (U) collection;
                        }
                    }

                    String fieldName = role.substring(role.lastIndexOf(".") + 1, role.length());
                    String orderBy = null;

                    //generate "SELECT o FROM Object o JOIN o.itens c WHERE o = ?1 ORDER BY {something}"
                    StringBuilder queryString = new StringBuilder();
                    queryString.append(" SELECT ").append(" c ");
                    queryString.append(" FROM ").append(owner.getClass().getName()).append(" o ");
                    queryString.append(" JOIN ").append("o.").append(fieldName).append(" c ");
                    queryString.append(" WHERE o = ?1 ");

                    orderBy = getOrderBy(fieldName, owner.getClass());

                    if (orderBy != null && !orderBy.isEmpty()) {
                        //alias of attribute is "c"
                        queryString.append(" ORDER BY ").append(getOrderByWithAlias("c", orderBy));
                    }

                    Query query = getEntityManager().createQuery(queryString.toString());
                    query.setParameter(1, owner);

                    for (Object result : query.getResultList()) {
                        //prevent to add a proxy
                        if (result instanceof HibernateProxy) {
                            LazyInitializer lazyInitializer = ((HibernateProxy) result).getHibernateLazyInitializer();
                            collection.add(lazyInitializer.getImplementation());
                        } else {
                            collection.add(result);
                        }
                    }

                    return (U) collection;
                }

            }
        }
        return object;
    }

    public String getOrderByWithAlias(String alias, String orderBy) {
        if (orderBy == null || orderBy.isEmpty()) {
            return "";
        }
        String[] parts = orderBy.split(",");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(alias).append(parts[i].replace(" ", ""));

        }
        return builder.toString();
    }

    private String getOrderBy(String fieldName, Class entity) {
        String orderBy = null;

        //try find in cache
        ClassField classField = new ClassField(entity, fieldName);
        if (ORDER_BY_MAP.containsKey(classField)) {
            return ORDER_BY_MAP.get(classField);
        }

        try {
            Field field = getDeclaredField(entity, fieldName);
            OrderBy annotation = field.getAnnotation(OrderBy.class);
            orderBy = getOrderByFromAnnotaion(annotation, field, null);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        if (orderBy == null || orderBy.isEmpty()) {
            try {
                Method method = entity.getDeclaredMethod("get" + StringUtils.getUpperFirstLetter(fieldName));
                OrderBy annotation = method.getAnnotation(OrderBy.class);
                orderBy = getOrderByFromAnnotaion(annotation, null, method);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        ORDER_BY_MAP.put(new ClassField(entity, fieldName), orderBy);

        return orderBy;
    }

    private Field getDeclaredField(Class clazz, String fieldName) {
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            if (field == null && clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
                field = getDeclaredField(clazz.getSuperclass(), fieldName);
            }
        } catch (SecurityException ex) {
            throw new RuntimeException(ex);
        }

        return field;
    }

    private String getOrderByFromAnnotaion(OrderBy annotation, Field field, Method method) {
        String orderBy = null;
        if (annotation != null) {
            String value = annotation.value();
            if (value != null && !value.isEmpty()) {
                orderBy = value;
            } else {
                Type realType = null;
                if (field != null) {
                    if (field.getGenericType() != null && field.getGenericType() instanceof ParameterizedType) {
                        if (((ParameterizedType) field.getGenericType()).getActualTypeArguments().length > 0) {
                            realType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        }
                    }
                }
                if (method != null) {
                    if (method.getGenericReturnType() != null && method.getGenericReturnType() instanceof ParameterizedType) {
                        if (((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments().length > 0) {
                            realType = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
                        }
                    }
                }
                if (realType == null) {
                    return null;
                }
                orderBy = EntityUtils.getIdFieldName((Class) realType);
            }
        }
        return orderBy;
    }

    private List<Restriction> getRestrictionsFromMap(Map<String, Object> args) {
        List<Restriction> restrictions = new ArrayList<Restriction>();
        for (Entry e : args.entrySet()) {
            restrictions.add(new Restriction(e.getKey().toString(), e.getValue()));
        }
        return restrictions;
    }

    private List<Restriction> getRestrictions(Restriction restriction) {
        List<Restriction> restrictions = null;
        if (restriction != null) {
            restrictions = new ArrayList<Restriction>();
            restrictions.add(restriction);
        }
        return restrictions;
    }
}
