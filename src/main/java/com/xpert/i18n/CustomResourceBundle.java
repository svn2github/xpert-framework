package com.xpert.i18n;

import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * Classe para customizar as mensagens na tela. A função dela é exibir a
 * mensagem do Locale default da aplicação, caso não seja encontrado a mensagem
 * na tela. Por padrão , quando não encontrado o JSF coloca o padrão:
 * ???nomePropriedade??
 *
 * @author Ayslan
 */
public class CustomResourceBundle extends ResourceBundle {

    public CustomResourceBundle() {
    }

    @Override
    public Enumeration<String> getKeys() {
        return parent.getKeys();
    }

    @Override
    protected Object handleGetObject(String key) {
        return I18N.get(key);
    }
}
