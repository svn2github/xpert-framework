package com.xpert.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
    
    public static void main(String[] args) {
        
        Profile profile1 = new Profile(1, "Profile 1");
        Profile profile2 = new Profile(2, "Profile 2");
        Profile profile3 = new Profile(3, "Profile 3");
        
       List<Person> people = new ArrayList<Person>();
        
        Person person1 = new Person(1, "Person 1", profile1);
        Person person2 = new Person(2, "Person 2", profile1);
        Person person3 = new Person(3, "Person 3", profile2);
        Person person4 = new Person(4, "Person 4", profile1);
        Person person5 = new Person(5, "Person 5", profile3);
        Person person6 = new Person(6, "Person 6", profile1);
        Person person7 = new Person(7, "Person 7", profile1);
        Person person8 = new Person(8, "Person 8", profile2);
        Person person9 = new Person(9, "Person 9", profile2);
        Person person10 = new Person(9, "Person 10", null);
        
        people.add(person1);
        people.add(person2);
        people.add(person3);
        people.add(person4);
        people.add(person5);
        people.add(person6);
        people.add(person7);
        people.add(person8);
        people.add(person9);
        people.add(person10);
        
        
        GroupList<Profile, Person> groupList = new GroupList<Profile, Person>(people, "profile");
        for(GroupList<Profile, Person> group : groupList.getList()){
            System.out.println(" --- "+group.getKey());
            System.out.println(" --- "+group.getValue());
        }
        
        
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
