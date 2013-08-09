package com.xpert.faces.validation;

class Validation {

    public static boolean validateCPF(String cpf) {

        cpf = cpf.replaceAll("[^\\d]", "");
        boolean valid = false;
        if (cpf.length() < 11) {
            return valid;
        }
        if (cpf.equals("11111111111")) {
            return valid;
        }
        if (cpf.equals("22222222222")) {
            return valid;
        }
        if (cpf.equals("33333333333")) {
            return valid;
        }
        if (cpf.equals("44444444444")) {
            return valid;
        }
        if (cpf.equals("55555555555")) {
            return valid;
        }
        if (cpf.equals("66666666666")) {
            return valid;
        }
        if (cpf.equals("77777777777")) {
            return valid;
        }
        if (cpf.equals("88888888888")) {
            return valid;
        }
        if (cpf.equals("99999999999")) {
            return valid;
        }
        if (cpf.equals("00000000000")) {
            return valid;
        }

        int d1, d2;
        int digit1, digit2, resto;
        int digitCPF;
        String nDigResult;

        d1 = d2 = 0;
        digit1 = digit2 = resto = 0;

        for (int nCount = 1; nCount < cpf.length() - 1; nCount++) {
            digitCPF = Integer.valueOf(cpf.substring(nCount - 1, nCount)).intValue();

            // multiplique a ultima casa por 2 a seguinte por 3 a seguinte por 4 e assim por diante.
            d1 = d1 + (11 - nCount) * digitCPF;

            // para o segundo digit repita o procedimento incluindo o primeiro digit calculado no passo anterior.
            d2 = d2 + (12 - nCount) * digitCPF;
        }

        // Primeiro resto da divis�o por 11.
        resto = (d1 % 11);

        // Se o resultado for 0 ou 1 o digit � 0 caso contr�rio o digit � 11 menos o resultado anterior.
        if (resto < 2) {
            digit1 = 0;
        } else {
            digit1 = 11 - resto;
        }

        d2 += 2 * digit1;

        // Segundo resto da divis�o por 11.
        resto = (d2 % 11);

        // Se o resultado for 0 ou 1 o digit � 0 caso contr�rio o digit � 11 menos o resultado anterior.
        if (resto < 2) {
            digit2 = 0;
        } else {
            digit2 = 11 - resto;
        }

        // digit verificador do CPF que est� sendo validado.
        String nDigVerific = cpf.substring(cpf.length() - 2, cpf.length());

        // Concatenando o primeiro resto com o segundo.
        nDigResult = String.valueOf(digit1) + String.valueOf(digit2);

        // comparar o digit verificador do cpf com o primeiro resto + o segundo resto.
        return nDigVerific.equals(nDigResult);
    }

    public static boolean validateCNPJ(String cnpj) {
        @SuppressWarnings("unused")
        int soma = 0, dig;

        cnpj = cnpj.replaceAll("[^\\d]", "");

        if (cnpj.length() != 14) {
            return false;
        }

        String cnpj_calc = cnpj.substring(0, 12);

        char[] chr_cnpj = cnpj.toCharArray();

        // Primeira parte
        for (int i = 0; i < 4; i++) {
            if (chr_cnpj[i] - 48 >= 0 && chr_cnpj[i] - 48 <= 9) {
                soma += (chr_cnpj[i] - 48) * (6 - (i + 1));
            }
        }
        for (int i = 0; i < 8; i++) {
            if (chr_cnpj[i + 4] - 48 >= 0 && chr_cnpj[i + 4] - 48 <= 9) {
                soma += (chr_cnpj[i + 4] - 48) * (10 - (i + 1));
            }
        }
        dig = 11 - (soma % 11);

        cnpj_calc += (dig == 10 || dig == 11) ? "0" : Integer.toString(dig);

        // Segunda parte
        soma = 0;
        for (int i = 0; i < 5; i++) {
            if (chr_cnpj[i] - 48 >= 0 && chr_cnpj[i] - 48 <= 9) {
                soma += (chr_cnpj[i] - 48) * (7 - (i + 1));
            }
        }
        for (int i = 0; i < 8; i++) {
            if (chr_cnpj[i + 5] - 48 >= 0 && chr_cnpj[i + 5] - 48 <= 9) {
                soma += (chr_cnpj[i + 5] - 48) * (10 - (i + 1));
            }
        }
        dig = 11 - (soma % 11);
        cnpj_calc += (dig == 10 || dig == 11) ? "0" : Integer.toString(dig);

        return cnpj.equals(cnpj_calc);
    }

    public static boolean validateLongitude(String coordenada) {
        coordenada = coordenada.toUpperCase();
        try {
            if (coordenada.charAt(3) != 'º'
                    || coordenada.charAt(6) != '\''
                    || !coordenada.substring(9, 11).equals("\'\'")
                    ) {
                return false;
            }
            int graus = Integer.parseInt(coordenada.substring(0, 3));
            int minutos = Integer.parseInt(coordenada.substring(4, 6));
            int segundos = Integer.parseInt(coordenada.substring(7, 9));
            if (graus > 180
                    || (graus == 180 && (minutos != 0 || segundos != 0))
                    || (graus < 180 && (minutos > 59 || segundos > 59))) {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
    
    public static boolean validateLatitude(String coordenada) {
        coordenada = coordenada.toUpperCase();
        try {
            if (coordenada.charAt(2) != 'º'
                    || coordenada.charAt(5) != '\''
                    || !coordenada.substring(8, 10).equals("\'\'")
                    ) {
                return false;
            }
            int graus = Integer.parseInt(coordenada.substring(0, 2));
            int minutos = Integer.parseInt(coordenada.substring(3, 5));
            int segundos = Integer.parseInt(coordenada.substring(6, 8));
            if (graus > 90
                    || (graus == 90 && (minutos != 0 || segundos != 0))
                    || (graus < 90 && (minutos > 59 || segundos > 59))) {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
    
}
