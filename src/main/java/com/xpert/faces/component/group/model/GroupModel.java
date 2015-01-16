package com.xpert.faces.component.group.model;

import com.xpert.utils.CollectionsUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author Ayslan
 * @param <K> Key Type
 * @param <V> Value Type
 */
public class GroupModel<K, V> {

    private String groupBy;
    private String itemSortField;
    private GroupSortOrder itemSortOrder;
    private String sortField;
    private GroupSortOrder sortOrder;
    /**
     * the original value
     */
    private List<V> value;
    private List<GroupModelItem<K, V>> itens;

    public GroupModel(String field, List value) {
        this.groupBy = field;
        this.value = value;
        this.itens = new ArrayList<GroupModelItem<K, V>>();
    }

    public int getItensSize() {
        if (itens != null) {
            return itens.size();
        }
        return 0;
    }

    public int getSize() {
        if (value != null) {
            return value.size();
        }
        return 0;
    }

    public void groupItens() {
        if (value != null) {
         //   System.out.println("Grouping by: " + groupBy + " size: " + value.size() + " list: " + value);
            Map<Object, GroupModelItem> map = new HashMap<Object, GroupModelItem>();
            itens = new ArrayList<GroupModelItem<K, V>>();
            for (Object item : value) {
                try {
                    Object keyForData;
                    try {
                        keyForData = PropertyUtils.getProperty(item, groupBy);
                    } catch (NestedNullException ex) {
                        //null key
                        keyForData = null;
                    }
                    GroupModelItem groupModelItem = map.get(keyForData);
                    if (groupModelItem == null) {
                        try {
                            groupModelItem = new GroupModelItem();
                            groupModelItem.setKey(keyForData);
                            groupModelItem.setValue(new ArrayList());
                            itens.add(groupModelItem);
                        } catch (Exception ex) {
                            throw new RuntimeException("Error in instance type " + value.getClass().getName(), ex);
                        }
                        map.put(keyForData, groupModelItem);
                    }
                    groupModelItem.getValue().add(item);
                } catch (Exception ex) {
                    throw new RuntimeException("Error getting property " + groupBy + " in collection ", ex);
                }
            }

        } else {
            itens = new ArrayList<GroupModelItem<K, V>>();
        }

        //order main list
        String sortKey = "key";
        if (sortField != null && !sortField.isEmpty()) {
            sortKey = sortKey + "." + sortField;
        }

        if (itens != null && !itens.isEmpty()) {

            Object firstItem = itens.get(0);
            boolean isSort = false;
            if (firstItem instanceof Comparable || (sortField != null && !sortField.isEmpty())) {
                isSort = true;
            }
            if (isSort == true) {
                if (sortOrder == null || sortOrder.equals(GroupSortOrder.ASC)) {
                    CollectionsUtils.orderAsc(itens, sortKey);
                } else {
                    CollectionsUtils.orderDesc(itens, sortKey);
                }
            }

        }

       // System.out.println("quantidade de itens: " + itens.size());

        //normalize
        if (itens != null) {
            for (int i = 0; i < itens.size(); i++) {
                GroupModelItem groupModelItem = itens.get(i);
                groupModelItem.setIndex(i);
                if (i == 0) {
                    groupModelItem.setFirst(true);
                } else if (i == itens.size() - 1) {
                    groupModelItem.setLast(true);
                }
                //order itens
                if (itemSortField != null && !itemSortField.isEmpty()) {
                    if (itemSortOrder == null || itemSortOrder.equals(GroupSortOrder.ASC)) {
                        CollectionsUtils.orderAsc(groupModelItem.getValue(), itemSortField);
                    } else {
                        CollectionsUtils.orderDesc(groupModelItem.getValue(), itemSortField);
                    }
                }
            }
        }

    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public String getItemSortField() {
        return itemSortField;
    }

    public void setItemSortField(String itemSortField) {
        this.itemSortField = itemSortField;
    }

    public GroupSortOrder getItemSortOrder() {
        return itemSortOrder;
    }

    public void setItemSortOrder(GroupSortOrder itemSortOrder) {
        this.itemSortOrder = itemSortOrder;
    }

    public GroupSortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(GroupSortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<V> getValue() {
        return value;
    }

    public void setValue(List<V> value) {
        this.value = value;
    }

    public List<GroupModelItem<K, V>> getItens() {
        return itens;
    }

    public void setItens(List<GroupModelItem<K, V>> itens) {
        this.itens = itens;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

}
