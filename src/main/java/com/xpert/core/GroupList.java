package com.xpert.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author ayslan
 */
public class GroupList<K, V> extends ArrayList<GroupList> {

    private static final Logger logger = Logger.getLogger(GroupList.class.getName());
    private String field;
    private Collection<V> value;
    private K key;

    public GroupList() {
    }

    public GroupList(Collection<V> value, String field) {
        this.field = field;
        this.value = value;
    }
    
    public GroupList<K, V> getList() {

        if (value != null) {
            try {
                Map<Object, Collection> map = new HashMap<Object, Collection>();
                for (V v : value) {
                    K keyForData = (K) PropertyUtils.getProperty(v, field);
                    Collection collection = map.get(keyForData);
                    if (collection == null) {
                        collection = new ArrayList<V>();
                        map.put(keyForData, collection);
                    }
                    collection.add(v);

                }

                GroupList<K, V> groupListParent = new GroupList<K, V>();
                for (Map.Entry<Object, Collection> e : map.entrySet()) {
                    GroupList<K, V> groupList = new GroupList<K, V>();
                    groupList.setKey((K) e.getKey());
                    groupList.setValue(e.getValue());
                    groupListParent.add(groupList);
                }
                return groupListParent;

            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }


        return null;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Collection<V> getValue() {
        return value;
    }

    public void setValue(Collection<V> value) {
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }
}
