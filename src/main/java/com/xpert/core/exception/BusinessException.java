package com.xpert.core.exception;

public class BusinessException extends StackException {

    public BusinessException() {
    }

    public BusinessException(String mensagem, String... parametros) {
        super(mensagem, parametros);
    }
    
    public BusinessException(String mensagem) {
        super(mensagem);
    }

    @Override
    public void check() throws BusinessException {
        if (this.isNotEmpty()) {
            throw this;
        }
    }
}
