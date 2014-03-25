package com.xpert.faces.primefaces;

import com.xpert.core.crud.AbstractBaseBean;
import com.xpert.i18n.XpertResourceBundle;
import com.xpert.persistence.dao.BaseDAO;
import com.xpert.persistence.query.JoinBuilder;
import com.xpert.persistence.query.QueryType;
import com.xpert.persistence.query.Restriction;
import com.xpert.persistence.query.RestrictionType;
import com.xpert.persistence.query.Restrictions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public class LazyDataModelImpl<T> extends LazyDataModel {

    private static boolean DEBUG = false;
    private static final Logger logger = Logger.getLogger(LazyDataModelImpl.class.getName());
    private static final String DEFAULT_PAGINATOR_TEMPLATE = "{FirstPageLink} {PreviousPageLink} {PageLinks} {NextPageLink} {LastPageLink} {RowsPerPageDropdown} {CurrentPageReport}";
    private static final String UNKNOW_COUNT_PAGINATOR_TEMPLATE = "{PreviousPageLink} {NextPageLink} {RowsPerPageDropdown} {CurrentPageReport}";
    private BaseDAO<T> dao;
    private String defaultOrder;
    private String currentOrderBy;
    private String attributes;
    private OrderByHandler orderByHandler;
    private FilterByHandler filterByHandler;
    private LazyCountType lazyCountType;
    private Integer currentRowCount;
    /*
     * to add restrictions on query to filter table
     */
    private List<Restriction> restrictions;
    private List<Restriction> queryRestrictions;
    private Restriction restriction;
    private JoinBuilder joinBuilder;

    public LazyDataModelImpl(String attributes, String defaultOrder, Restriction restriction, BaseDAO<T> dao) {
        this.dao = dao;
        this.attributes = attributes;
        this.defaultOrder = defaultOrder;
        this.restriction = restriction;
    }

    public LazyDataModelImpl(String attributes, String defaultOrder, List<Restriction> restrictions, BaseDAO<T> dao) {
        this.dao = dao;
        this.attributes = attributes;
        this.defaultOrder = defaultOrder;
        this.restrictions = restrictions;
    }

    public LazyDataModelImpl(String defaultOrder, Restriction restriction, BaseDAO<T> dao) {
        this.dao = dao;
        this.defaultOrder = defaultOrder;
        this.restriction = restriction;
    }

    public LazyDataModelImpl(String defaultOrder, List<Restriction> restrictions, BaseDAO<T> dao) {
        this.dao = dao;
        this.defaultOrder = defaultOrder;
        this.restrictions = restrictions;
    }

    public LazyDataModelImpl(String defaultOrder, BaseDAO<T> dao) {
        this.dao = dao;
        this.defaultOrder = defaultOrder;
    }

    public LazyDataModelImpl(String defaultOrder, Restriction restriction, BaseDAO<T> dao, JoinBuilder joinBuilder) {
        this.dao = dao;
        this.defaultOrder = defaultOrder;
        this.restriction = restriction;
        this.joinBuilder = joinBuilder;
    }

    public LazyDataModelImpl(String defaultOrder, List<Restriction> restrictions, BaseDAO<T> dao, JoinBuilder joinBuilder) {
        this.dao = dao;
        this.defaultOrder = defaultOrder;
        this.restrictions = restrictions;
        this.joinBuilder = joinBuilder;
    }

    public LazyDataModelImpl(String defaultOrder, BaseDAO<T> dao, JoinBuilder joinBuilder) {
        this.dao = dao;
        this.defaultOrder = defaultOrder;
        this.joinBuilder = joinBuilder;
    }

    @Override
    public List load(int first, int pageSize, String orderBy, SortOrder order, Map filters) {

        long begin = System.currentTimeMillis();

        LazyCountType lazyCountType = getLazyCountType();
        if (lazyCountType == null) {
            lazyCountType = LazyCountType.ALWAYS;
        }
        if (orderBy == null || orderBy.trim().isEmpty()) {
            orderBy = defaultOrder;
        } else {
            OrderByHandler orderByHandler = getOrderByHandler();
            if (orderByHandler != null) {
                orderBy = orderByHandler.getOrderBy(orderBy);
            }
            if (order.equals(SortOrder.DESCENDING)) {
                orderBy = orderBy + " DESC";
            }
        }
        if (DEBUG) {
            logger.log(Level.INFO, "Lazy Count Type: {0}. Using order by {1}", new Object[]{lazyCountType, orderBy});
        }

        List<Restriction> currentQueryRestrictions = new ArrayList<Restriction>();

        // queryRestrictions = new ArrayList<Restriction>();
        if (restrictions != null && !restrictions.isEmpty()) {
            currentQueryRestrictions.addAll(restrictions);
        }
        if (restriction != null) {
            currentQueryRestrictions.add(restriction);
        }
        if (filters != null && !filters.isEmpty()) {
            for (Entry e : ((Map<String, String>) filters).entrySet()) {
                if (e.getValue() != null && !e.getValue().toString().isEmpty()) {
                    FilterByHandler filterByHandler = getFilterByHandler();
                    Restrictions restrictionsFromFilterBy = null;
                    if (filterByHandler != null) {
                        restrictionsFromFilterBy = filterByHandler.getFilterBy(e.getKey().toString(), e.getValue());
                    }
                    if (filterByHandler != null) {
                        currentQueryRestrictions.addAll(restrictionsFromFilterBy);
                    } else {
                        if (DEBUG) {
                            logger.log(Level.INFO, "Restriction added. Name: {0}, Value:  {1}", new Object[]{e.getKey(), e.getValue()});
                        }
                        currentQueryRestrictions.add(new Restriction(e.getKey().toString(), RestrictionType.DATA_TABLE_FILTER, e.getValue()));
                    }
                }
            }
        }

        this.currentOrderBy = orderBy;

        List<T> dados = dao.getQueryBuilder().type(QueryType.SELECT, attributes).from(dao.getEntityClass()).add(currentQueryRestrictions).join(joinBuilder)
                .orderBy(orderBy).getResultList(first, pageSize);

        if (DEBUG) {
            logger.log(Level.INFO, "Select on entity {0}, records found: {1} ", new Object[]{dao.getEntityClass().getName(), dados.size()});
        }

        //If ALWAYS or (ONLY_ONCE and not set currentRowCount or restrictions has changed)
        if (lazyCountType.equals(LazyCountType.ALWAYS)
                || (lazyCountType.equals(LazyCountType.ONLY_ONCE) && (currentRowCount == null || !currentQueryRestrictions.equals(queryRestrictions)))) {
            currentRowCount = dao.count(currentQueryRestrictions).intValue();
            if (DEBUG) {
                logger.log(Level.INFO, "Count on entity {0}, records found: {1} ", new Object[]{dao.getEntityClass().getName(), currentRowCount});
            }
            this.setRowCount(currentRowCount);
        }
        if (lazyCountType.equals(LazyCountType.ONLY_ONCE)) {
            this.setRowCount(currentRowCount);
        } else if (lazyCountType.equals(LazyCountType.NONE)) {
            currentRowCount = dados.size();
            this.setRowCount(Integer.MAX_VALUE);
        }

        queryRestrictions = currentQueryRestrictions;

        long end = System.currentTimeMillis();
        if (DEBUG) {
            logger.log(Level.INFO, "Load method executed in {0} milliseconds", (end - begin));
        }
        return dados;
    }

    /**
     * Return Paginator Template
     *
     * @return
     */
    public String getPaginatorTemplate() {
        if (isLazyCountTypeNone()) {
            return UNKNOW_COUNT_PAGINATOR_TEMPLATE;
        }
        return DEFAULT_PAGINATOR_TEMPLATE;
    }

    public boolean isLazyCountTypeNone() {
        LazyCountType lazyCountType = getLazyCountType();
        if (lazyCountType != null && lazyCountType.equals(LazyCountType.NONE)) {
            return true;
        }
        return false;
    }

    /**
     *
     * @return Current Page Report Template
     */
    public String getCurrentPageReportTemplate() {
        if (isLazyCountTypeNone()) {
            return XpertResourceBundle.get("page") + " {currentPage} ";
        }
        return "{totalRecords} " + XpertResourceBundle.get("records") + " (" + XpertResourceBundle.get("page") + " {currentPage} " + XpertResourceBundle.get("of") + " {totalPages})";
    }

    /**
     * Return all objects from data base, based on filters from data table
     *
     * @param orderBy
     * @return
     */
    public List getAllResults(String orderBy) {
        return dao.getQueryBuilder().type(QueryType.SELECT, attributes).from(dao.getEntityClass()).add(queryRestrictions).join(joinBuilder)
                .orderBy(orderBy).getResultList();
    }

    /**
     * Return all objects from data base, based on filters from data table
     *
     * @return
     */
    public List getAllResults() {
        return getAllResults(currentOrderBy);
    }

    @Override
    public void setRowIndex(int rowIndex) {
        if (getPageSize() == 0) {
            setPageSize(1);
        }
        super.setRowIndex(rowIndex);
    }

    public BaseDAO<T> getDao() {
        return dao;
    }

    public void setDao(BaseDAO<T> dao) {
        this.dao = dao;
    }

    /**
     * Default order by of query
     *
     * @return
     */
    public String getDefaultOrder() {
        return defaultOrder;
    }

    public void setDefaultOrder(String defaultOrder) {
        this.defaultOrder = defaultOrder;
    }

    /**
     * Restrictions to be added in Query
     *
     * @return
     */
    public List<Restriction> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(List<Restriction> restrictions) {
        this.restrictions = restrictions;
    }

    public OrderByHandler getOrderByHandler() {
        return orderByHandler;
    }

    public void setOrderByHandler(OrderByHandler orderByHandler) {
        this.orderByHandler = orderByHandler;
    }

    public String getCurrentOrderBy() {
        return currentOrderBy;
    }

    public void setCurrentOrderBy(String currentOrderBy) {
        this.currentOrderBy = currentOrderBy;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    /**
     *
     * @return Current Restrictions (Data Table restrictions and defined
     * Restrictions)
     */
    public List<Restriction> getQueryRestrictions() {
        return queryRestrictions;
    }

    public void setQueryRestrictions(List<Restriction> queryRestrictions) {
        this.queryRestrictions = queryRestrictions;
    }

    public JoinBuilder getJoinBuilder() {
        return joinBuilder;
    }

    public void setJoinBuilder(JoinBuilder joinBuilder) {
        this.joinBuilder = joinBuilder;
    }

    public LazyCountType getLazyCountType() {
        return lazyCountType;
    }

    public void setLazyCountType(LazyCountType lazyCountType) {
        this.lazyCountType = lazyCountType;
    }

    public Integer getCurrentRowCount() {
        return currentRowCount;
    }

    public void setCurrentRowCount(Integer currentRowCount) {
        this.currentRowCount = currentRowCount;
    }

    public FilterByHandler getFilterByHandler() {
        return filterByHandler;
    }

    public void setFilterByHandler(FilterByHandler filterByHandler) {
        this.filterByHandler = filterByHandler;
    }

}
