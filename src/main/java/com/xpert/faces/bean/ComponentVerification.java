package com.xpert.faces.bean;

import com.xpert.faces.utils.FacesUtils;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.event.ComponentSystemEvent;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.confirmdialog.ConfirmDialog;
import org.primefaces.component.dialog.Dialog;

/**
 *
 * @author Ayslan
 */
@ManagedBean
public class ComponentVerification {

    public List<String> widgets = new ArrayList<String>();

    public void verifyUniqueWidgetVar(ComponentSystemEvent event) {
        UIComponent component = event.getComponent();

        String widget = "";
        if (component instanceof Dialog) {
            Dialog dialog = (Dialog) component;
            widget = dialog.getWidgetVar();
        }
        if (component instanceof ConfirmDialog) {
            ConfirmDialog confirmDialog = (ConfirmDialog) component;
            widget = confirmDialog.getWidgetVar();
        }
        if (component instanceof CommandButton) {
            CommandButton commandButton = (CommandButton) component;
            widget = commandButton.getWidgetVar();
        }

        if (widgets.contains(widget)) {
            UIComponent c = FacesUtils.findComponentInRoot(component.getClientId());
            if (c != null) {
                c.setRendered(false);
            }
        } else {
            widgets.add(widget);
        }

    }
}
