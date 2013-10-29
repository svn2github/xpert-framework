package com.xpert.utils;

/**
 *
 * @author Ayslan
 */
public class Mod11 {

    public static String getDV(String number) {

        return getDV(number, false);
    }

    public static String getDV(String number, int quantidadeDigitos) {

        return getDV(number, false, quantidadeDigitos);
    }

    /**
     * Calcular um dígito verificador a partir de uma sequência de números
     * enviada.
     *
     * @param number - Sequência de números para cálculo do DV
     * @param dezPorX Indica se deve haver substituição de resultado 10 por X
     * durante o cálculo - padrão usado em alguns lugares
     * @return DV gerado.
     */
    public static String getDV(String number, boolean dezPorX) {

        if (!org.apache.commons.lang.StringUtils.isNumeric(number)) {
            throw new IllegalArgumentException("Value is not number");
        }

        int peso = number.length() + 1;
        int dv = 0;
        for (int i = 0; i < number.length(); i++) {
            dv += Integer.parseInt(number.substring(i, i + 1)) * peso--;
        }
        dv = dv % 11;
        if (dv > 1) {
            return String.valueOf(11 - dv);
        } else if (dv == 1 && dezPorX) {
            return "X";
        }
        return "0";

    }

    /**
     * Calcular um dígito verificador com a quantidade de casas indicadas a
     * partir de uma sequência de números enviada.
     *
     * @param number - Sequência de números para cálculo do DV
     * @param dezPorX Indica se deve haver substituição de resultado 10 por X
     * durante o cálculo - padrão usado em alguns lugares
     * @param quantidadeDigitos Quantidade de dígitos a serem retornados
     * @return DV gerado.
     */
    public static String getDV(String number, boolean dezPorX, int quantidadeDigitos) {
        if (quantidadeDigitos > 1) {
            String parcial = getDV(number, dezPorX);
            return parcial + getDV(number + parcial, dezPorX, --quantidadeDigitos);
        } else {
            return getDV(number, dezPorX);
        }
    }

    /**
     * Calcular um dígito verificador a partir de uma sequência de números
     * enviada. O maior peso usado é 9, retornando a 2.
     *
     * @param number - Sequência de números para cálculo do DV
     * @param dezPorX Indica se deve haver substituição de resultado 10 por X
     * durante o cálculo - padrão usado em alguns lugares
     * @return DV gerado.
     */
    public static String getDVBase10(String number, boolean dezPorX) {
        char subUm = '0';
        if (dezPorX) {
            subUm = 'X';
        }
        return getDVBaseParametrizada(number, 10, '0', subUm);
    }

    /**
     * Calcular um dígito verificador usando o módulo 11, base 10, com a
     * quantidade de casas indicadas a partir de uma sequência de números
     * enviada.
     *
     * @param number - Sequência de números para cálculo do DV
     * @param dezPorX Indica se deve haver substituição de resultado 10 por X
     * durante o cálculo - padrão usado em alguns lugares
     * @param quantidadeDigitos Quantidade de dígitos a serem retornados
     * @return DV gerado.
     */
    public static String getDVBase10(String number, boolean dezPorX, int quantidadeDigitos) {
        char subUm = '0';
        if (dezPorX) {
            subUm = 'X';
        }
        return getDVBaseParametrizada(number, 10, '0', subUm, quantidadeDigitos);
    }

    /**
     * Calcular um dígito verificador a partir de uma sequência de números
     * enviada. O maior peso usado atinge a base, retorna a 2
     *
     * @param number - Sequência de números para cálculo do DV
     * @param base Valor da base que se deseja usar para o cálculo do DV
     * @param subZero Caracter que deve substituir o resultado quando o resto
     * for 0
     * @param subUm Caracter que deve substituir o resultado quando o resto for
     * 1
     * @return DV gerado.
     */
    public static String getDVBaseParametrizada(String number, int base, char subZero, char subUm) {

        if (!org.apache.commons.lang.StringUtils.isNumeric(number)) {
            throw new IllegalArgumentException("Value is not number");
        }

        int peso = 2;
        int dv = 0;
        for (int i = number.length() - 1; i >= 0; i--) {
            dv += Integer.parseInt(number.substring(i, i + 1)) * peso;
            if (peso == base - 1) {
                peso = 2;
            } else {
                peso++;
            }
        }
        dv = dv % 11;
        if (dv > 1) {
            return String.valueOf(11 - dv);
        } else if (dv == 1) {
            return String.valueOf(subUm);
        }
        return String.valueOf(subZero);

    }

