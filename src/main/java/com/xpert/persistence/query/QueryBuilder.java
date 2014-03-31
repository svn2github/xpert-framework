package com.xpert.persistence.query;

import com.xpert.i18n.I18N;
import com.xpert.persistence.exception.QueryFileNotFoundException;
import com.xpert.utils.StringUtils;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author Ayslan
 */
public class QueryBuilder {

  
    private String orderBy;
    private String attributes;
    /**
     * to be used in SUM, MAX, MIN, COUNT
     */
    private String atrribute;
    private Class from;
    private String alias;
    private StringBuilder joins = new StringBuilder();
    private List<Restriction> restrictions = new ArrayList<Restriction>();
    private List<Restriction> normalizedRestrictions = new ArrayList<Restriction>();
    private QueryType type;
    private EntityManager entityManager;
    private static final boolean DEBUG = true;
    private static final Logger logger = Logger.getLogger(QueryBuilder.class.getName());

    public QueryBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public QueryBuilder(EntityManager entityManager, String alias) {
        this.entityManager = entityManager;
        this.alias = alias;
    }

    public QueryBuilder from(Class from) {
        this.from = from;
        return this;
    }

    public QueryBuilder from(Class from, String alias) {
        this.from = from;
        this.alias = alias;
        return this;
    }

    public QueryBuilder selectDistinct(String attributes) {
        this.attributes = "DISTINCT " + attributes;
        return this;
    }

    public QueryBuilder select(String attributes) {
        this.attributes = attributes;
        return this;
    }

    public QueryBuilder leftJoin(String join) {
        this.joins.append("LEFT JOIN ").append(join).append(" ");
        return this;
    }

    public QueryBuilder innerJoin(String join) {
        this.joins.append("INNER JOIN ").append(join).append(" ");
        return this;
    }

    public QueryBuilder join(String join) {
        this.joins.append("JOIN ").append(join).append(" ");
        return this;
    }

    public QueryBuilder leftJoinFetch(String join) {
        this.joins.append("LEFT JOIN FETCH ").append(join).append(" ");
        return this;
    }

    public QueryBuilder innerJoinFetch(String join) {
        this.joins.append("INNER JOIN FETCH ").append(join).append(" ");
        return this;
    }

    public QueryBuilder joinFetch(String join) {
        this.joins.append("JOIN FETCH ").append(join).append(" ");
        return this;
    }

    public QueryBuilder join(JoinBuilder joinBuilder) {
        if (joinBuilder != null) {
            this.joins.append(joinBuilder);
        }
        return this;
    }

    public QueryBuilder orderBy(String order) {
        this.orderBy = order;
        return this;
    }

    public static String getQuerySelectClausule(QueryType type, String select) {

        if (type == null) {
            return "";
        }

        StringBuilder queryString = new StringBuilder();

        if (type.equals(QueryType.COUNT)) {
            queryString.append("SELECT COUNT(*) ");
        } else if (type.equals(QueryType.MAX)) {
            queryString.append("SELECT MAX(").append(select).append(") ");
        } else if (type.equals(QueryType.MIN)) {
            queryString.append("SELECT MIN(").append(select).append(") ");
        } else if (type.equals(QueryType.SUM)) {
            queryString.append("SELECT SUM(").append(select).append(") ");
        } else if (type.equals(QueryType.SELECT) && (select != null && !select.isEmpty())) {
            queryString.append("SELECT ").append(select).append(" ");
        }

        return queryString.toString();

    }

    private void loadNormalizedRestrictions() {
        normalizedRestrictions = RestrictionsNormalizer.getNormalizedRestrictions(from, restrictions, alias);
    }

