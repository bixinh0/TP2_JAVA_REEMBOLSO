package br.edu.infnet;

// ETAPA 8 - Contrato do serviço que autoriza (ou não) o reembolso.
// Em produção seria um sistema externo da operadora; nos testes, um mock.
public interface AutorizadorReembolso {

    boolean autorizar(Consulta consulta);
}
