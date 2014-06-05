package com.xpert.faces.bean;

import com.xpert.faces.primefaces.PrimeFacesUtils;
import javax.faces.bean.ManagedBean;

/**
 *
 * @author ayslan
 */
@ManagedBean
public class PrimeFacesVersion {

    public boolean isPrimeFaces3() {
        return PrimeFacesUtils.isVersion3();
    }
    
}
