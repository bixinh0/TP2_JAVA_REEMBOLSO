package br.edu.infnet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static br.edu.infnet.ReembolsoTestHelper.assertValorAproximado;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// ETAPA 3 - TESTE ISOLADO EM CLASSE DEDICADA
// Esta classe testa SOMENTE a CalculadoraReembolso. Nenhum teste aqui
// depende de banco de dados, rede, arquivos ou de outras classes do sistema.
class CalculadoraReembolsoTest {

    // Test fixture: objeto testado, recriado antes de cada teste
    private CalculadoraReembolso calculadora;

    // ETAPA 4 - DUMMY: só preenche o parâmetro Paciente da assinatura
    private Paciente pacienteDummy;

    @BeforeEach
    void setUp() {
        calculadora = new CalculadoraReembolso();
        pacienteDummy = new PacienteDummy();
    }

    @AfterEach
    void tearDown() {
        calculadora = null;
        pacienteDummy = null;
    }


    // ETAPA 1 - REEMBOLSO BÁSICO

    @Test
    void deveReembolsar140ParaConsultaDe200Com70PorCentoDeCobertura() {
        // FASE 1 - SETUP: define os valores de entrada
        double valorConsulta = 200.0;
        double percentualCobertura = 70.0;

        // FASE 2 - EXECUTION: o dummy entra só para satisfazer a assinatura
        double reembolso = calculadora.calcular(valorConsulta, percentualCobertura, pacienteDummy);

        // FASE 3 - ASSERTION: R$ 200 com 70% de cobertura = R$ 140
        assertValorAproximado(140.0, reembolso);
    }


    // ETAPA 2 - CASOS DE BORDA

    @ParameterizedTest(name = "consulta de R$ {0} com {1}% de cobertura = R$ {2}")
    @CsvSource({
            "0,   70,  0.0",    // valor da consulta igual a 0
            "100, 70,  70.0",   // valor da consulta igual a 100
            "200, 0,   0.0",    // cobertura igual a 0%
            "120, 100, 120.0",  // cobertura igual a 100% (reembolso integral)
            "0,   0,   0.0",    // valor e cobertura iguais a 0
            "100, 100, 100.0"   // valor e cobertura iguais a 100
    })
    void deveCalcularReembolsoNosValoresLimite(double valorConsulta,
                                              double percentualCobertura,
                                              double reembolsoEsperado) {
        assertValorAproximado(reembolsoEsperado,
                calculadora.calcular(valorConsulta, percentualCobertura, pacienteDummy));
    }

    @Test
    void deveLancarExcecaoQuandoPercentualForMaiorQue100() {
        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcular(200.0, 101.0, pacienteDummy));
        assertEquals("Percentual de cobertura deve estar entre 0 e 100", excecao.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoValorDaConsultaForNegativo() {
        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcular(-1.0, 70.0, pacienteDummy));
        assertEquals("Valor da consulta não pode ser negativo", excecao.getMessage());
    }


    // ETAPA 4 - DUMMY PARA PACIENTE
    // Prova que o paciente não interfere no cálculo: com o dummy ou com um
    // paciente "de verdade", o reembolso é o mesmo.
    @Test
    void deveCalcularOMesmoReembolsoIndependenteDoPaciente() {
        double comDummy = calculadora.calcular(200.0, 70.0, pacienteDummy);
        double comPacienteReal = calculadora.calcular(200.0, 70.0, new Paciente("Ana"));

        assertValorAproximado(comDummy, comPacienteReal);
    }

    // ETAPA 6 - STUB DE PLANO DE SAÚDE
    // Os stubs devolvem percentuais fixos (50% e 80%), então o resultado
    // esperado de cada cálculo é conhecido de antemão.

    @Test
    void deveReembolsar50PorCentoComPlanoBasico() {
        PlanoSaude planoBasico = new PlanoSaudeBasicoStub();   // sempre 50%

        double reembolso = calculadora.calcular(100.0, planoBasico, pacienteDummy);

        assertValorAproximado(50.0, reembolso);
    }

    @Test
    void deveReembolsar80PorCentoComPlanoPremium() {
        PlanoSaude planoPremium = new PlanoSaudePremiumStub(); // sempre 80%

        double reembolso = calculadora.calcular(100.0, planoPremium, pacienteDummy);

        assertValorAproximado(80.0, reembolso);
    }

    @Test
    void deveCalcularReembolsosDiferentesParaPlanosDiferentes() {
        double valorConsulta = 120.0;

        double reembolsoBasico = calculadora.calcular(valorConsulta, new PlanoSaudeBasicoStub(), pacienteDummy);
        double reembolsoPremium = calculadora.calcular(valorConsulta, new PlanoSaudePremiumStub(), pacienteDummy);

        assertValorAproximado(60.0, reembolsoBasico);   // 50% de 120
        assertValorAproximado(96.0, reembolsoPremium);  // 80% de 120
    }

    // ETAPA 10 - COMPARAÇÃO COM MARGEM DE ERRO
    // R$ 99,99 com 33,33% de cobertura dá 33,326667 (dízima). Com
    // assertEquals(33.33, ...) o teste falharia; com a margem de 0.01 ele
    // passa, porque a diferença real é de apenas 0,0033.
    @Test
    void deveAceitarDiferencaDecimalDentroDaMargemDeErro() {
        double reembolso = calculadora.calcular(99.99, 33.33, pacienteDummy);

        assertValorAproximado(33.33, reembolso);
    }

    // ETAPA 11 - REGRA DE TETO (R$ 150 por consulta)
    // Testes escritos ANTES de alterar a CalculadoraReembolso (fase RED):
    // na primeira execução, os testes acima do teto falharam.

    @Test
    void deveLimitarReembolsoAoTetoDe150() {
        // 70% de R$ 300 = R$ 210, mas o máximo permitido é R$ 150
        double reembolso = calculadora.calcular(300.0, 70.0, pacienteDummy);

        assertValorAproximado(CalculadoraReembolso.TETO_REEMBOLSO, reembolso);
    }

    @Test
    void deveManterReembolsoQuandoCalculoForExatamenteIgualAoTeto() {
        // 60% de R$ 250 = R$ 150: exatamente no limite, nada muda
        assertValorAproximado(150.0, calculadora.calcular(250.0, 60.0, pacienteDummy));
    }

    @Test
    void naoDeveAlterarReembolsoAbaixoDoTeto() {
        // 70% de R$ 200 = R$ 140: abaixo do teto, o valor é mantido
        assertValorAproximado(140.0, calculadora.calcular(200.0, 70.0, pacienteDummy));
    }

    @Test
    void deveAplicarTetoTambemNoCalculoComPlanoDeSaude() {
        // plano premium: 80% de R$ 500 = R$ 400 -> limitado a R$ 150
        assertValorAproximado(150.0, calculadora.calcular(500.0, new PlanoSaudePremiumStub(), pacienteDummy));
    }
}
