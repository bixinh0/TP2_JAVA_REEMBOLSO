package br.edu.infnet;

import java.util.ArrayList;
import java.util.List;

// DUBLÊ DE TESTE: FAKE
// Implementação simplificada, mas FUNCIONAL, do HistoricoConsultas.
// Em vez de um banco de dados real, guarda as consultas em uma lista em
// memória. Os dados registrados podem ser consultados depois, o que permite
// testar o serviço sem banco, sem configuração e de forma rápida.
class HistoricoConsultasFake implements HistoricoConsultas {

    // List (interface) + ArrayList (classe): a "tabela" do fake
    private final List<Consulta> consultas = new ArrayList<>();

    @Override
    public void registrar(Consulta consulta) {
        consultas.add(consulta);
    }

    @Override
    public List<Consulta> listarTodas() {
        // devolve uma cópia para o teste não alterar a lista interna
        return new ArrayList<>(consultas);
    }

    @Override
    public List<Consulta> listarPorPaciente(Paciente paciente) {
        return consultas.stream()
                .filter(consulta -> consulta.getPaciente() == paciente)
                .toList();
    }
}
