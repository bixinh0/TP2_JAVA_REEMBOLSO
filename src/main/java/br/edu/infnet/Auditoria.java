package br.edu.infnet;

// ETAPA 7 - Contrato do serviço de auditoria.
// Toda consulta registrada precisa ser informada à auditoria.
public interface Auditoria {

    void registrarConsulta(Consulta consulta);
}
