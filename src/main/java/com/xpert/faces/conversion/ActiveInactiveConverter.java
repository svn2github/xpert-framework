package com.xpert.faces.conversion;

import com.xpert.i18n.XpertResourceBundle;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * Converter to show Active when true and Inactive when false
 *
 * @author Ayslan
 */
@FacesConverter(value = "activeInactiveConverter")
public class ActiveInactiveConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        Boolean condition = (Boolean) value;
        if (condition) {
            return XpertResourceBundle.get("active");
        } else {
            return XpertResourceBundle.get("inactive");
        }
    }
}
