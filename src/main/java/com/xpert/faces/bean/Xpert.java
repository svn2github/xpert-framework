package com.xpert.faces.bean;

import com.xpert.faces.primefaces.PrimeFacesUtils;

/**
 *
 * @author Ayslan
 */
public class Xpert {
 
    public String normalizePrimeFacesWidget(String widgetVar){
        return PrimeFacesUtils.normalizeWidgetVar(widgetVar);
    }
    
}
