package com.xpert.faces.component.initializer;

import java.io.IOException;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.event.PreRenderComponentEvent;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;
import javax.persistence.EntityManager;

/**
 *
 * @author ayslan
 */
public class InitializerHandler extends TagHandler {

    protected final TagAttribute property;
    protected final TagAttribute value;
    protected final TagAttribute entityManager;

    public InitializerHandler(ComponentConfig config) {
        super(config);
        this.value = this.getAttribute("value");
        this.property = this.getAttribute("property");
        this.entityManager = this.getAttribute("entityManager");
    }

    public InitializerHandler(TagConfig config) {
        super(config);
        this.value = this.getAttribute("value");
        this.property = this.getAttribute("property");
        this.entityManager = this.getAttribute("entityManager");
    }

    public void apply(final FaceletContext faceletContext, final UIComponent parent) throws IOException {
        InitializerEventListener initializerEventListener = new InitializerEventListener(
                getProperty(faceletContext), getValueExpression(faceletContext), faceletContext, parent, getEntityManager(faceletContext));
        parent.subscribeToEvent(PreRenderComponentEvent.class, initializerEventListener);
    }

    public String getProperty(FaceletContext faceletContext) {
        if (property != null) {
            ValueExpression valueExpression = property.getValueExpression(faceletContext, String.class);
            if (valueExpression != null) {
                return (String) valueExpression.getValue(faceletContext);

            }
        }
        return null;
    }
    public EntityManager getEntityManager(FaceletContext faceletContext) {
        if (entityManager != null) {
            ValueExpression valueExpression = entityManager.getValueExpression(faceletContext, EntityManager.class);
            if (valueExpression != null) {
                return (EntityManager) valueExpression.getValue(faceletContext);

            }
        }
        return null;
    }

    public ValueExpression getValueExpression(FaceletContext faceletContext) {
        if (value != null) {
            return value.getValueExpression(faceletContext, Object.class);
        }
        return null;
    }
}
