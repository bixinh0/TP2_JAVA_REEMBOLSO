package br.edu.infnet;

// DUBLÊ DE TESTE: STUB
// Simula um plano de saúde com resposta FIXA e pré-programada: sempre 50%.
// O stub não tem regra nenhuma; ele só devolve o valor de que o teste
// precisa, para que o resultado do cálculo seja previsível.
class PlanoSaudeBasicoStub implements PlanoSaude {

    @Override
    public double getPercentualCobertura() {
        return 50.0;
    }
}
