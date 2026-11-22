package br.edu.infnet;

// ETAPA 8 - Lançada quando o autorizador recusa o reembolso da consulta
public class ReembolsoNaoAutorizadoException extends RuntimeException {

    public ReembolsoNaoAutorizadoException() {
        super("Consulta não autorizada para reembolso");
    }
}
