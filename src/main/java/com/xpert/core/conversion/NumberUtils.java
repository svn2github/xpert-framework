package com.xpert.core.conversion;

import com.xpert.i18n.I18N;
import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 *
 * @author Ayslan
 */
public class NumberUtils {

    public static String convertToMoney(BigDecimal valor) {

        NumberFormat nf = NumberFormat.getCurrencyInstance(I18N.getLocale());
        String retorno = nf.format(valor);

        return retorno;
    }

    public static String convertToNumber(BigDecimal valor) {

        NumberFormat numberFormat = getNumberFormat();
        String retorno = numberFormat.format(valor);

        return retorno;
    }

    public static String convertToMoney(Double valor) {

        NumberFormat nf = NumberFormat.getCurrencyInstance(I18N.getLocale());
        String retorno = nf.format(valor);

        return retorno;
    }

    public static String convertToNumber(Double valor) {

        NumberFormat numberFormat = getNumberFormat();
        String retorno = numberFormat.format(valor);

        return retorno;
    }

    public static String convertToNumber(String valor) {

        NumberFormat numberFormat = getNumberFormat();
        String retorno = numberFormat.format(new BigDecimal(valor));

        return retorno;
    }

    public static NumberFormat getNumberFormat() {
        return getNumberFormat(2, 2);
    }

    public static NumberFormat getNumberFormat(int maximumFractionDigits, int minimumFractionDigits) {

        NumberFormat numberFormat = NumberFormat.getNumberInstance(I18N.getLocale());
        numberFormat.setMaximumFractionDigits(maximumFractionDigits);
        numberFormat.setMinimumFractionDigits(minimumFractionDigits);

        return numberFormat;
    }
}
