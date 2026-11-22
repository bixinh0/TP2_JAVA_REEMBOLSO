package br.edu.infnet;

// Serviço que coordena o processo de reembolso.
// As dependências chegam pelo construtor (injeção de dependência), sempre
// pelo tipo da interface. Assim, nos testes, podemos entregar dublês.
public class ReembolsoService {

    private final CalculadoraReembolso calculadora;
    private final PlanoSaude plano;                 // ETAPA 6
    private final HistoricoConsultas historico;      // ETAPA 5
    private final Auditoria auditoria;               // ETAPA 7
    private final AutorizadorReembolso autorizador;  // ETAPA 8

    public ReembolsoService(CalculadoraReembolso calculadora, PlanoSaude plano,
                            HistoricoConsultas historico, Auditoria auditoria,
                            AutorizadorReembolso autorizador) {
        this.calculadora = calculadora;
        this.plano = plano;
        this.historico = historico;
        this.auditoria = auditoria;
        this.autorizador = autorizador;
    }

    public double processarReembolso(Consulta consulta) {
        // ETAPA 8: só se pode reembolsar consultas autorizadas.
        // A verificação vem antes de tudo: se for recusada, nada é
        // calculado, registrado no histórico ou enviado à auditoria.
        if (!autorizador.autorizar(consulta)) {
            throw new ReembolsoNaoAutorizadoException();
        }

        double reembolso = calculadora.calcular(consulta.getValor(), plano, consulta.getPaciente());
        historico.registrar(consulta);
        auditoria.registrarConsulta(consulta);
        return reembolso;
    }
}
