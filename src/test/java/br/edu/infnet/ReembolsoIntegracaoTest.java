package br.edu.infnet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static br.edu.infnet.ReembolsoTestHelper.assertValorAproximado;
import static br.edu.infnet.ReembolsoTestHelper.criarConsulta;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

// ETAPA 12 - INTEGRAÇÃO COM VÁRIOS DUBLÊS
// Teste mais completo, que exercita o fluxo inteiro do ReembolsoService
// com todos os componentes trabalhando juntos:
//   - STUB   PlanoSaudePremiumStub  -> cobertura fixa de 80%
//   - MOCK   AutorizadorReembolso   -> Mockito decide quem é autorizado
//   - FAKE   HistoricoConsultasFake -> guarda as consultas em memória
//   - SPY    AuditoriaSpy           -> registra as chamadas à auditoria
//   - HELPER criarConsulta()        -> monta as consultas com dados padrão
// Além disso, a regra de teto (R$ 150) e a margem de erro (0.01) continuam valendo.
class ReembolsoIntegracaoTest {

    private PlanoSaude planoStub;
    private AutorizadorReembolso autorizadorMock;
    private HistoricoConsultasFake historicoFake;
    private AuditoriaSpy auditoriaSpy;
    private ReembolsoService servico;

    @BeforeEach
    void setUp() {
        planoStub = new PlanoSaudePremiumStub();
        autorizadorMock = mock(AutorizadorReembolso.class);
        historicoFake = new HistoricoConsultasFake();
        auditoriaSpy = new AuditoriaSpy();

        servico = new ReembolsoService(new CalculadoraReembolso(), planoStub,
                historicoFake, auditoriaSpy, autorizadorMock);
    }

    @Test
    void deveProcessarCenarioCompletoDeReembolsoDeUmPaciente() {
        // FASE 1 - SETUP: um paciente com três consultas no mês
        Paciente paciente = new Paciente("Paciente Integração");
        Consulta clinicoGeral = criarConsulta(paciente, 100.0);   // 80% = 80
        Consulta especialista = criarConsulta(paciente, 250.0);   // 80% = 200 -> teto 150
        Consulta semAutorizacao = criarConsulta(paciente, 300.0); // será recusada

        // o mock simula a resposta da operadora para cada consulta
        when(autorizadorMock.autorizar(clinicoGeral)).thenReturn(true);
        when(autorizadorMock.autorizar(especialista)).thenReturn(true);
        when(autorizadorMock.autorizar(semAutorizacao)).thenReturn(false);

        // FASE 2 - EXECUTION
        double reembolsoClinico = servico.processarReembolso(clinicoGeral);
        double reembolsoEspecialista = servico.processarReembolso(especialista);
        assertThrows(ReembolsoNaoAutorizadoException.class,
                () -> servico.processarReembolso(semAutorizacao));

        // FASE 3 - ASSERTION
        // cálculo (stub de 80% + teto de R$ 150), com margem de erro
        assertValorAproximado(80.0, reembolsoClinico);
        assertValorAproximado(150.0, reembolsoEspecialista);
        assertValorAproximado(230.0, reembolsoClinico + reembolsoEspecialista);

        // fake: só as duas consultas autorizadas foram guardadas
        assertEquals(2, historicoFake.listarPorPaciente(paciente).size());
        assertFalse(historicoFake.listarTodas().contains(semAutorizacao));

        // spy: a auditoria foi chamada uma vez para cada consulta registrada
        assertEquals(2, auditoriaSpy.getQuantidadeChamadas());
        assertSame(especialista, auditoriaSpy.getUltimaConsultaRegistrada());

        // mock: cada consulta passou exatamente uma vez pelo autorizador
        verify(autorizadorMock).autorizar(clinicoGeral);
        verify(autorizadorMock).autorizar(especialista);
        verify(autorizadorMock).autorizar(semAutorizacao);
        verifyNoMoreInteractions(autorizadorMock);
    }

    @Test
    void deveManterHistoricosSeparadosParaPacientesDiferentes() {
        Paciente ana = new Paciente("Ana");
        Paciente bruno = new Paciente("Bruno");
        Consulta consultaAna = criarConsulta(ana, 120.0);     // 80% = 96
        Consulta consultaBruno = criarConsulta(bruno);        // padrão R$ 200 -> 160 -> teto 150
        when(autorizadorMock.autorizar(consultaAna)).thenReturn(true);
        when(autorizadorMock.autorizar(consultaBruno)).thenReturn(true);

        assertValorAproximado(96.0, servico.processarReembolso(consultaAna));
        assertValorAproximado(150.0, servico.processarReembolso(consultaBruno));

        assertEquals(1, historicoFake.listarPorPaciente(ana).size());
        assertEquals(1, historicoFake.listarPorPaciente(bruno).size());
        assertEquals(2, auditoriaSpy.getQuantidadeChamadas());
        verify(autorizadorMock).autorizar(consultaAna);
        verify(autorizadorMock).autorizar(consultaBruno);
    }
}
