package br.edu.infnet;

// DUBLÊ DE TESTE: STUB
// Simula um segundo plano de saúde, também com resposta fixa: sempre 80%.
class PlanoSaudePremiumStub implements PlanoSaude {

    @Override
    public double getPercentualCobertura() {
        return 80.0;
    }
}