    /**
     * @param restrictions
     * @param alias
     * @return String of the part after "WHERE" from JPQL generated
     */
    public static String getQueryStringFromRestrictions(List<Restriction> restrictions) {

        int currentParameter = 1;
        StringBuilder queryString = new StringBuilder();
        boolean processPropertyAndValue = false;
        Restriction lastRestriction = null;

        for (Restriction restriction : restrictions) {

            if (lastRestriction != null) {
                if (lastRestriction.getRestrictionType().equals(RestrictionType.OR)) {
                    queryString.append(" OR ");
                    processPropertyAndValue = true;
                } else {
                    if (!lastRestriction.getRestrictionType().equals(RestrictionType.START_GROUP)
                            && !restriction.getRestrictionType().equals(RestrictionType.OR)
                            && !restriction.getRestrictionType().equals(RestrictionType.END_GROUP)) {
                        queryString.append(" AND ");
                    }
                    processPropertyAndValue = true;
                }
            }

            if (restriction.getRestrictionType().equals(RestrictionType.START_GROUP)
                    || restriction.getRestrictionType().equals(RestrictionType.END_GROUP)) {
                queryString.append(restriction.getRestrictionType().getSymbol());
                processPropertyAndValue = false;
            } else if (restriction.getRestrictionType().equals(RestrictionType.OR)) {
                processPropertyAndValue = false;
            } else {
                processPropertyAndValue = true;
            }

            lastRestriction = restriction;

            if (processPropertyAndValue) {
                String propertyName = restriction.getProperty();
                //custom query string
                if (restriction.getRestrictionType().equals(RestrictionType.QUERY_STRING)) {
                    queryString.append(" (").append(restriction.getProperty()).append(") ");
                } else {
                    if (restriction.getRestrictionType().equals(RestrictionType.LIKE) || restriction.getRestrictionType().equals(RestrictionType.NOT_LIKE)) {
                        queryString.append("UPPER(").append(propertyName).append(")").append(" ");
                    } else if (restriction.getTemporalType() != null && restriction.getTemporalType().equals(TemporalType.DATE)) {
                        queryString.append("CAST(").append(propertyName).append(" AS date)").append(" ");
                    } else {
                        queryString.append(propertyName);
                    }
                    //if Value is null set default to IS NULL
                    if (restriction.getValue() == null) {
                        //  EQUALS null or IS_NULL
                        if (restriction.getRestrictionType().equals(RestrictionType.EQUALS) || restriction.getRestrictionType().equals(RestrictionType.NULL)) {
                            queryString.append(" IS NULL ");
                        } else if (restriction.getRestrictionType().equals(RestrictionType.NOT_NULL)) {
                            queryString.append(" IS NOT NULL ");
                        }
                    } else {
                        queryString.append(" ").append(restriction.getRestrictionType().getSymbol()).append(" ");
                        if (restriction.getRestrictionType().equals(RestrictionType.LIKE) || restriction.getRestrictionType().equals(RestrictionType.NOT_LIKE)) {
                            queryString.append("UPPER(?").append(currentParameter).append(")");
                        } else if (restriction.getRestrictionType().equals(RestrictionType.IN) || restriction.getRestrictionType().equals(RestrictionType.NOT_IN)) {
                            queryString.append("(?").append(currentParameter).append(")");
                        } else {
                            queryString.append("?").append(currentParameter);
                        }
                        currentParameter++;
                    }
                }
            }
        }

        return queryString.toString();
    }

    /**
     * @return The complete String of JPQL generated in this QueryBuilder
     */
    public String getQueryString() {

        StringBuilder queryString = new StringBuilder();

        if (type == null) {
            type = QueryType.SELECT;
        }

        if (type.equals(QueryType.SELECT)) {
            queryString.append(QueryBuilder.getQuerySelectClausule(type, attributes));
        } else {
            queryString.append(QueryBuilder.getQuerySelectClausule(type, atrribute));
        }

        queryString.append("FROM ").append(from.getName()).append(" ");

        if (alias != null) {
            queryString.append(alias).append(" ");
        }

        if (joins != null && joins.length() > 0) {
            queryString.append(joins).append(" ");
        }

        //normalize
        loadNormalizedRestrictions();

        //where clausule
        if (normalizedRestrictions != null && !normalizedRestrictions.isEmpty()) {
            queryString.append("WHERE ");
        }

        //restrictions
        queryString.append(QueryBuilder.getQueryStringFromRestrictions(normalizedRestrictions));

        //order by
        if (type.equals(QueryType.SELECT) && orderBy != null && !orderBy.trim().isEmpty()) {
            queryString.append(" ORDER BY ").append(orderBy).toString();
        }

        return queryString.toString();
    }

