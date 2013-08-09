package com.xpert.faces.component.filter;

import com.xpert.i18n.XpertResourceBundle;
import java.io.IOException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

/**
 *
 * @author Ayslan
 */
public class FilterOnEnterRenderer extends Renderer {

    @Override
    public void encodeEnd(final FacesContext context, final UIComponent component) throws IOException {
        final ResponseWriter writer = context.getResponseWriter();
        final FilterOnEnter filterOnEnter = (FilterOnEnter) component;

        String target = "";
        if (filterOnEnter.getTarget() != null && !filterOnEnter.getTarget().isEmpty()) {
            UIComponent targetComponent = component.findComponent(filterOnEnter.getTarget());
            if (targetComponent == null) {
                throw new FacesException("Cannot find component " + filterOnEnter.getTarget() + " in view.");
            }else{
                target = targetComponent.getClientId(context);
            }
        }

        writer.startElement("script", null);
        writer.writeAttribute("type", "text/javascript", null);
        writer.write(getScript(target, filterOnEnter));
        writer.endElement("script");
    }

    public String getScript(String target, FilterOnEnter filterOnEnter) {

        StringBuilder script = new StringBuilder();
        script.append("$(function() {Xpert.filterOnEnter('");
        script.append(target).append("','");
        script.append(filterOnEnter.getSelector()).append("');});");

        return script.toString();
    }
}
