package br.com.agenteingles.agente.simulado;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.agenteingles.agente.DesafioGerado;
import br.com.agenteingles.agente.PedidoDeGeracao;
import br.com.agenteingles.desafio.FormatoDoDesafio;
import br.com.agenteingles.modulo.NivelCefr;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GeradorDeDesafioSimuladoTest {

    private final GeradorDeDesafioSimulado gerador = new GeradorDeDesafioSimulado();

    private PedidoDeGeracao pedido(List<String> enunciadosRecentes, List<String> errosRecentes) {
        return new PedidoDeGeracao(
                1L,
                "verbo_to_be",
                "Verbo \"to be\"",
                "Formas am/is/are no presente.",
                NivelCefr.A1,
                "Viagem",
                "Aeroporto, hotel, restaurante e deslocamento.",
                FormatoDoDesafio.TEXTO,
                new BigDecimal("4.00"),
                errosRecentes,
                enunciadosRecentes);
    }

    @Test
    @DisplayName("o desafio gerado traz enunciado, cena e criterio de avaliacao")
    void desafioGeradoVemCompleto() {
        DesafioGerado desafio = gerador.gerar(pedido(List.of(), List.of()), 1).get(0);

        assertThat(desafio.enunciado()).isNotBlank();
        assertThat(desafio.contextoDaCena()).isNotBlank();
        assertThat(desafio.respostaDeReferencia()).isNotBlank();
        assertThat(desafio.criterioDeAvaliacao()).isNotBlank();
    }

    @Test
    @DisplayName("o gerador nao repete um enunciado ja usado")
    void geradorNaoRepeteEnunciadoJaUsado() {
        // Marca todos menos um como ja usados: o gerador precisa achar justamente o que sobrou.
        List<String> todosOsEnunciados = new ArrayList<>();
        for (int tentativa = 0; tentativa < 400; tentativa++) {
            todosOsEnunciados.add(gerador.gerar(pedido(List.of(), List.of()), 1).get(0).enunciado());
        }
        List<String> distintos = todosOsEnunciados.stream().distinct().toList();
        assertThat(distintos).hasSizeGreaterThan(5);

        List<String> jaUsados = new ArrayList<>(distintos.subList(0, distintos.size() - 1));
        String unicoDisponivel = distintos.get(distintos.size() - 1);

        DesafioGerado desafio = gerador.gerar(pedido(jaUsados, List.of()), 1).get(0);

        assertThat(desafio.enunciado()).isEqualTo(unicoDisponivel);
    }

    @Test
    @DisplayName("a cena avisa qual erro recente esta sendo reforcado")
    void cenaAvisaOErroReforcado() {
        DesafioGerado desafio = gerador.gerar(pedido(List.of(), List.of("concordancia_do_verbo_to_be")), 1).get(0);

        assertThat(desafio.contextoDaCena()).contains("concordancia do verbo to be");
    }

    @Test
    @DisplayName("a cena vem do tema escolhido")
    void cenaVemDoTemaEscolhido() {
        DesafioGerado desafio = gerador.gerar(pedido(List.of(), List.of()), 1).get(0);

        assertThat(desafio.contextoDaCena())
                .containsAnyOf("aeroporto", "hotel", "restaurante");
    }
}
