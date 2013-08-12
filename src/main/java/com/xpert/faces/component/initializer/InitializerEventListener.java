package com.xpert.faces.component.initializer;

import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.view.facelets.FaceletContext;
import javax.persistence.EntityManager;

/**
 *
 * @author ayslan
 */
public class InitializerEventListener implements ComponentSystemEventListener {

    public static final String INITILIZER_BEAN_IDENTIFIER = "xpert.initializer.initializerBean";
    private String property;
    private ValueExpression valueExpression;
    private FaceletContext faceletContext;
    private EntityManager entityManager;
    private UIComponent parent;

    public InitializerEventListener() {
    }

    public InitializerEventListener(String property, ValueExpression valueExpression,
            FaceletContext faceletContext, UIComponent parent, EntityManager entityManager) {
        this.property = property;
        this.valueExpression = valueExpression;
        this.faceletContext = faceletContext;
        this.parent = parent;
        this.entityManager = entityManager;
    }

    public InitializerBean getInitializerBean(FacesContext context) {
        Map requestMap = context.getExternalContext().getRequestMap();
        InitializerBean initializerBean = (InitializerBean) requestMap.get(INITILIZER_BEAN_IDENTIFIER);
        if (initializerBean != null) {
            return initializerBean;
        } else {
            initializerBean = new InitializerBean(entityManager);
            requestMap.put(INITILIZER_BEAN_IDENTIFIER, initializerBean);
            return initializerBean;
        }
    }

    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        if (parent == null || faceletContext == null) {
            return;
        }
        InitializerBean initializerBean = getInitializerBean(faceletContext.getFacesContext());
        if (valueExpression == null) {
            initializerBean.initialize(parent, faceletContext.getFacesContext(), property);
        } else {
            initializerBean.initialize(parent, faceletContext.getFacesContext(), valueExpression);
        }
    }
}
