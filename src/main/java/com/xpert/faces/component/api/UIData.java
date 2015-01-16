/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xpert.faces.component.api;

import java.util.Map;
import javax.faces.FacesException;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIViewRoot;
import javax.faces.component.UniqueIdVendor;
import javax.faces.context.FacesContext;
import javax.faces.render.Renderer;
import org.primefaces.util.ComponentUtils;

/**
 *
 * @author ayslan
 */
public class UIData extends javax.faces.component.UIData {

    private String clientId = null;
    private StringBuilder idBuilder = new StringBuilder();
    private Object oldVar = null;

    protected enum PropertyKeys {

        rowIndex, rowIndexVar, saved;

        String toString;

        PropertyKeys(String toString) {
            this.toString = toString;
        }

        PropertyKeys() {
        }

        public String toString() {
            return ((this.toString != null) ? this.toString : super.toString());
        }
    }

    @Override
    public void setRowIndex(int rowIndex) {
        saveDescendantState();
        setRowModel(rowIndex);
        restoreDescendantState();
    }

    @Override
    public String getClientId(FacesContext context) {
        if (this.clientId != null) {
            return this.clientId;
        }

        String id = getId();
        if (id == null) {
            UniqueIdVendor parentUniqueIdVendor = ComponentUtils.findParentUniqueIdVendor(this);

            if (parentUniqueIdVendor == null) {
                UIViewRoot viewRoot = context.getViewRoot();

                if (viewRoot != null) {
                    id = viewRoot.createUniqueId();
                } else {
                    throw new FacesException("Cannot create clientId for " + this.getClass().getCanonicalName());
                }
            } else {
                id = parentUniqueIdVendor.createUniqueId(context, null);
            }

            this.setId(id);
        }

        UIComponent namingContainer = findParentNamingContainer(this);
        if (namingContainer != null) {
            String containerClientId = namingContainer.getContainerClientId(context);

            if (containerClientId != null) {
                this.clientId = this.idBuilder.append(containerClientId).append(UINamingContainer.getSeparatorChar(context)).append(id).toString();
                this.idBuilder.setLength(0);
            } else {
                this.clientId = id;
            }
        } else {
            this.clientId = id;
        }

        Renderer renderer = getRenderer(context);
        if (renderer != null) {
            this.clientId = renderer.convertClientId(context, this.clientId);
        }

        return this.clientId;
    }

    public static UIComponent findParentNamingContainer(UIComponent component) {
        UIComponent parent = component.getParent();

        while (parent != null) {
            if (parent instanceof NamingContainer) {
                return (UIComponent) parent;
            }

            parent = parent.getParent();
        }

        return null;
    }

    public void setRowModel(int rowIndex) {
        //update rowIndex
        getStateHelper().put(PropertyKeys.rowIndex, rowIndex);
        getDataModel().setRowIndex(rowIndex);

        //clear datamodel
        if (rowIndex == -1) {
            setDataModel(null);
        }

        //update var
        String var = (String) this.getVar();
        if (var != null) {
            String rowIndexVar = this.getRowIndexVar();
            Map<String, Object> requestMap = getFacesContext().getExternalContext().getRequestMap();

            if (rowIndex == -1) {
                oldVar = requestMap.remove(var);

                if (rowIndexVar != null) {
                    requestMap.remove(rowIndexVar);
                }
            } else if (isRowAvailable()) {
                requestMap.put(var, getRowData());

                if (rowIndexVar != null) {
                    requestMap.put(rowIndexVar, rowIndex);
                }
            } else {
                requestMap.remove(var);

                if (rowIndexVar != null) {
                    requestMap.put(rowIndexVar, rowIndex);
                }

                if (oldVar != null) {
                    requestMap.put(var, oldVar);
                    oldVar = null;
                }
            }
        }
    }

