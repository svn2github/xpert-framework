package com.xpert.faces.primefaces;

import org.primefaces.context.RequestContext;

/**
 *
 * @author Ayslan
 */
public class PrimeFacesUtils {

    public static void closeDialog(String dialog) {
        if (dialog != null && !dialog.trim().isEmpty()) {
            RequestContext requestContext = RequestContext.getCurrentInstance();
            requestContext.execute(dialog + ".hide()");
        }
    }

    public static void showDialog(String dialog) {
        if (dialog != null && !dialog.trim().isEmpty()) {
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
