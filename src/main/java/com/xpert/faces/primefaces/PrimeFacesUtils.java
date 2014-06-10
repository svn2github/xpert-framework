package com.xpert.faces.primefaces;

import java.lang.reflect.Field;
import org.primefaces.context.RequestContext;

/**
 *
 * @author Ayslan
 */
public class PrimeFacesUtils {
    
    public static String normalizeDialog(String dialog) {
        if (dialog != null && !PrimeFacesUtils.isVersion3()) {
            return "PF('" + dialog + "')";
        }
        return dialog;
    }

    public static void closeDialog(String dialog) {
        if (dialog != null && !dialog.trim().isEmpty()) {
            dialog = normalizeDialog(dialog);
            RequestContext requestContext = RequestContext.getCurrentInstance();
            requestContext.execute(dialog + ".hide()");
        }
    }

    public static void showDialog(String dialog) {
        if (dialog != null && !dialog.trim().isEmpty()) {
            dialog = normalizeDialog(dialog);
            RequestContext requestContext = RequestContext.getCurrentInstance();
            requestContext.execute(dialog + ".show()");
        }
    }

    public static void update(String... targets) {
        RequestContext context = RequestContext.getCurrentInstance();
        if (context != null) {
            for (String string : targets) {
                context.update(string);
            }
        }
    }
    private static Boolean IS_VERSION_3;

    public static boolean isVersion3() {
        if (IS_VERSION_3 != null) {
            return IS_VERSION_3;
        } else {
            try {
                Class classConstants = Class.forName("org.primefaces.util.Constants");
                Field fieldVersion;
                fieldVersion = classConstants.getDeclaredField("VERSION");
                if (fieldVersion.get(null).toString().startsWith("3")) {
                    IS_VERSION_3 = true;
                }
            } catch (Exception ex) {
                IS_VERSION_3 = false;
            }

        }
        return IS_VERSION_3;
    }

    /**
     * @param targets
     * @deprecated use update instead
     */
    @Deprecated
    public static void addPartialUpdateTarget(String... targets) {
        RequestContext context = RequestContext.getCurrentInstance();
        if (context != null) {
            for (String string : targets) {
                context.update(string);
            }
        }
    }
}
