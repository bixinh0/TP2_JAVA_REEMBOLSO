package br.edu.infnet;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

// ETAPAS 9 e 10 - HELPERS (MÉTODOS AUXILIARES DE TESTE)
// Centraliza a criação de objetos Consulta com dados padrão. Os testes
// deixam de repetir "new Consulta(...)" e, se o construtor de Consulta
// mudar, só este arquivo precisa ser alterado. O helper não altera nenhuma
// regra do sistema: ele apenas organiza o código dos testes.
final class ReembolsoTestHelper {

    // Dados padrão usados quando o teste não precisa de valores específicos
    static final Paciente PACIENTE_PADRAO = new Paciente("Paciente Padrão");
    static final double VALOR_PADRAO = 200.0;
    static final String ESPECIALIDADE_PADRAO = "Clínico Geral";
    static final LocalDate DATA_PADRAO = LocalDate.of(2026, 11, 10);

    // ETAPA 10: diferença máxima aceita entre dois valores em reais
    static final double MARGEM_ERRO = 0.01;

    private ReembolsoTestHelper() {
        // classe utilitária: não deve ser instanciada
    }

    // Consulta totalmente padrão
    static Consulta criarConsulta() {
        return criarConsulta(PACIENTE_PADRAO, VALOR_PADRAO);
    }

    // Consulta padrão com outro valor
    static Consulta criarConsulta(double valor) {
        return criarConsulta(PACIENTE_PADRAO, valor);
    }

    // Consulta padrão para outro paciente
    static Consulta criarConsulta(Paciente paciente) {
        return criarConsulta(paciente, VALOR_PADRAO);
    }

    // Único ponto do código de teste que conhece o construtor de Consulta
    static Consulta criarConsulta(Paciente paciente, double valor) {
        return new Consulta(paciente, valor, ESPECIALIDADE_PADRAO, DATA_PADRAO);
    }


    // ETAPA 10 - COMPARAÇÃO COM MARGEM DE ERRO
    // Contas com double podem gerar resultados como 33.326666... ou
    // 0.30000000000000004. Comparar com assertEquals(esperado, obtido) exige
    // igualdade exata e pode falhar por diferenças irrelevantes. Este método
    // aceita uma diferença de até 0.01 (um centavo) entre os valores.
    static void assertValorAproximado(double esperado, double obtido) {
        assertEquals(esperado, obtido, MARGEM_ERRO,
                () -> "Esperado R$ " + esperado + ", mas foi R$ " + obtido
                        + " (margem de erro de " + MARGEM_ERRO + ")");
    }
}