    /**
     * Calcular um dígito verificador usando o módulo 11, base 10, com a
     * quantidade de casas indicadas a partir de uma sequência de números
     * enviada.
     *
     * @param number - Sequência de números para cálculo do DV
     * @param base Valor da base que se deseja usar para o cálculo do DV
     * @param subZero Caracter que deve substituir o resultado quando o resto
     * for 0
     * @param subUm Caracter que deve substituir o resultado quando o resto for
     * 1
     * @param quantidadeDigitos Quantidade de dígitos a serem retornados
     * @return DV gerado.
     */
    public static String getDVBaseParametrizada(String fonte, int base,
            char subZero, char subUm, int quantidadeDigitos) {
        if (quantidadeDigitos > 1) {
            String parcial = getDVBaseParametrizada(fonte, base, subZero, subUm);
            return parcial + getDVBaseParametrizada(fonte + parcial, base, subZero, subUm, --quantidadeDigitos);
        } else {
            return getDVBaseParametrizada(fonte, base, subZero, subUm);
        }
    }

    /**
     * Calcular um dígito verificador a partir de uma sequência de números
     * enviada e uma constante a ser acrescida ao somatório. O maior peso usado
     * atinge a base, retorna a 2
     *
     * @param number - Sequência de números para cálculo do DV
     * @param base Valor da base que se deseja usar para o cálculo do DV
     * @param subZero Caracter que deve substituir o resultado quando o resto
     * for 0
     * @param subUm Caracter que deve substituir o resultado quando o resto for
     * 1
     * @param constante Valor que deve ser acrescido ao somatório durante o
     * cálculo
     * @return DV gerado.
     */
    public static String getDVBaseParametrizadaComConstante(String number, int base, char subZero, char subUm, int constante) {
        if (!org.apache.commons.lang.StringUtils.isNumeric(number)) {
            throw new IllegalArgumentException("Value is not number");
        }
        int peso = 2;
        int dv = constante;
        for (int i = number.length() - 1; i >= 0; i--) {
            dv += Integer.parseInt(number.substring(i, i + 1)) * peso;
            if (peso == base - 1) {
                peso = 2;
            } else {
                peso++;
            }
        }
        dv = dv % 11;
        if (dv > 1) {
            return String.valueOf(11 - dv);
        } else if (dv == 1) {
            return String.valueOf(subUm);
        }
        return String.valueOf(subZero);
    }

    public static String getMod11(String string) {
        int d1, d2;
        int digito1, digito2, resto;
        int digitoVerificador;
        String nDigResult;

        d1 = d2 = 0;
        digito1 = digito2 = resto = 0;

        for (int nCount = 1; nCount <= string.length(); nCount++) {
            digitoVerificador = Integer.valueOf(string.substring(nCount - 1, nCount)).intValue();

            //multiplique a ultima casa por 2 a seguinte por 3 a seguinte por 4 e assim por diante.
            d1 = d1 + (11 - nCount) * digitoVerificador;

            //para o segundo digito repita o procedimento incluindo o primeiro digito calculado no passo anterior.
            d2 = d2 + (12 - nCount) * digitoVerificador;
        }

        //Primeiro resto da divisão por 11.
        resto = (d1 % 11);

        //Se o resultado for 0 ou 1 o digito é 0 caso contrário o digito é 11 menos o resultado anterior.
        if (resto < 2) {
            digito1 = 0;

        } else {
            digito1 = 11 - resto;


        }
        d2 += 2 * digito1;

        //Segundo resto da divisão por 11.
        resto = (d2 % 11);

        //Se o resultado for 0 ou 1 o digito é 0 caso contrário o digito é 11 menos o resultado anterior.
        if (resto < 2) {
            digito2 = 0;

        } else {
            digito2 = 11 - resto;

            //Digito verificador do CPF que está sendo validado.

        }

        //Concatenando o primeiro resto com o segundo.
        nDigResult = String.valueOf(digito1) + String.valueOf(digito2);

        //comparar o digito verificador do cpf com o primeiro resto + o segundo resto.
        return nDigResult;
    }

    public static void main(String[] args) {
        System.out.println(getDV("005610443", 2));
        System.out.println("fim");
    }
}
