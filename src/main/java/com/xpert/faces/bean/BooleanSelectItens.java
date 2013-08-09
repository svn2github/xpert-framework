package com.xpert.faces.bean;

import com.xpert.i18n.XpertResourceBundle;
import java.util.ArrayList;
import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;

/**
 *
 * @author Ayslan
 */
@ManagedBean
public class BooleanSelectItens extends ArrayList<SelectItem> {

    public BooleanSelectItens() {
        add(new SelectItem(true, XpertResourceBundle.get("yes")));
        add(new SelectItem(false, XpertResourceBundle.get("no")));
    }
    
}
