/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xpert.faces.conversion;

/**
 *
 * @author Ayslan
 */
class Mask {

    private static final int CPF_LENGTH = 11;
    private static final int CNPJ_LENGTH = 14;

    public static String maskCpf(String value) {
        if (value != null) {
            value = value.replaceAll("[^\\d]", "");
            if (value.length() < CPF_LENGTH) {
                value = fillZeros(value, CPF_LENGTH);
            }
            return mask(value, "###.###.###-##");
        }
        return null;
    }

    public static String maskCnpj(String value) {
        if (value != null) {
            value = value.replaceAll("[^\\d]", "");
            if (value.length() < CNPJ_LENGTH) {
                value = fillZeros(value, CNPJ_LENGTH);
            }
            return mask(value, "##.###.###/####-##");
        }
        return null;
    }

    public static String fillZeros(String string, int tamanho) {
        String value = "";
        if (string != null && !string.trim().isEmpty()) {
            value = string;
            for (int x = value.length(); x < tamanho; x++) {
                value = "0" + value;
            }
        }
        return value;
    }

    public static String mask(String value, String mask) {
        for (int i = 0; i < value.length(); i++) {
            mask = mask.replaceFirst("#", value.substring(i, i + 1));
        }
        return mask;
    }
}
