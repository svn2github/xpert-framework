package com.xpert.faces.component.group;

import com.xpert.utils.CollectionsUtils;
import java.util.ArrayList;
import java.util.Collection;
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

    private String field;
    private String itemSortField;
    private GroupSortOrder itemSortOrder;
    private GroupSortOrder sortOrder;
    /**
     * the original value
     */
    private Collection<V> value;
    private List<GroupModelItem<K, V>> itens;

    public GroupModel(String field, Collection value) {
        this.field = field;
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
            Map<Object, GroupModelItem> map = new HashMap<Object, GroupModelItem>();
            itens = new ArrayList<GroupModelItem<K, V>>();
            for (Object item : value) {
                try {
                    Object keyForData;
                    try {
                        keyForData = PropertyUtils.getProperty(item, field);
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
                    throw new RuntimeException("Error getting property " + field + " in collection ", ex);
                }
            }

        } else {
            itens = new ArrayList<GroupModelItem<K, V>>();
        }

        //order main list
        if (sortOrder == null || sortOrder.equals(GroupSortOrder.ASC)) {
            CollectionsUtils.orderAsc(itens, "key");
        } else {
            CollectionsUtils.orderDesc(itens, "key");
        }

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
                        CollectionsUtils.orderAsc(groupModelItem.getValue(), itemSortField);
                    }
                }
            }
        }

    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
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

    public Collection<V> getValue() {
        return value;
    }

    public void setValue(Collection<V> value) {
        this.value = value;
    }

    public List<GroupModelItem<K, V>> getItens() {
        return itens;
    }

    public void setItens(List<GroupModelItem<K, V>> itens) {
        this.itens = itens;
    }

}
