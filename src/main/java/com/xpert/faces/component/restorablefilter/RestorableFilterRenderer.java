package com.xpert.faces.component.restorablefilter;

import com.xpert.faces.utils.FacesUtils;
import java.io.IOException;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import org.primefaces.component.api.UIColumn;
import org.primefaces.component.column.Column;
import org.primefaces.component.datatable.DataTable;

/**
 *
 * @author Ayslan
 */
public class RestorableFilterRenderer extends Renderer {

    @Override
    public void encodeEnd(final FacesContext context, final UIComponent component) throws IOException {

        final RestorableFilter restorableFilter = (RestorableFilter) component;

        UIComponent targetComponent = component.findComponent(restorableFilter.getTarget());
        if (targetComponent == null) {
            throw new FacesException("Cannot find component " + restorableFilter.getTarget() + " in view.");
        } else {
            if (targetComponent instanceof DataTable == false) {
                throw new FacesException("RestorableFilter target " + restorableFilter.getTarget() + " (" + targetComponent.getClass().getName() + ")" + " is not a DataTable");
            }
            final ResponseWriter writer = context.getResponseWriter();
            final DataTable dataTable = (DataTable) targetComponent;
            String separator = String.valueOf(UINamingContainer.getSeparatorChar(context));
            StringBuilder bodyScript = new StringBuilder();
            Map filters = (Map) FacesUtils.getFromSession(dataTable.getClientId());
            for (UIColumn uicolumn : dataTable.getColumns()) {
                // params.put("teste", "teste");
                Column column = (Column) uicolumn;
                ValueExpression valueExpressionFilterBy = column.getValueExpression("filterBy");
                if (valueExpressionFilterBy != null) {
                    String expressionString = valueExpressionFilterBy.getExpressionString();
                    expressionString = expressionString.substring(2, expressionString.length() - 1);      //Remove #{}
                    expressionString = expressionString.substring(expressionString.indexOf(".") + 1, expressionString.length());      //Remove first property.
                    if (filters.containsKey(expressionString)) {
                        ValueExpression valueExpression = column.getValueExpression("filterValue");
                        if (valueExpression != null) {
                            valueExpression.setValue(context.getELContext(), filters.get(expressionString));
                        } else {
                            String filterId = column.getContainerClientId(context) + separator + "filter";
                            bodyScript.append("$(PrimeFaces.escapeClientId('").append(filterId).append("')).val('").append(filters.get(expressionString)).append("');");
                        }
                    }
                }
            }

            StringBuilder scriptBuilder = new StringBuilder();
            if (bodyScript.length() > 0) {
                scriptBuilder.append("$(function(){");
                scriptBuilder.append(bodyScript);
                scriptBuilder.append("});");
            }

            String scriptFilters = scriptBuilder.toString();

            if (scriptFilters != null && !scriptFilters.isEmpty()) {
                writer.startElement("script", null);
                writer.write(scriptFilters);
                writer.endElement("script");
            }
        }
    }

}
