package com.xpert.faces.conversion;

import com.xpert.core.conversion.Mask;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * Conversor para cpf, na tela exibe com a mascara e ao submeter, remove a
 * mascara
 *
 * @author Ayslan
 */
@FacesConverter(value = "cpfConverter")
public class CpfConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        String cpf = "";

        if (value != null) {
            cpf = value.replaceAll("[^\\d]", "");
        }

        return cpf;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {

        String cpf = "";

        if (value != null && !value.toString().isEmpty()) {
            cpf = Mask.maskCpf(value.toString());
        }

        return cpf;
    }
}
