package br.edu.infnet;

// ETAPA 3 - CLASSE DEDICADA
// Toda a regra do cálculo fica concentrada nesta classe, que não depende
// de banco, arquivo, rede ou outra classe do sistema.
public class CalculadoraReembolso {

    // ETAPA 11: valor máximo de reembolso por consulta
    public static final double TETO_REEMBOLSO = 150.0;

    // ETAPA 4: o método passa a receber também o Paciente.
    // O paciente ainda não participa do cálculo; ele só faz parte da assinatura.
    public double calcular(double valorConsulta, double percentualCobertura, Paciente paciente) {
        validar(valorConsulta, percentualCobertura);
        double reembolso = valorConsulta * percentualCobertura / 100;
        return aplicarTeto(reembolso);
    }

    // ETAPA 6: o percentual pode vir do plano de saúde do paciente
    public double calcular(double valorConsulta, PlanoSaude plano, Paciente paciente) {
        return calcular(valorConsulta, plano.getPercentualCobertura(), paciente);
    }

    // ETAPA 11: nenhum reembolso pode passar de R$ 150 por consulta
    private double aplicarTeto(double reembolso) {
        return Math.min(reembolso, TETO_REEMBOLSO);
    }

    // 0 e 100 são limites válidos; fora da faixa, a entrada é rejeitada
    private void validar(double valorConsulta, double percentualCobertura) {
        if (valorConsulta < 0) {
            throw new IllegalArgumentException("Valor da consulta não pode ser negativo");
        }
        if (percentualCobertura < 0 || percentualCobertura > 100) {
            throw new IllegalArgumentException("Percentual de cobertura deve estar entre 0 e 100");
        }
    }
}
