package com.xpert.faces.component.pdfprinter;

import java.io.IOException;

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

public class PDFPrinterTagHandler extends TagHandler {

    private final TagAttribute target;
    private final TagAttribute fileName;
    private final TagAttribute orientation;

    public PDFPrinterTagHandler(TagConfig tagConfig) {
        super(tagConfig);
        this.target = getRequiredAttribute("target");
        this.fileName = getAttribute("fileName");
        this.orientation = getAttribute("orientation");
    }

    public void apply(FaceletContext faceletContext, UIComponent parent) throws IOException, FacesException, FaceletException, ELException {
        if (ComponentHandler.isNew(parent)) {
            ValueExpression targetVE = target.getValueExpression(faceletContext, Object.class);
            ValueExpression fileNameVE = null;
            if (fileName != null) {
                fileNameVE = fileName.getValueExpression(faceletContext, Object.class);
            }
            ValueExpression orientationVE = null;
            if (orientation != null) {
                orientationVE = orientation.getValueExpression(faceletContext, Object.class);
            }

            ActionSource actionSource = (ActionSource) parent;
            actionSource.addActionListener(new PDFPrinter(targetVE, fileNameVE, orientationVE));

            ClientBehaviorHolder clientBehaviorHolder = (ClientBehaviorHolder) parent;
            clientBehaviorHolder.addClientBehavior("click", new PDFPrinterBehavior(target.getValue(faceletContext)));

        }
    }

}
