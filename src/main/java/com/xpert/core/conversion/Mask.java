package com.xpert.core.conversion;

import java.text.ParseException;
import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;

public class Mask {

    public static String mask(String valor, String mask) {
        JFormattedTextField jTFmask = null;
        try {
            jTFmask = new JFormattedTextField(new MaskFormatter(mask));
            jTFmask.setText(valor);
        } catch (ParseException e) {
        }
        if (jTFmask == null) {
            return "";
        } else {
            return jTFmask.getText();
        }
    }

    public static String mask(double valor, String mask) {
        return mask(Double.toString(valor), mask);
    }

    public static String mask(int valor, String mask) {
        return mask(Integer.toString(valor), mask);
    }

    public static String maskInscricao(String cnpj) {
        return mask(cnpj, "##.###.##-#");
    }

    public static String maskCnpj(String cnpj) {
        return mask(cnpj, "##.###.###/####-##");
    }

    public static String maskCPF(String cpf) {
        return mask(cpf, "###.###.###-##");
    }

    public static String maskCep(String cep) {
        return mask(cep, "##.###-###");
    }

    public static String maskTelefone(String phone) {
        return mask(phone, "(##)####-####");
    }

    public static String maskPlacaCarro(String placa) {
        return mask(placa, "AAA-####");
    }
}
