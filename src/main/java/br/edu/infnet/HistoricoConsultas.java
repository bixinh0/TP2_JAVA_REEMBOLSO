package br.edu.infnet;

import java.util.List;

// ETAPA 5 - Contrato do histórico de consultas.
// A interface diz O QUE o histórico oferece, mas não COMO os dados são
// guardados. Em produção poderia ser um banco de dados; nos testes, um fake.
public interface HistoricoConsultas {

    void registrar(Consulta consulta);

    List<Consulta> listarTodas();

    List<Consulta> listarPorPaciente(Paciente paciente);
}
