package com.xpert.faces.component.datefilter;

import com.xpert.i18n.I18N;
import com.xpert.i18n.XpertResourceBundle;
import java.io.IOException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import org.primefaces.component.behavior.ajax.AjaxBehavior;
import org.primefaces.component.calendar.Calendar;
import org.primefaces.component.calendar.CalendarRenderer;
import org.primefaces.component.column.Column;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.inputtext.InputText;

/**
 *
 * @author ayslan
 */
public class DateFilterRenderer extends Renderer {

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
    }

    @Override
    public void decode(FacesContext context, UIComponent component) {
        setDateFilterValues(context, component);
    }

    public void setDateFilterValues(FacesContext context, UIComponent component) {
        DateFilter dateFilter = (DateFilter) component;

        String clientId = dateFilter.getClientId(context);
        String startCalendarValue = (String) context.getExternalContext().getRequestParameterMap().get(clientId + "_calendar-start_input");
        String endCalendarValue = (String) context.getExternalContext().getRequestParameterMap().get(dateFilter.getId() + "_calendar-end_input");

        if (startCalendarValue != null) {
            dateFilter.setCalendarStartValue(startCalendarValue);
        }
        if (endCalendarValue != null) {
            dateFilter.setCalendarEndValue(endCalendarValue);
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        setDateFilterValues(context, component);
        encodeDateFilter(component, context);
    }

    public UIComponent getColumnParent(UIComponent component) {
        if (component != null) {
            UIComponent parent = component.getParent();
            if (parent instanceof Column) {
                return parent;
            } else {
                if (component.getParent() != null) {
                    return getColumnParent(component.getParent());
                }
            }
        }
        return null;
    }

    public void encodeDateFilter(UIComponent component, FacesContext context) throws IOException {

        DateFilter dateFilter = (DateFilter) component;
        String dateFormat = I18N.getDatePattern();

        Calendar calendarStart = new Calendar();
        calendarStart.setStyleClass("calendar-filter calendar-filter-start");
        calendarStart.setNavigator(true);
        calendarStart.setShowOn("both");
        calendarStart.setShowButtonPanel(true);
        calendarStart.setPattern(dateFormat);
        calendarStart.setReadonly(true);
        calendarStart.setId(component.getId() + "_calendar-start");

        if (dateFilter.getCalendarStartValue() != null) {
            calendarStart.setSubmittedValue(dateFilter.getCalendarStartValue());
        }

        Calendar calendarEnd = new Calendar();
        calendarEnd.setStyleClass("calendar-filter calendar-filter-end");
        calendarEnd.setNavigator(true);
        calendarEnd.setShowOn("both");
        calendarEnd.setShowButtonPanel(true);
        calendarEnd.setPattern(dateFormat);
        calendarEnd.setReadonly(true);
        calendarEnd.setId(component.getId() + "_calendar-end");

        if (dateFilter.getCalendarEndValue() != null) {
            calendarEnd.setSubmittedValue(dateFilter.getCalendarEndValue());
        }

        Column column = (Column) getColumnParent(component);
        if (column == null) {
            throw new FacesException("Date Filter musto be child of a Column");
        }
        
        column.setFilterStyle("display: none;");
        DataTable dataTable = (DataTable) column.getParent();
        String widgetVar = dataTable.resolveWidgetVar();

        AjaxBehavior ajaxBehavior = new AjaxBehavior();
        ajaxBehavior.setProcess(":" + dataTable.getClientId());
        ajaxBehavior.setUpdate(":" + dataTable.getClientId());

        String filterScript = "Xpert.dateFilter('" + calendarEnd.getClientId() + "');" + widgetVar + ".filter(); return false;";
        ajaxBehavior.setOnstart(filterScript);

        calendarStart.addClientBehavior("dateSelect", ajaxBehavior);
        calendarEnd.addClientBehavior("dateSelect", ajaxBehavior);

        calendarStart.setParent(component);
        calendarEnd.setParent(component);

        //writer response
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("div", component);
        writer.writeAttribute("class", "panel-calendar-filter", null);
        writer.startElement("table", component);
        //first line
        writer.startElement("tr", component);
        //first column
        writer.startElement("td", component);
        writer.write(XpertResourceBundle.get("from"));
        writer.endElement("td");
        //second column
        writer.startElement("td", component);

        CalendarRenderer calendarRenderer = new CalendarRenderer();
        calendarRenderer.encodeEnd(context, calendarStart);

        writer.endElement("td");
        writer.endElement("tr");
        //second line
        writer.startElement("tr", component);
        //first column
        writer.startElement("td", component);
        writer.write(XpertResourceBundle.get("to"));
        writer.endElement("td");
        //second column

        writer.startElement("td", component);
        calendarRenderer = new CalendarRenderer();
        calendarRenderer.encodeEnd(context, calendarEnd);
        writer.endElement("td");
        writer.endElement("tr");
        writer.endElement("table");
        writer.endElement("div");
        //clear parent and prevent to add to tree
        calendarStart.setParent(null);
        calendarEnd.setParent(null);





    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
