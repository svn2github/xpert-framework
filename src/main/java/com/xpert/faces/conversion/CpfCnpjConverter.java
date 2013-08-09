package com.xpert.faces.conversion;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * Conversor para cpf e cnpj, na tela exibe com a mascara e ao submeter, remove
 * a mascara
 *
 * @author Ayslan
 */
@FacesConverter(value = "cpfCnpjConverter")
public class CpfCnpjConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        String cpfCnpj = "";

        if (value != null) {
            cpfCnpj = value.replaceAll("[^\\d]", "");
        }

        return cpfCnpj;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {

        String cpfCnpj = "";

        if (value != null && !value.toString().isEmpty()) {
            if (value.toString().length() > 11) {
                cpfCnpj = Mask.maskCnpj(value.toString());
            } else {
                cpfCnpj = Mask.maskCpf(value.toString());
            }
        }

        return cpfCnpj;
    }
}
