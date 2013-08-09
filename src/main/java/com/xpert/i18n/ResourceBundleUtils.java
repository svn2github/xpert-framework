package com.xpert.i18n;

import com.xpert.maker.BeanCreator;
import com.xpert.utils.StringUtils;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Ayslan
 */
public class ResourceBundleUtils {

    private static final Logger logger = Logger.getLogger(ResourceBundleUtils.class.getName());
    public static final Locale PT_BR = new Locale("pt", "BR");
    private static final Pattern PATTERN_FIND_NUMBER = Pattern.compile("\\{[0-9]\\}");

    /**
     *
     * Método que pega a mensagem e formata através dos parametros informados.
     * Os parametros devem está nas mensagens no formato: {0} {1}. Ex: Já existe
     * o município {0} cadastrado para o estado {1}
     *
     * @param message
     * @param array
     * @return
     */
    public static String get(String key, String bundle, Object... array) {
        return get(key, bundle, null, array);
    }

    /**
     *
     * Método que pega a mensagem e formata através dos parametros informados.
     * Os parametros devem está nas mensagens no formato: {0} {1}. Ex: Já existe
     * o município {0} cadastrado para o estado {1}
     *
     * @param message
     * @param array
     * @return
     */
    public static String get(String key, String bundle, ClassLoader classLoader, Object... array) {

        if (key == null || key.isEmpty()) {
            try {
                throw new IllegalArgumentException("ResourceBundle key is required");
            } catch (IllegalArgumentException ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
            return "ResourceBundle key is required";
        }

        Locale locale = I18N.getLocale();

        ResourceBundle resourceBundle = null;

        try {
            if (classLoader != null) {
                resourceBundle = ResourceBundle.getBundle(bundle, locale, classLoader);
            } else {
                resourceBundle = ResourceBundle.getBundle(bundle, locale);
            }

            if (resourceBundle == null || (!locale.equals(PT_BR) && !resourceBundle.containsKey(key))) {
                resourceBundle = ResourceBundle.getBundle(bundle, PT_BR, classLoader);
            }

            if (resourceBundle == null || !resourceBundle.containsKey(key)) {
                return key;
            }

            key = resourceBundle.getString(key);

            if (array != null && array.length > 0) {
                Matcher matcher = PATTERN_FIND_NUMBER.matcher(key);
                while (matcher.find()) {
                    String chave = matcher.group();
                    int posicao = Integer.valueOf(StringUtils.getOnlyIntegerNumbers(chave));
                    if (posicao < array.length && array[posicao] != null) {
                        key = key.replace(chave, array[posicao].toString());
                    }
                }
                return key;
            }

        } catch (MissingResourceException ex2) {
            return key;
        }

        return key;
    }
}
