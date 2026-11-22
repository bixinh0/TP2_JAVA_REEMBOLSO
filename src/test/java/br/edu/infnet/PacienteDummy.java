package br.edu.infnet;

// DUBLÊ DE TESTE: DUMMY
// Objeto fictício que existe só para preencher o parâmetro Paciente na
// assinatura de CalculadoraReembolso.calcular(). Ele não tem lógica extra
// e nenhum dado dele é lido pelo cálculo: se fosse removido do método,
// o resultado dos testes seria exatamente o mesmo.
class PacienteDummy extends Paciente {

    PacienteDummy() {
        super("Paciente Dummy");
    }
}
