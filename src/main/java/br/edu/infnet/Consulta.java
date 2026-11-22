package br.edu.infnet;

import java.time.LocalDate;

// ETAPA 5 - Consulta médica que será reembolsada e guardada no histórico
public class Consulta {

    private final Paciente paciente;
    private final double valor;
    private final String especialidade;
    private final LocalDate data;

    public Consulta(Paciente paciente, double valor, String especialidade, LocalDate data) {
        this.paciente = paciente;
        this.valor = valor;
        this.especialidade = especialidade;
        this.data = data;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public double getValor() {
        return valor;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public LocalDate getData() {
        return data;
    }
}