    public Query createQuery() {
        return createQuery(null);
    }

    public Query createQuery(Integer maxResults) {
        return createQuery(null, maxResults);
    }

    public List<QueryParameter> getQueryParameters() {
        int position = 1;
        List<QueryParameter> parameters = new ArrayList<QueryParameter>();
        for (Restriction re : normalizedRestrictions) {
            if (re.getRestrictionType().isIgnoreParameter()) {
                continue;
            }
            if (re.getValue() != null) {
                QueryParameter parameter = null;
                if (re.getRestrictionType().equals(RestrictionType.LIKE)) {
                    if (re.getLikeType() == null || re.getLikeType().equals(LikeType.BOTH)) {
                        parameter = new QueryParameter(position, "%" + re.getValue() + "%");
                    } else if (re.getLikeType().equals(LikeType.BEGIN)) {
                        parameter = new QueryParameter(position, re.getValue() + "%");
                    } else if (re.getLikeType().equals(LikeType.END)) {
                        parameter = new QueryParameter(position, "%" + re.getValue());
                    }
                } else {
                    if (re.getTemporalType() != null && (re.getValue() instanceof Date || re.getValue() instanceof Calendar)) {
                        parameter = new QueryParameter(position, re.getValue(), re.getTemporalType());
                    } else {
                        parameter = new QueryParameter(position, re.getValue());
                    }
                }
                parameters.add(parameter);
                position++;
            }
        }
        return parameters;
    }

    public Long count() {
        type = QueryType.COUNT;
        return (Long) createQuery().getSingleResult();
    }

    public Number sum(String property) {
        type = QueryType.SUM;
        atrribute = property;
        return (Number) createQuery().getSingleResult();
    }

    public Object max(String property) {
        type = QueryType.MAX;
        atrribute = property;
        return (Number) createQuery().getSingleResult();
    }

    public Object min(String property) {
        type = QueryType.MIN;
        atrribute = property;
        return (Number) createQuery().getSingleResult();
    }

    public Query createQuery(Integer firstResult, Integer maxResults) {

        String queryString = getQueryString();

        if (DEBUG == true) {
            logger.log(Level.INFO, "Query String: {0}", queryString);
        }

        Query query = entityManager.createQuery(queryString);

        List<QueryParameter> parameters = getQueryParameters();
        for (QueryParameter parameter : parameters) {
            if (parameter.getTemporalType() != null && (parameter.getValue() instanceof Date || parameter.getValue() instanceof Calendar)) {
                if (parameter.getValue() instanceof Date) {
                    query.setParameter(parameter.getPosition(), (Date) parameter.getValue(), parameter.getTemporalType());
                } else if (parameter.getValue() instanceof Calendar) {
                    query.setParameter(parameter.getPosition(), (Calendar) parameter.getValue(), parameter.getTemporalType());
                }
            } else {
                query.setParameter(parameter.getPosition(), parameter.getValue());
            }
        }

        if (maxResults != null) {
            query.setMaxResults(maxResults);
        }
        if (firstResult != null) {
            query.setFirstResult(firstResult);
        }
        return query;
    }

    /**
     * @return entityManager.getSigleResult(), returns null when
     * NoResultExceptionis throw
     */
    public Object getSigleResult() {
        return this.getSigleResult(null);
    }

