package com.fiap.territory.servico;

import com.fiap.territory.dominio.EstrategiaPrevisao;
import com.fiap.territory.dominio.PrevisaoComparacao;
import com.fiap.territory.dominio.PrevisaoMediaMovel;
import com.fiap.territory.excecao.DadosInvalidosException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

//Factory com mapa tipo -> criador. A janela vem da ConfiguracaoSistema (quem chama repassa).
@Component
public class FabricaPrevisao {

    @FunctionalInterface
    private interface Criador {
        EstrategiaPrevisao criar(int janela) throws DadosInvalidosException;
    }

    private final Map<TipoPrevisao, Criador> criadores = new EnumMap<>(TipoPrevisao.class);

    public FabricaPrevisao() {
        criadores.put(TipoPrevisao.MEDIA_MOVEL, PrevisaoMediaMovel::new);
        // DECISAO: a comparação usa dois períodos de metade da janela (arredondada pra cima): janela 5 -> 2 x 3 = 6 leituras.
        criadores.put(TipoPrevisao.COMPARACAO, janela -> new PrevisaoComparacao((janela + 1) / 2));
    }

    public EstrategiaPrevisao criar(TipoPrevisao tipo, int janela) throws DadosInvalidosException {
        Criador criador = tipo == null ? null : criadores.get(tipo);
        if (criador == null) {
            throw new DadosInvalidosException("Tipo de previsão desconhecido ou nulo: " + tipo);
        }
        return criador.criar(janela);
    }
}
