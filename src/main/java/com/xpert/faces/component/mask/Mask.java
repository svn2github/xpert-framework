package com.xpert.faces.component.mask;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponentBase;

/**
 *
 * @author Ayslan
 */
@ResourceDependencies({
    @ResourceDependency(library = "xpert", name = "scripts/core.js")
})
public class Mask extends UIComponentBase {

    public static final String COMPONENT_FAMILY = "com.xpert.component";

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }
}
