package br.edu.infnet;

// DUBLÊ DE TESTE: SPY
// Implementa a Auditoria só para "espionar" o que o serviço faz com ela:
// guarda SE o método registrarConsulta() foi chamado, QUANTAS vezes e com
// QUAL consulta. Depois, o teste consulta essas informações para verificar
// a interação, e não apenas o valor de retorno.
class AuditoriaSpy implements Auditoria {

    private int quantidadeChamadas = 0;
    private Consulta ultimaConsultaRegistrada;

    @Override
    public void registrarConsulta(Consulta consulta) {
        quantidadeChamadas++;
        ultimaConsultaRegistrada = consulta;
    }

    public boolean foiChamado() {
        return quantidadeChamadas > 0;
    }

    public int getQuantidadeChamadas() {
        return quantidadeChamadas;
    }

    public Consulta getUltimaConsultaRegistrada() {
        return ultimaConsultaRegistrada;
    }
}
