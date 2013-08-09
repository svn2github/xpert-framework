package com.xpert.faces.utils;

import com.xpert.Configuration;
import com.xpert.core.exception.StackException;
import com.xpert.core.exception.UniqueFieldException;
import com.xpert.i18n.XpertResourceBundle;
import com.xpert.i18n.I18N;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public class FacesMessageUtils {

    public static void sucess() {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, XpertResourceBundle.get("sucess"), null));
    }

    public static void info(String sumario) {
        getMessage(FacesContext.getCurrentInstance(), FacesMessage.SEVERITY_INFO, sumario);
    }

    public static void info(String sumario, String... parameters) {
        getMessage(FacesContext.getCurrentInstance(), FacesMessage.SEVERITY_INFO, sumario, parameters);
    }

    public static void info(StackException stackException) {
        getStackExceptionMessage(stackException, FacesMessage.SEVERITY_INFO);
    }

    public static void warning(String sumario) {
        getMessage(FacesContext.getCurrentInstance(), FacesMessage.SEVERITY_WARN, sumario);
    }

    public static void warning(String sumario, String... parameters) {
        getMessage(FacesContext.getCurrentInstance(), FacesMessage.SEVERITY_WARN, sumario, parameters);
    }

    public static void warning(StackException stackException) {
        getStackExceptionMessage(stackException, FacesMessage.SEVERITY_WARN);
    }

    public static void error(String sumario) {
        getMessage(FacesContext.getCurrentInstance(), FacesMessage.SEVERITY_ERROR, sumario);
    }

    public static void error(String sumario, String... parameters) {
        getMessage(FacesContext.getCurrentInstance(), FacesMessage.SEVERITY_ERROR, sumario, parameters);
    }

    public static void error(StackException stackException) {
        getStackExceptionMessage(stackException, FacesMessage.SEVERITY_ERROR);
    }

    public static void fatal(String sumario) {
        getMessage(FacesContext.getCurrentInstance(), FacesMessage.SEVERITY_FATAL, sumario);
    }

    public static void fatal(String sumario, String... parameters) {
        getMessage(FacesContext.getCurrentInstance(), FacesMessage.SEVERITY_FATAL, sumario, parameters);
    }

    public static void fatal(StackException stackException) {
        getStackExceptionMessage(stackException, FacesMessage.SEVERITY_FATAL);
    }

    public static void getStackExceptionMessage(StackException stackException, FacesMessage.Severity severity) {
        boolean i18n = true;

        if (stackException instanceof UniqueFieldException) {
            if (((UniqueFieldException) stackException).isI18n() == false) {
                i18n = false;
            }
        }

        if (stackException.getExceptions() == null || stackException.getExceptions().isEmpty()) {
            getMessage(FacesContext.getCurrentInstance(), severity, stackException.getMessage(), i18n, stackException.getParametros());
        } else {
            for (StackException re : stackException.getExceptions()) {
                getMessage(FacesContext.getCurrentInstance(), severity, re.getMessage(), i18n, re.getParametros());
            }
        }
    }

    /**
     * With I18N
     *
     * @param facesContext
     * @param severity
     * @param summary
     * @param parameters
     */
    public static void getMessage(FacesContext facesContext, FacesMessage.Severity severity, String summary, String... parameters) {
        getMessage(facesContext, severity, summary, true, parameters);
    }

    public static void getMessage(FacesContext facesContext, FacesMessage.Severity severity, String summary, boolean i18n, String... parameters) {

        if (Configuration.BUNDLE != null && i18n) {
            if (parameters != null && parameters.length > 0) {
                summary = I18N.get(summary, parameters);
            } else {
                summary = I18N.get(summary);
            }
        }

        facesContext.addMessage(null, new FacesMessage(severity, summary, null));

    }
}
