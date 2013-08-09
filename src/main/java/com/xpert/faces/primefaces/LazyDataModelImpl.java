package com.xpert.faces.primefaces;

import com.xpert.persistence.dao.BaseDAO;
import com.xpert.persistence.query.JoinBuilder;
import com.xpert.persistence.query.QueryType;
import com.xpert.persistence.query.Restriction;
import com.xpert.persistence.query.RestrictionType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public class LazyDataModelImpl<T> extends LazyDataModel {

    private BaseDAO<T> dao;
    private String defaultOrder;
    private String attributes;
    private OrderByHandler orderByHandler;
    /*
     * to add restrictions on query to filter table
     */
    private List<Restriction> restrictions;
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
        if (orderBy == null || orderBy.trim().isEmpty()) {
            orderBy = defaultOrder;
        } else {
            
            if(orderByHandler != null){
                orderBy = getOrderByHandler().getOrderBy(orderBy);
            }
            
            if (order.equals(SortOrder.DESCENDING)) {
                orderBy = orderBy + " DESC";
            }
        }

        List<Restriction> queryRestrictions = new ArrayList<Restriction>();
        if (restrictions != null && !restrictions.isEmpty()) {
            queryRestrictions.addAll(restrictions);
        }
        if (restriction != null) {
            queryRestrictions.add(restriction);
        }
        if (filters != null && !filters.isEmpty()) {
            for (Entry e : ((Map<String, String>) filters).entrySet()) {
                if (e.getValue() != null && !e.getValue().toString().isEmpty()) {
                    queryRestrictions.add(new Restriction(e.getKey().toString(), RestrictionType.DATA_TABLE_FILTER, e.getValue()));
                }
            }
        }
        

        List<T> dados = dao.getQueryBuilder().type(QueryType.SELECT, attributes).from(dao.getEntityClass()).add(queryRestrictions).join(joinBuilder)
                .orderBy(orderBy).getResultList(first, pageSize);
        
        this.setRowCount(dao.count(queryRestrictions).intValue());

        return dados;
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

    public String getDefaultOrder() {
        return defaultOrder;
    }

    public void setDefaultOrder(String defaultOrder) {
        this.defaultOrder = defaultOrder;
    }

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
    
}