    /**
     * @param maxResults
     * @return entityManager.getSigleResult(), returns null when
     * NoResultExceptionis throw
     */
    public Object getSigleResult(Integer maxResults) {
        try {
            type = QueryType.SELECT;
            return this.createQuery(maxResults).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     * @param <T> Result Type
     * @return entityManager.getResultList()
     */
    public <T> List<T> getResultList() {
        return getResultList(null, null, null);
    }

    /**
     * @param <T> Result Type
     * @param expectedType The expected type in result
     * @return entityManager.getResultList()
     */
    public <T> List<T> getResultList(Class expectedType) {
        return getResultList(null, null, expectedType);
    }

    /**
     * @param <T> Result Type
     * @param maxResults Max results in query
     * (entityManager.setMaxResult(maxResults))
     * @return entityManager.getResultList()
     */
    public <T> List<T> getResultList(Integer maxResults) {
        return getResultList(null, maxResults);
    }

    /**
     *
     * @param <T> Result Type
     * @param maxResults Max results in query
     * (entityManager.setMaxResult(maxResults))
     * @param expectedType The expected type in result
     * @return entityManager.getResultList()
     */
    public <T> List<T> getResultList(Integer maxResults, Class expectedType) {
        return getResultList(null, maxResults, expectedType);
    }

    /**
     * @param <T> Result Type
     * @param firstResult First results in query
     * (entityManager.setFirstResult(maxResults))
     * @param maxResults Max results in query
     * (entityManager.setMaxResult(maxResults))
     * @return entityManager.getResultList()
     */
    public <T> List<T> getResultList(Integer firstResult, Integer maxResults) {
        return getResultList(firstResult, maxResults, null);
    }

    /**
     *
     * @param <T> Result Type
     * @param firstResult First results in query
     * (entityManager.setFirstResult(maxResults))
     * @param maxResults Max results in query
     * (entityManager.setMaxResult(maxResults))
     * @param expectedType The expected type in result
     * @return
     */
    public <T> List<T> getResultList(Integer firstResult, Integer maxResults, Class expectedType) {
        type = QueryType.SELECT;
        List list = this.createQuery(firstResult, maxResults).getResultList();
        if (list != null && attributes != null && !attributes.trim().isEmpty() && expectedType != null) {
            return getNormalizedResultList(attributes, list, expectedType);
        }
        return list;
    }

    /**
     * @param <T> Result Type
     * @param attributes Attributes to select
     * @param resultList The Query Result List
     * @param clazz The expected type in result
     * @return
     */
    public static <T> List<T> getNormalizedResultList(String attributes, List resultList, Class<T> clazz) {
        if (attributes != null && attributes.split(",").length > 0) {
            List result = new ArrayList();
            String[] fields = attributes.split(",");
            for (Object object : resultList) {
                try {
                    Object entity = clazz.newInstance();
                    for (int i = 0; i < fields.length; i++) {
                        String property = fields[i].trim().replaceAll("/s", "");
                        initializeCascade(property, entity);
                        if (object instanceof Object[]) {
                            PropertyUtils.setProperty(entity, property, ((Object[]) object)[i]);
                        } else {
                            PropertyUtils.setProperty(entity, property, object);
                        }
                    }
                    result.add(entity);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
            return result;
        }
        return resultList;
    }

    public static void initializeCascade(String property, Object bean) {
        int index = property.indexOf(".");
        if (index > -1) {
            try {
                String field = property.substring(0, property.indexOf("."));
                Object propertyToInitialize = PropertyUtils.getProperty(bean, field);
                if (propertyToInitialize == null) {
                    propertyToInitialize = PropertyUtils.getPropertyDescriptor(bean, field).getPropertyType().newInstance();
                    PropertyUtils.setProperty(bean, field, propertyToInitialize);
                }
                String afterField = property.substring(index + 1, property.length());
                if (afterField != null && afterField.indexOf(".") > -1) {
                    initializeCascade(afterField, propertyToInitialize);
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    public static Query createNativeQueryFromFile(EntityManager entityManager, String queryPath, Class daoClass) {
        return createNativeQueryFromFile(entityManager, queryPath, daoClass, null);
    }

    public static Query createNativeQueryFromFile(EntityManager entityManager, String queryPath, Class daoClass, Class resultClass) {

        InputStream inputStream = daoClass.getResourceAsStream(queryPath);
        if (inputStream == null) {
            throw new QueryFileNotFoundException("Query File not found: " + queryPath + " in package: " + daoClass.getPackage());
        }
        try {
            String queryString = readInputStreamAsString(inputStream);
            if (resultClass != null) {
                return entityManager.createNativeQuery(queryString, resultClass);
            } else {
                return entityManager.createNativeQuery(queryString);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    public static String readInputStreamAsString(InputStream inputStream)
            throws IOException {

        BufferedInputStream bis = new BufferedInputStream(inputStream);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1) {
            byte b = (byte) result;
            buf.write(b);
            result = bis.read();
        }
        bis.close();
        buf.close();
        return buf.toString();
    }

    public List<Restriction> getNormalizedRestrictions() {
        return normalizedRestrictions;
    }

    /**
     * Add a restriction map
     *
     * @param restrictions
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder add(Map<String, Object> restrictions) {
        if (restrictions != null) {
            for (Map.Entry e : restrictions.entrySet()) {
                this.restrictions.add(new Restriction(e.getKey().toString(), e.getValue()));
            }
        }
        return this;
    }

    /**
     * Add a restriction list
     *
     * @param restrictions
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder add(List<Restriction> restrictions) {
        if (restrictions != null) {
            this.restrictions.addAll(restrictions);
        }
        return this;
    }

    /**
     * Add a restriction
     *
     * @param restriction
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder add(Restriction restriction) {
        if (restriction != null) {
            this.restrictions.add(restriction);
        }
        return this;
    }

    /**
     * Add a restriction
     *
     * @param property
     * @param restrictionType
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder add(String property, RestrictionType restrictionType) {
        this.add(new Restriction(property, restrictionType));
        return this;
    }

    /**
     * Add a RestrictionType.EQUALS
     *
     * @param property
     * @param value
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder add(String property, Object value) {
        this.add(new Restriction(property, value));
        return this;
    }

    /**
     * Add a Date restriction
     *
     * @param property
     * @param restrictionType
     * @param value
     * @param temporalType
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder add(String property, RestrictionType restrictionType, Date value, TemporalType temporalType) {
        this.add(new Restriction(property, restrictionType, value, temporalType));
        return this;
    }

    /**
     * Add a Calendar restriction
     *
     * @param property
     * @param restrictionType
     * @param value
     * @param temporalType
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder add(String property, RestrictionType restrictionType, Calendar value, TemporalType temporalType) {
        this.add(new Restriction(property, restrictionType, value, temporalType));
        return this;
    }

    /**
     * Add a restriction
     *
     * @param property
     * @param restrictionType
     * @param value
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder add(String property, RestrictionType restrictionType, Object value) {
        this.add(new Restriction(property, restrictionType, value));
        return this;
    }

    /**
     * Add a restriction
     *
     * @param property
     * @param restrictionType
     * @param value
     * @param likeType
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder add(String property, RestrictionType restrictionType, Object value, LikeType likeType) {
        this.add(new Restriction(property, restrictionType, value, likeType));
        return this;
    }

    /**
     * Add a RestrictionType.QUERY_STRING
     *
     * @param property
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder addQueryString(String property) {
        this.add(new Restriction(property, RestrictionType.QUERY_STRING));
        return this;
    }

    /**
     * Add a RestrictionType.NULL (property 'is null')
     *
     * @param property
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder isNull(String property) {
        this.add(new Restriction(property, RestrictionType.NULL));
        return this;
    }

    /**
     * Add a RestrictionType.NOT_NULL (property 'is not null')
     *
     * @param property
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder isNotNull(String property) {
        this.add(new Restriction(property, RestrictionType.NOT_NULL));
        return this;
    }

    /**
     * Add a RestrictionType.LIKE (property 'like' value)
     *
     * @param property
     * @param value
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder like(String property, Object value) {
        this.add(new Restriction(property, RestrictionType.LIKE, value));
        return this;
    }

    /**
     * Add a RestrictionType.LIKE (property 'like' value)
     *
     * @param property
     * @param value
     * @param likeType
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder like(String property, Object value, LikeType likeType) {
        this.add(new Restriction(property, RestrictionType.LIKE, value, likeType));
        return this;
    }

    /**
     * Add a RestrictionType.NOT_LIKE (property 'not like' value)
     *
     * @param property
     * @param value
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder notLike(String property, Object value) {
        this.add(new Restriction(property, RestrictionType.NOT_LIKE, value));
        return this;
    }

    /**
     * Add a RestrictionType.NOT_LIKE (property 'not like' value)
     *
     * @param property
     * @param value
     * @param likeType
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder notLike(String property, Object value, LikeType likeType) {
        this.add(new Restriction(property, RestrictionType.NOT_LIKE, value, likeType));
        return this;
    }

    /**
     * Add a RestrictionType.GREATER_THAN (property '>' value)
     *
     * @param property
     * @param value
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder greaterThan(String property, Object value) {
        this.add(new Restriction(property, RestrictionType.GREATER_THAN, value));
        return this;
    }

    /**
     * Add a Date RestrictionType.GREATER_THAN (property '>' value)
     *
     * @param property
     * @param value
     * @param temporalType
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder greaterThan(String property, Date value, TemporalType temporalType) {
        this.add(new Restriction(property, RestrictionType.GREATER_THAN, value, temporalType));
        return this;
    }

    /**
     * Add a Calendar RestrictionType.GREATER_THAN (property '>' value)
     *
     * @param property
     * @param value
     * @param temporalType
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder greaterThan(String property, Calendar value, TemporalType temporalType) {
        this.add(new Restriction(property, RestrictionType.GREATER_THAN, value, temporalType));
        return this;
    }

    /**
     * Add a RestrictionType.GREATER_EQUALS_THAN (property '>=' value)
     *
     * @param property
     * @param value
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder greaterEqualsThan(String property, Object value) {
        this.add(new Restriction(property, RestrictionType.GREATER_EQUALS_THAN, value));
        return this;
    }

    /**
     * Add a Date RestrictionType.GREATER_EQUALS_THAN (property '>=' value)
     *
     * @param property
     * @param value
     * @param temporalType
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder greaterEqualsThan(String property, Date value, TemporalType temporalType) {
        this.add(new Restriction(property, RestrictionType.GREATER_EQUALS_THAN, value, temporalType));
        return this;
    }

    /**
     * Add a Calendar RestrictionType.GREATER_EQUALS_THAN (property '>=' value)
     *
     * @param property
     * @param value
     * @param temporalType
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder greaterEqualsThan(String property, Calendar value, TemporalType temporalType) {
        this.add(new Restriction(property, RestrictionType.GREATER_EQUALS_THAN, value, temporalType));
        return this;
    }

    /**
     * Add a RestrictionType.LESS_THAN (property '&lt' value)
     *
     * @param property
     * @param value
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder lessThan(String property, Object value) {
        this.add(new Restriction(property, RestrictionType.LESS_THAN, value));
        return this;
    }

    /**
     * Add a Date RestrictionType.LESS_THAN (property '&lt' value)
     *
     * @param property
     * @param value
     * @param temporalType
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder lessThan(String property, Date value, TemporalType temporalType) {
        this.add(new Restriction(property, RestrictionType.LESS_THAN, value, temporalType));
        return this;
    }

    /**
     * Add a Calendar RestrictionType.LESS_THAN (property '&lt' value)
     *
     * @param property
     * @param value
     * @param temporalType
     * @return
     */
    public QueryBuilder lessThan(String property, Calendar value, TemporalType temporalType) {
        this.add(new Restriction(property, RestrictionType.LESS_THAN, value, temporalType));
        return this;
    }

    /**
     * Add a RestrictionType.LESS_EQUALS_THAN (property '&lt=' value)
     *
     * @param property
     * @param value
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder lessEqualsThan(String property, Object value) {
        this.add(new Restriction(property, RestrictionType.LESS_EQUALS_THAN, value));
        return this;
    }

    /**
     * Add a Date RestrictionType.LESS_EQUALS_THAN (property '&lt=' value)
     *
     * @param property
     * @param value
     * @param temporalType
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder lessEqualsThan(String property, Date value, TemporalType temporalType) {
        this.add(new Restriction(property, RestrictionType.LESS_EQUALS_THAN, value, temporalType));
        return this;
    }

    /**
     * Add a Calendar RestrictionType.LESS_EQUALS_THAN (property '&lt=' value)
     *
     * @param property
     * @param value
     * @param temporalType
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder lessEqualsThan(String property, Calendar value, TemporalType temporalType) {
        this.add(new Restriction(property, RestrictionType.LESS_EQUALS_THAN, value, temporalType));
        return this;
    }

    /**
     * Add a RestrictionType.IN (property 'in' value)
     *
     * @param property
     * @param value
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder in(String property, Object value) {
        this.add(new Restriction(property, RestrictionType.IN, value));
        return this;
    }

    /**
     * Add a RestrictionType.NOT_IN (property 'not in' value)
     *
     * @param property
     * @param value
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder notIn(String property, Object value) {
        this.add(new Restriction(property, RestrictionType.NOT_IN, value));
        return this;
    }

    /**
     * Add a RestrictionType.EQUALS (property '=' value)
     *
     * @param property
     * @param value
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder equals(String property, Object value) {
        this.add(new Restriction(property, RestrictionType.EQUALS, value));
        return this;
    }

    /**
     * Add a Date RestrictionType.EQUALS (property '=' value)
     *
     * @param property
     * @param value
     * @param temporalType
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder equals(String property, Date value, TemporalType temporalType) {
        this.add(new Restriction(property, RestrictionType.EQUALS, value, temporalType));
        return this;
    }

    /**
     * Add a Calendar RestrictionType.EQUALS (property '=' value)
     *
     * @param property
     * @param value
     * @param temporalType
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder equals(String property, Calendar value, TemporalType temporalType) {
        this.add(new Restriction(property, RestrictionType.EQUALS, value, temporalType));
        return this;
    }

    /**
     * Add a RestrictionType.NOT_EQUALS (property '!=' value)
     *
     * @param property
     * @param value
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder notEquals(String property, Object value) {
        this.add(new Restriction(property, RestrictionType.NOT_EQUALS, value));
        return this;
    }

    /**
     * Add a Date RestrictionType.NOT_EQUALS (property '!=' value)
     *
     * @param property
     * @param value
     * @param temporalType
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder notEquals(String property, Date value, TemporalType temporalType) {
        this.add(new Restriction(property, RestrictionType.NOT_EQUALS, value, temporalType));
        return this;
    }

    /**
     * Add a Calendar RestrictionType.NOT_EQUALS (property '!=' value)
     *
     * @param property
     * @param value
     * @param temporalType
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder notEquals(String property, Calendar value, TemporalType temporalType) {
        this.add(new Restriction(property, RestrictionType.NOT_EQUALS, value, temporalType));
        return this;
    }

    /**
     * Add a RestrictionType.OR
     *
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder or() {
        this.add(new Restriction(RestrictionType.OR));
        return this;
    }

    /**
     * Add a RestrictionType.START_GROUP
     *
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder startGroup() {
        this.add(new Restriction(RestrictionType.START_GROUP));
        return this;
    }

    /**
     * Add a RestrictionType.END_GROUP
     *
     * @return Current QueryBuilder with added restriction
     */
    public QueryBuilder endGroup() {
        this.add(new Restriction(RestrictionType.END_GROUP));
        return this;
    }

}
