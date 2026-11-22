package br.edu.infnet;

// ETAPA 4 - Paciente
// Classe de domínio exigida pela nova assinatura do cálculo.
// Por enquanto nenhuma regra de reembolso usa os dados do paciente.
public class Paciente {

    private final String nome;

    public Paciente(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
