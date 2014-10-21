package com.xpert.persistence.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Utility class to manipulae JPA entities
 *
 * @author Ayslan
 */
public class EntityUtils {

    private static final Logger logger = Logger.getLogger(EntityUtils.class.getName());
    private static final Map<Class, String> ID_NAME_MAP = new HashMap<Class, String>();
    private static final Map<Class, AccessibleObject> ID_ACCESSIBLE_MAP = new HashMap<Class, AccessibleObject>();
    private static final Map<Class, Class> ID_TYPE_MAP = new HashMap<Class, Class>();

    /**
     * Return true is @EmbeddedId is present in class
     *
     * @param clazz
     * @return
     */
    public static boolean hasEmbeddedId(Class clazz) {
        AccessibleObject accessibleObject = getIdAccessibleObject(clazz);
        if (accessibleObject != null && accessibleObject.isAnnotationPresent(EmbeddedId.class)) {
            return true;
        }
        return false;
    }

    /**
     * Method to return entity id (field/method with annotation @Id or
     *
     * @EmbeddedId)
     *
     * @param object
     * @return
     */
    public static Object getId(Object object) {
        if (object == null) {
            return null;
        }
        return getId(object, object.getClass());
    }

    /**
     * Returns true if entity has a not null @Id/ @EmbeddedId
     *
     * @param object
     * @return
     */
    public static boolean isPersisted(Object object) {
        return getId(object) != null;
    }

    /**
     * Method to return entity id (field/method with annotation @Id or
     *
     * @EmbeddedId)
     *
     * @param object
     * @param clazz current class
     * @return
     */
    public static Object getId(Object object, Class clazz) {

        try {
            AccessibleObject accessibleObject = getIdAccessibleObject(clazz);
            if (accessibleObject instanceof Field) {
                Field field = (Field) accessibleObject;
                field.setAccessible(true);
                return field.get(object);
            }

            if (accessibleObject instanceof Method) {
                Method method = (Method) accessibleObject;
                return method.invoke(object);
            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Return the type of @Id/@EmbeddedId from entity. Example: Integer.class,
     * Long.class
     *
     * @param clazz
     * @return
     */
    public static Class getIdType(Class clazz) {
        Class type = ID_TYPE_MAP.get(clazz);
        if (type != null) {
            return type;
        }
        AccessibleObject accessibleObject = getIdAccessibleObject(clazz);
        if (accessibleObject instanceof Field) {
            type = ((Field) accessibleObject).getType();
            ID_TYPE_MAP.put(clazz, type);
            return type;
        }

        if (accessibleObject instanceof Method) {
            type = ((Method) accessibleObject).getReturnType();
            ID_TYPE_MAP.put(clazz, type);
            return type;
        }
        return null;
    }

    /**
     * Return the name of @Id/@EmbeddedId field/method from entity
     *
     * @param object
     * @return
     */
    public static String getIdFieldName(Object object) {
        return getIdFieldName(object.getClass());
    }

    /**
     * Return the name of @Id/@EmbeddedId field/method from entity
     *
     * @param clazz
     * @param object
     * @return
     */
    public static String getIdFieldName(Class clazz) {

        String nameFromMap = ID_NAME_MAP.get(clazz);
        if (nameFromMap != null) {
            return nameFromMap;
        }

        AccessibleObject accessibleObject = getIdAccessibleObject(clazz);

        if (accessibleObject instanceof Field) {
            String name = ((Field) accessibleObject).getName();
            ID_NAME_MAP.put(clazz, name);
            return name;
        }

        if (accessibleObject instanceof Method) {
            String name = ((Method) accessibleObject).getName();
            String withoutGet = name.substring(3, name.length());
            withoutGet = getLowerFirstLetter(withoutGet);
            ID_NAME_MAP.put(clazz, withoutGet);
            return withoutGet;
        }

        return null;
    }

    /**
     * Return field/method with annotation @Id/@EmbeddedId from a entity class.
     * AccessibleObject is superclass of Field and Method.
     *
     * @param clazz
     * @return
     */
    public static AccessibleObject getIdAccessibleObject(Class clazz) {

        AccessibleObject accessibleObjectFromMap = ID_ACCESSIBLE_MAP.get(clazz);
        if (accessibleObjectFromMap != null) {
            return accessibleObjectFromMap;
        }

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)) {
                ID_ACCESSIBLE_MAP.put(clazz, field);
                return field;
            }
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Id.class) || method.isAnnotationPresent(EmbeddedId.class)) {
                ID_ACCESSIBLE_MAP.put(clazz, method);
                return method;
            }
        }
        if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            return getIdAccessibleObject(clazz.getSuperclass());
        }
        return null;
    }

    public static String getLowerFirstLetter(String string) {
        if (string.length() == 1) {
            return string.toLowerCase();
        }
        if (string.length() > 1) {
            return string.substring(0, 1).toLowerCase() + "" + string.substring(1, string.length());
        }
        return "";
    }

    /**
     * Returns true if the class has the annotation @Entity
     *
     * @param clazz
     * @return
     */
    public static boolean isEntity(Class clazz) {
        if (clazz.isAnnotationPresent(Entity.class)) {
            return true;
        }
        if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            return isEntity(clazz.getSuperclass());
        }
        return false;
    }
}