    public java.lang.String getRowIndexVar() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.rowIndexVar, null);
    }

    public void setRowIndexVar(java.lang.String _rowIndexVar) {
        getStateHelper().put(PropertyKeys.rowIndexVar, _rowIndexVar);
    }

    protected void saveDescendantState() {
        FacesContext context = getFacesContext();

        if (this.getChildCount() > 0) {
            for (UIComponent kid : getChildren()) {
                saveDescendantState(kid, context);
            }
        }

        if (this.getFacetCount() > 0) {
            for (UIComponent facet : this.getFacets().values()) {
                saveDescendantState(facet, context);
            }
        }
    }

    protected void saveDescendantState(UIComponent component, FacesContext context) {
        Map<String, SavedState> saved = (Map<String, SavedState>) getStateHelper().get(PropertyKeys.saved);

        if (component instanceof EditableValueHolder) {
            EditableValueHolder input = (EditableValueHolder) component;
            SavedState state = null;
            String componentClientId = component.getClientId(context);

            if (saved == null) {
                state = new SavedState();
                getStateHelper().put(PropertyKeys.saved, componentClientId, state);
            }

            if (state == null) {
                state = saved.get(componentClientId);

                if (state == null) {
                    state = new SavedState();
                    getStateHelper().put(PropertyKeys.saved, componentClientId, state);
                }
            }

            state.setValue(input.getLocalValue());
            state.setValid(input.isValid());
            state.setSubmittedValue(input.getSubmittedValue());
            state.setLocalValueSet(input.isLocalValueSet());
        } else if (component instanceof UIForm) {
            UIForm form = (UIForm) component;
            String componentClientId = component.getClientId(context);
            SavedState state = null;
            if (saved == null) {
                state = new SavedState();
                getStateHelper().put(UIData.PropertyKeys.saved, componentClientId, state);
            }

            if (state == null) {
                state = saved.get(componentClientId);
                if (state == null) {
                    state = new SavedState();
                    //saved.put(clientId, state);
                    getStateHelper().put(PropertyKeys.saved, componentClientId, state);
                }
            }
            state.setSubmitted(form.isSubmitted());
        }

        //save state for children
        if (component.getChildCount() > 0) {
            for (UIComponent kid : component.getChildren()) {
                saveDescendantState(kid, context);
            }
        }

        //save state for facets
        if (component.getFacetCount() > 0) {
            for (UIComponent facet : component.getFacets().values()) {
                saveDescendantState(facet, context);
            }
        }

    }

    protected void restoreDescendantState() {
        FacesContext context = getFacesContext();

        if (getChildCount() > 0) {
            for (UIComponent kid : getChildren()) {
                restoreDescendantState(kid, context);
            }
        }

        if (this.getFacetCount() > 0) {
            for (UIComponent facet : this.getFacets().values()) {
                restoreDescendantState(facet, context);
            }
        }
    }

    protected void restoreDescendantState(UIComponent component, FacesContext context) {
        String id = component.getId();
        component.setId(id); //reset the client id
        Map<String, SavedState> saved = (Map<String, SavedState>) getStateHelper().get(PropertyKeys.saved);

        if (component instanceof EditableValueHolder) {
            EditableValueHolder input = (EditableValueHolder) component;
            String componentClientId = component.getClientId(context);

            SavedState state = saved.get(componentClientId);
            if (state == null) {
                state = new SavedState();
            }

            input.setValue(state.getValue());
            input.setValid(state.isValid());
            input.setSubmittedValue(state.getSubmittedValue());
            input.setLocalValueSet(state.isLocalValueSet());
        } else if (component instanceof UIForm) {
            UIForm form = (UIForm) component;
            String componentClientId = component.getClientId(context);
            SavedState state = saved.get(componentClientId);
            if (state == null) {
                state = new SavedState();
            }

            form.setSubmitted(state.getSubmitted());
            state.setSubmitted(form.isSubmitted());
        }

        //restore state of children
        if (component.getChildCount() > 0) {
            for (UIComponent kid : component.getChildren()) {
                restoreDescendantState(kid, context);
            }
        }

        //restore state of facets
        if (component.getFacetCount() > 0) {
            for (UIComponent facet : component.getFacets().values()) {
                restoreDescendantState(facet, context);
            }
        }

    }
}
