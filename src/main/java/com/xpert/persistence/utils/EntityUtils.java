package com.xpert.persistence.utils;

import java.lang.annotation.Annotation;
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
 *
 * @author Ayslan
 */
public class EntityUtils {

    private static final Logger logger = Logger.getLogger(EntityUtils.class.getName());
    private static final Map<Class, String> idNameMap = new HashMap<Class, String>();

    public static Object getId(Object object) {
        if (object == null) {
            return null;
        }
        return getId(object, object.getClass());
    }

    public static boolean isPersisted(Object object) {
        return getId(object) != null;
    }

    public static Object getId(Object object, Class clazz) {
        Field[] fields = clazz.getDeclaredFields();
        Method[] methods = clazz.getDeclaredMethods();
        try {
            for (Field field : fields) {
                if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)) {
                    field.setAccessible(true);
                    return field.get(object);
                }
            }
            for (Method method : methods) {
                if (method.isAnnotationPresent(Id.class) || method.isAnnotationPresent(EmbeddedId.class)) {
                    return method.invoke(object);
                }
            }
            if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
                return getId(object, clazz.getSuperclass());
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static String getIdFieldName(Object object) {
        return getIdFieldName(object.getClass());
    }

    public static String getIdFieldName(Class clazz) {

        String nameFromMap = idNameMap.get(clazz);
        if (nameFromMap != null) {
            return nameFromMap;
        }

        for (Field field : clazz.getDeclaredFields()) {
            String name = field.getName();
            Annotation[] annotations = field.getDeclaredAnnotations();
            for (int i = 0; i < annotations.length; i++) {
                if (annotations[i].annotationType().equals(Id.class) || annotations[i].annotationType().equals(EmbeddedId.class)) {
                    idNameMap.put(clazz, name);
                    return name;
                }
            }
        }
        for (Method method : clazz.getDeclaredMethods()) {
            String name = method.getName();
            if (name.startsWith("get")) {
                Annotation[] annotations = method.getDeclaredAnnotations();
                for (int i = 0; i < annotations.length; i++) {
                    if (annotations[i].annotationType().equals(Id.class) || annotations[i].annotationType().equals(EmbeddedId.class)) {
                        String withoutGet = name.substring(3, name.length());
                        withoutGet = getLowerFirstLetter(withoutGet);
                        idNameMap.put(clazz, withoutGet);
                        return withoutGet;
                    }
                }
            }
        }
        if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            return getIdFieldName(clazz.getSuperclass());
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
