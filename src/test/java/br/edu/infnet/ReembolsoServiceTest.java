package br.edu.infnet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static br.edu.infnet.ReembolsoTestHelper.assertValorAproximado;
import static br.edu.infnet.ReembolsoTestHelper.criarConsulta;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

// Testes do ReembolsoService: aqui o serviço é testado com dublês no lugar
// das dependências reais (banco de dados, serviços externos etc.).
class ReembolsoServiceTest {

    private HistoricoConsultasFake historicoFake;
    private AuditoriaSpy auditoriaSpy;
    private AutorizadorReembolso autorizadorMock;
    private ReembolsoService servico;

    @BeforeEach
    void setUp() {
        // FAKE: histórico em memória no lugar de um banco de dados real
        historicoFake = new HistoricoConsultasFake();
        // SPY: auditoria que registra se e como foi chamada
        auditoriaSpy = new AuditoriaSpy();
        // MOCK (Mockito): autorizador criado a partir da própria interface,
        // sem nenhuma classe de implementação. Por padrão um mock devolve
        // false para métodos boolean, então configuramos aqui que, salvo
        // indicação contrária no teste, toda consulta é autorizada.
        autorizadorMock = mock(AutorizadorReembolso.class);
        when(autorizadorMock.autorizar(any(Consulta.class))).thenReturn(true);
        // STUB: plano com cobertura fixa de 50%
        servico = new ReembolsoService(new CalculadoraReembolso(), new PlanoSaudeBasicoStub(),
                historicoFake, auditoriaSpy, autorizadorMock);
    }


    // ETAPA 5 - FAKE PARA HISTÓRICO DE CONSULTAS

    @Test
    void deveIniciarComHistoricoVazio() {
        assertTrue(historicoFake.listarTodas().isEmpty());
    }

    @Test
    void deveRegistrarConsultaNoHistoricoAoProcessarReembolso() {
        Consulta consulta = criarConsulta();                       // ETAPA 9: helper

        double reembolso = servico.processarReembolso(consulta);

        // o cálculo continua correto (50% de 200)...
        assertValorAproximado(100.0, reembolso);
        // ...e o fake guardou a consulta, que pode ser consultada depois
        List<Consulta> registradas = historicoFake.listarTodas();
        assertEquals(1, registradas.size());
        assertSame(consulta, registradas.get(0));
    }

    @Test
    void deveConsultarHistoricoPorPaciente() {
        Paciente ana = new Paciente("Ana");
        Paciente bruno = new Paciente("Bruno");
        servico.processarReembolso(criarConsulta(ana));            // ETAPA 9: helper
        servico.processarReembolso(criarConsulta(bruno));
        servico.processarReembolso(criarConsulta(ana));

        assertEquals(3, historicoFake.listarTodas().size());
        assertEquals(2, historicoFake.listarPorPaciente(ana).size());
        assertEquals(1, historicoFake.listarPorPaciente(bruno).size());
    }


    // ETAPA 6 - STUB DE PLANO DE SAÚDE

    @Test
    void deveUsarOPercentualDoPlanoInformadoAoServico() {
        // serviço montado com o stub premium (80%) no lugar do básico
        ReembolsoService servicoPremium = new ReembolsoService(new CalculadoraReembolso(),
                new PlanoSaudePremiumStub(), historicoFake, auditoriaSpy, autorizadorMock);

        assertValorAproximado(120.0, servicoPremium.processarReembolso(criarConsulta(150.0))); // 80% de 150
    }


    // ETAPA 7 - SPY PARA AUDITORIA

    @Test
    void naoDeveChamarAuditoriaAntesDeProcessarReembolso() {
        assertFalse(auditoriaSpy.foiChamado());
    }

    @Test
    void deveChamarAuditoriaAoRegistrarConsulta() {
        Consulta consulta = criarConsulta();                       // ETAPA 9: helper

        servico.processarReembolso(consulta);

        // o spy "conta" o que aconteceu durante o teste
        assertTrue(auditoriaSpy.foiChamado());
        assertEquals(1, auditoriaSpy.getQuantidadeChamadas());
        assertSame(consulta, auditoriaSpy.getUltimaConsultaRegistrada());
    }

    @Test
    void deveChamarAuditoriaUmaVezParaCadaConsulta() {
        servico.processarReembolso(criarConsulta());               // ETAPA 9: helper
        servico.processarReembolso(criarConsulta());
        servico.processarReembolso(criarConsulta());

        assertEquals(3, auditoriaSpy.getQuantidadeChamadas());
    }


    // ETAPA 8 - MOCK PARA AUTORIZAÇÃO
    // when(...).thenReturn(...) programa a resposta do mock;
    // verify(...) confirma que o serviço realmente conversou com ele.

    @Test
    void deveReembolsarConsultaAutorizada() {
        Consulta consulta = criarConsulta();                       // ETAPA 9: helper
        when(autorizadorMock.autorizar(consulta)).thenReturn(true);

        double reembolso = servico.processarReembolso(consulta);

        assertValorAproximado(100.0, reembolso);
        verify(autorizadorMock).autorizar(consulta);       // consultou o autorizador
        verifyNoMoreInteractions(autorizadorMock);         // e só uma vez
    }

    @Test
    void deveLancarExcecaoQuandoConsultaNaoForAutorizada() {
        Consulta consulta = criarConsulta();                       // ETAPA 9: helper
        // o mock passa a simular um autorizador que RECUSA esta consulta
        when(autorizadorMock.autorizar(consulta)).thenReturn(false);

        ReembolsoNaoAutorizadoException excecao = assertThrows(ReembolsoNaoAutorizadoException.class,
                () -> servico.processarReembolso(consulta));

        assertEquals("Consulta não autorizada para reembolso", excecao.getMessage());
        verify(autorizadorMock, times(1)).autorizar(consulta);
        // nada pode ter sido registrado quando a consulta é recusada
        assertTrue(historicoFake.listarTodas().isEmpty());
        assertFalse(auditoriaSpy.foiChamado());
    }

    @Test
    void naoDeveConsultarAutorizadorQuandoNenhumReembolsoForProcessado() {
        // nenhum processamento => nenhuma interação com o mock
        verify(autorizadorMock, never()).autorizar(any(Consulta.class));
    }

    // ETAPA 11 - REGRA DE TETO NO SERVIÇO

    @Test
    void deveRegistrarReembolsoLimitadoAoTeto() {
        // plano básico: 50% de R$ 1.000 = R$ 500 -> limitado a R$ 150
        double reembolso = servico.processarReembolso(criarConsulta(1000.0));

        assertValorAproximado(150.0, reembolso);
        assertEquals(1, historicoFake.listarTodas().size());
    }
}
