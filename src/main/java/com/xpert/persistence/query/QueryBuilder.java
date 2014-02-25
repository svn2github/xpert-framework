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
import javax.persistence.Query;
import javax.persistence.TemporalType;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author Ayslan
 */
public class QueryBuilder {

    private static final String DATE_FILTER_INTERVAL_SEPARATOR = " ## ";
    private String order;
    private String attributeName;
    private String select;
    private Class from;
    private String alias;
    private StringBuilder joins = new StringBuilder();
    private List<Restriction> restrictions = new ArrayList<Restriction>();
    private List<Restriction> normalizedRestrictions = new ArrayList<Restriction>();
    private QueryType type;
    private EntityManager entityManager;
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

    public QueryBuilder selectDistinct(String select) {
        this.select = "DISTINCT " + select;
        return this;
    }

    public QueryBuilder select(String select) {
        this.select = select;
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
            this.alias = joinBuilder.getAlias();
            this.joins.append(joinBuilder);
        }
        return this;
    }

    public QueryBuilder orderBy(String order) {
        this.order = order;
        return this;
    }

    public QueryBuilder type(QueryType type) {
        this.type = type;
        return this;
    }

    public QueryBuilder type(QueryType type, String attributeName) {
        this.type = type;
        this.attributeName = attributeName;
        return this;
    }

    public QueryBuilder add(List<Restriction> restrictions) {
        if (restrictions != null) {
            this.restrictions.addAll(restrictions);
        }
        return this;
    }

    public QueryBuilder add(Map<String, Object> restrictions) {
        if (restrictions != null) {
            for (Map.Entry e : restrictions.entrySet()) {
                this.restrictions.add(new Restriction(e.getKey().toString(), e.getValue()));
            }
        }
        return this;
    }

    public QueryBuilder add(Restriction restriction) {
        if (restriction != null) {
            this.restrictions.add(restriction);
        }
        return this;
    }

    public QueryBuilder add(String field, Object value) {
        this.restrictions.add(new Restriction(order, value));
        return this;
    }

    public String getQuerySelectClausule() {

        StringBuilder queryString = new StringBuilder();

        if (type == null) {
            type = QueryType.SELECT;
        }

        if (type.equals(QueryType.COUNT)) {
            queryString.append("SELECT COUNT(*) ");
        } else if (type.equals(QueryType.MAX)) {
            queryString.append("SELECT MAX(").append(alias).append(".").append(attributeName).append(") ");
        } else if (type.equals(QueryType.MIN)) {
            queryString.append("SELECT MIN(").append(alias).append(".").append(attributeName).append(") ");
        } else if (type.equals(QueryType.SUM)) {
            queryString.append("SELECT SUM(").append(alias).append(".").append(attributeName).append(") ");
        } else if (type.equals(QueryType.SELECT)
                && ((attributeName != null && !attributeName.isEmpty()) || (select != null && !select.isEmpty()))) {
            queryString.append("SELECT ");

            if (attributeName != null && !attributeName.isEmpty()) {
                queryString.append(attributeName).append(" ");
            }

            if (select != null && !select.isEmpty()) {
                queryString.append(select).append(" ");
            }
        }
        return queryString.toString();

    }

    public void loadNormalizedRestrictions() {

        normalizedRestrictions = new ArrayList<Restriction>();
        //normalize result
        for (Restriction originalRestriction : restrictions) {
            boolean ignoreRestriction = false;
            List<Restriction> moreRestrictions = new ArrayList<Restriction>();

            //copy and create a new restriction
            Restriction restriction = new Restriction();
            restriction.setLikeType(originalRestriction.getLikeType());
            restriction.setProperty(originalRestriction.getProperty());
            restriction.setRestrictionType(originalRestriction.getRestrictionType());
            restriction.setTemporalType(originalRestriction.getTemporalType());
            restriction.setValue(originalRestriction.getValue());

            //if RestrictionType is null set default to EQUALS
            if (restriction.getRestrictionType() == null) {
                restriction.setRestrictionType(RestrictionType.EQUALS);
            }

            //DataTable filter has his own logic
            if (restriction.getRestrictionType().equals(RestrictionType.DATA_TABLE_FILTER)) {
                String property = "";
                if (alias != null && !alias.trim().isEmpty() && restriction.getProperty().indexOf(".") > -1) {
                    property = restriction.getProperty().substring(restriction.getProperty().indexOf(".") + 1, restriction.getProperty().length());
                } else {
                    property = restriction.getProperty();
                }

                Class propertyType = String.class;
                try {
                    //try to get type from property
                    propertyType = ReflectionUtils.getPropertyType(this.from, property);
                } catch (IllegalArgumentException ex) {
                    //type cannot be get, keep String type
                }

                //set to type to EQUALS when is not String
                if (!propertyType.equals(String.class)) {
                    restriction.setRestrictionType(RestrictionType.EQUALS);
                    if (propertyType.isEnum()) {
                        restriction.setValue(Enum.valueOf(propertyType, restriction.getValue().toString()));
                    }
                    if (propertyType.equals(Integer.class) || propertyType.equals(int.class)) {
                        restriction.setValue(Integer.valueOf(StringUtils.getOnlyIntegerNumbers(restriction.getValue().toString())));
                    }
                    if (propertyType.equals(Long.class) || propertyType.equals(long.class)) {
                        restriction.setValue(Long.valueOf(StringUtils.getOnlyIntegerNumbers(restriction.getValue().toString())));
                    }
                    if (propertyType.equals(BigDecimal.class)) {
                        restriction.setValue(new BigDecimal(restriction.getValue().toString()));
                    }
                    if (propertyType.equals(Boolean.class) || propertyType.equals(boolean.class)) {
                        restriction.setValue(Boolean.valueOf(restriction.getValue().toString()));
                    }
                    //if is a date, then its a interval, set GREATER THAN and LESS THAN
                    if (propertyType.equals(Date.class) || propertyType.equals(Calendar.class)) {

                        SimpleDateFormat dateFormat = new SimpleDateFormat(I18N.getDatePattern(), I18N.getLocale());
                        Object value = restriction.getValue().toString();
                        String[] dateArray = null;
                        if (value != null) {
                            dateArray = value.toString().split(DATE_FILTER_INTERVAL_SEPARATOR);
                        }
                        String startDateString = null;
                        String endDateString = null;
                        if (dateArray != null && dateArray.length > 0) {
                            startDateString = dateArray[0];
                            if (dateArray.length > 1) {
                                endDateString = dateArray[1];
                            }
                        }
                        try {
                            //if start date is empty then should be ignored
                            if (startDateString != null && !startDateString.isEmpty()) {
                                restriction.setValue(dateFormat.parse(startDateString.trim()));
                                restriction.setTemporalType(TemporalType.TIMESTAMP);
                                restriction.setRestrictionType(RestrictionType.GREATER_EQUALS_THAN);
                            } else {
                                ignoreRestriction = true;
                            }
                            //add LESS THAN
                            if (endDateString != null && !endDateString.trim().isEmpty()) {
                                Date dateEnd = dateFormat.parse(endDateString.trim());
                                //add 1 day e set to the first second
                                Calendar calendar = (Calendar) Calendar.getInstance().clone();
                                calendar.setTime(dateEnd);
                                calendar.add(Calendar.DATE, 1);
                                calendar.set(Calendar.HOUR, 0);
                                calendar.set(Calendar.MINUTE, 0);
                                calendar.set(Calendar.SECOND, 0);
                                calendar.set(Calendar.MILLISECOND, 0);
                                dateEnd = calendar.getTime();
                                moreRestrictions.add(new Restriction(restriction.getProperty(), RestrictionType.LESS_THAN, dateEnd));
                            }
                        } catch (ParseException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                } else {
                    restriction.setRestrictionType(RestrictionType.LIKE);
                }

            }
            if (ignoreRestriction == false) {
                normalizedRestrictions.add(restriction);
            }
            if (moreRestrictions != null) {
                normalizedRestrictions.addAll(moreRestrictions);
            }
        }
    }

    
    /**
     * @param restrictions
     * @param alias
     * @return String of the part after "WHERE" from JPQL generated
     */
    public static String getQueryStringFromRestrictions(List<Restriction> restrictions, String alias) {
       
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
                String propertyName;
                if (alias != null && !alias.trim().isEmpty()) {
                    propertyName = alias + "." + restriction.getProperty();
                } else {
                    propertyName = restriction.getProperty();
                }
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

        return queryString.toString();
    }

    /**
     * @return The complete String of JPQL generated in this QueryBuilder
     */
    public String getQueryString() {

        StringBuilder queryString = new StringBuilder();
        //type Ex: (SELECT FROM, SELECT MAX, SELECT MIN)
        queryString.append(getQuerySelectClausule());

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
        queryString.append(QueryBuilder.getQueryStringFromRestrictions(normalizedRestrictions, alias));

        //order by
        if (order != null && !order.trim().isEmpty()) {
            queryString.append(" ORDER BY ").append(order).toString();
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

    public Query createQuery(Integer firstResult, Integer maxResults) {

        String queryString = getQueryString();
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

    public <T> List<T> getResultList(Integer maxResults) {
        List list = this.createQuery(maxResults).getResultList();
        if (list != null && attributeName != null && !attributeName.trim().isEmpty()) {
            return getNormalizedResultList(attributeName, list, from);
        }
        return list;
    }

    public <T> List<T> getResultList(Integer firstResult, Integer maxResults) {
        List list = this.createQuery(firstResult, maxResults).getResultList();
        if (list != null && attributeName != null && !attributeName.trim().isEmpty()) {
            return getNormalizedResultList(attributeName, list, from);
        }
        return list;
    }

    public <T> List<T> getResultList() {
        List list = this.createQuery().getResultList();
        if (list != null && attributeName != null && !attributeName.trim().isEmpty()) {
            return getNormalizedResultList(attributeName, list, from);
        }
        return list;
    }

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
}
