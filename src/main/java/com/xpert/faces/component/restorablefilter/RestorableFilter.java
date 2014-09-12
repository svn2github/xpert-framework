package com.xpert.faces.component.restorablefilter;

import com.xpert.faces.utils.FacesUtils;
import java.util.List;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import org.primefaces.component.api.UIColumn;
import org.primefaces.component.column.Column;
import org.primefaces.component.datatable.DataTable;

/**
 *
 * @author Ayslan
 */
@ResourceDependencies({
    @ResourceDependency(library = "xpert", name = "scripts/core.js")
})
public class RestorableFilter extends UIComponentBase {

    public static final String COMPONENT_FAMILY = "com.xpert.component";

    protected enum PropertyKeys {

        target;

        private String toString;

        PropertyKeys(final String toString) {
            this.toString = toString;
        }

        PropertyKeys() {
        }

        @Override
        public String toString() {
            return ((this.toString != null) ? this.toString : super.toString());
        }
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public static void storeFilterInSession(Map filters) {
        FacesContext context = FacesContext.getCurrentInstance();
        DataTable dataTable = (DataTable) UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        FacesUtils.addToSession(dataTable.getClientId(), filters);
        context.getViewRoot().getViewMap().put(dataTable.getClientId() + "_restorableFilter", true);
    }

    public static void restoreFilterFromSession(Map currentFilters) {
        FacesContext context = FacesContext.getCurrentInstance();
        DataTable dataTable = (DataTable) UIComponent.getCurrentComponent(context);
//        if (dataTable.getAttributes().get("restorableFilter") == null) {
//            return;
//        }

        Map viewMap = context.getViewRoot().getViewMap();

        //only first time
        Object fromViewMap = viewMap.get(dataTable.getClientId() + "_restorableFilter");
        if (fromViewMap != null) {
            return;
        }
        Map filters = (Map) FacesUtils.getFromSession(dataTable.getClientId());
        if (filters != null && !filters.isEmpty()) {
            dataTable.setFilters(filters);
        }
        if (currentFilters != null && filters != null) {
            currentFilters.putAll(filters);
        }
    }

    public String getTarget() {
        return (String) getStateHelper().eval(RestorableFilter.PropertyKeys.target, null);
    }

    public void setTarget(final String confirmLabel) {
        setAttribute(RestorableFilter.PropertyKeys.target, confirmLabel);
    }

    @SuppressWarnings("unchecked")
    public void setAttribute(final RestorableFilter.PropertyKeys property, final Object value) {
        getStateHelper().put(property, value);

        List<String> setAttributes
                = (List<String>) this.getAttributes().get("javax.faces.component.UIComponentBase.attributesThatAreSet");

        if (setAttributes != null && value == null) {
            final String attributeName = property.toString();
            final ValueExpression ve = getValueExpression(attributeName);
            if (ve == null) {
                setAttributes.remove(attributeName);
            } else if (!setAttributes.contains(attributeName)) {
                setAttributes.add(attributeName);
            }
        }
    }

}
