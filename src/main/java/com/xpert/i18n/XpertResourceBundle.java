package com.xpert.i18n;

/**
 *
 * @author Ayslan
 */
public class XpertResourceBundle {

    public static final String CORE_BUNDLE_PATH = "com.xpert.messages";

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(String key, Object... array) {
        return ResourceBundleUtils.get(key, CORE_BUNDLE_PATH, XpertResourceBundle.class.getClassLoader(), array);
    }
}
