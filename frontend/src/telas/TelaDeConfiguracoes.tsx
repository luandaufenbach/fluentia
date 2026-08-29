import { useEffect, useState } from "react";
import { api } from "../servicos/api";
import type { ObjetivoDoUsuario, Tema, TipoDeCorrecao, Usuario } from "../tipos";
import "./TelaDeConfiguracoes.css";

/**
 * Conversão de minutos em desafios, espelhando a regra do backend
 * (ServicoDaSessao: minutos / 3, limitado entre 3 e 20).
 *
 * <p>Duplicar a regra aqui é deliberado e tem limite: serve só para PREVER o resultado
 * na tela antes de salvar. Quem decide a meta continua sendo o servidor — o número que
 * a Trilha exibe vem de lá, nunca deste cálculo.
 */
const MINUTOS_POR_DESAFIO = 3;
const META_MINIMA = 3;
const META_MAXIMA = 20;

/** Faixa em que mexer no ritmo muda alguma coisa. Fora dela o resultado é sempre o mesmo. */
const MINUTOS_MINIMOS = META_MINIMA * MINUTOS_POR_DESAFIO;
const MINUTOS_MAXIMOS = META_MAXIMA * MINUTOS_POR_DESAFIO;

function desafiosPorDia(minutos: number): number {
  const bruto = Math.floor(minutos / MINUTOS_POR_DESAFIO);
  return Math.min(Math.max(bruto, META_MINIMA), META_MAXIMA);
}

interface Props {
  /** Reabre o nivelamento: contrapartida de "prefiro comecar do inicio" ser definitivo. */
  onRefazerNivelamento: () => void;
  /** O objetivo influencia o tema escolhido pelo orquestrador, entao a trilha recarrega. */
  onPreferenciasSalvas: () => void;
}

const OBJETIVOS: { valor: ObjetivoDoUsuario; rotulo: string }[] = [
  { valor: "CONVERSACAO_GERAL", rotulo: "Conversação geral" },
  { valor: "VIAGEM", rotulo: "Viagem" },
  { valor: "TRABALHO", rotulo: "Trabalho" },
];

const TIPOS_DE_CORRECAO: { valor: TipoDeCorrecao; rotulo: string }[] = [
  { valor: "DETALHADA", rotulo: "Detalhada" },
  { valor: "RESUMIDA", rotulo: "Resumida" },
];

/** Objetivo, ritmo e tipo de correcao — os passos de onboarding que ficam editaveis. */
export function TelaDeConfiguracoes({
  onPreferenciasSalvas,
  onRefazerNivelamento,
}: Props) {
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [temas, setTemas] = useState<Tema[]>([]);
  const [salvando, setSalvando] = useState(false);
  const [mensagem, setMensagem] = useState<string | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    api
      .buscarUsuario()
      .then(setUsuario)
      .catch((falha: unknown) =>
        setErro(
          falha instanceof Error
            ? falha.message
            : "Não foi possível carregar o perfil.",
        ),
      );

    // A lista vem do servidor, e não escrita à mão aqui: já aconteceu de um tema ser
    // removido do banco (o de "inglês para dev"), e uma cópia no frontend teria
    // continuado oferecendo uma opção que não existe mais.
    api.listarTemas().then(setTemas).catch(() => setTemas([]));
  }, []);

  async function salvar() {
    if (!usuario) {
      return;
    }
    setSalvando(true);
    setMensagem(null);
    setErro(null);
    try {
      const atualizado = await api.salvarPreferencias({
        objetivo: usuario.objetivo,
        minutosPorDia: usuario.minutosPorDia,
        tipoDeCorrecao: usuario.tipoDeCorrecao,
        temaPreferidoId: usuario.temaPreferidoId,
      });
      setUsuario(atualizado);
      setMensagem("Preferências salvas.");
      onPreferenciasSalvas();
    } catch (falha) {
      setErro(
        falha instanceof Error ? falha.message : "Não foi possível salvar.",
      );
    } finally {
      setSalvando(false);
    }
  }

  if (erro && !usuario) {
    return <p className="tela-de-configuracoes__estado">{erro}</p>;
  }

  if (!usuario) {
    return (
      <p className="tela-de-configuracoes__estado">Carregando o perfil...</p>
    );
  }

  return (
    <div className="tela-de-configuracoes">
      <label className="tela-de-configuracoes__campo">
        <span className="tela-de-configuracoes__rotulo">Objetivo</span>
        <span className="tela-de-configuracoes__ajuda">
          Por que você está estudando. Sem um tema escolhido abaixo, é ele quem
          decide a cena dos desafios.
        </span>
        <select
          value={usuario.objetivo}
          onChange={(evento) =>
            setUsuario({
              ...usuario,
              objetivo: evento.target.value as ObjetivoDoUsuario,
            })
          }
        >
          {OBJETIVOS.map((opcao) => (
            <option key={opcao.valor} value={opcao.valor}>
              {opcao.rotulo}
            </option>
          ))}
        </select>
      </label>

      <label className="tela-de-configuracoes__campo">
        <span className="tela-de-configuracoes__rotulo">Tema das cenas</span>
        <span className="tela-de-configuracoes__ajuda">
          A roupagem dos desafios. O conceito praticado continua sendo decidido
          pela trilha — o tema muda só o cenário. O agente varia a cena quando o
          desafio anterior já usou a mesma.
        </span>
        <select
          value={usuario.temaPreferidoId ?? ""}
          onChange={(evento) =>
            setUsuario({
              ...usuario,
              // Vazio volta a ser "sem preferência", e não um tema qualquer:
              // desfazer a escolha precisa ser tão fácil quanto fazê-la.
              temaPreferidoId: evento.target.value
                ? Number(evento.target.value)
                : null,
            })
          }
        >
          <option value="">Deixar o objetivo decidir</option>
          {temas.map((tema) => (
            <option key={tema.id} value={tema.id}>
              {tema.nome}
            </option>
          ))}
        </select>
      </label>

      <label className="tela-de-configuracoes__campo">
        <span className="tela-de-configuracoes__rotulo">Ritmo</span>
        <span className="tela-de-configuracoes__ajuda">
          Minutos de prática por dia. A faixa vai de {MINUTOS_MINIMOS} a{" "}
          {MINUTOS_MAXIMOS} porque é nela que mexer muda a meta.
        </span>
        <input
          type="number"
          min={MINUTOS_MINIMOS}
          max={MINUTOS_MAXIMOS}
          step={3}
          value={usuario.minutosPorDia}
          onChange={(evento) =>
            setUsuario({
              ...usuario,
              minutosPorDia: Number(evento.target.value),
            })
          }
        />
        {/*
          Fecha a conta que o app fazia escondido: você escolhe minutos e a Trilha
          cobra desafios. Sem isto, "15 minutos" e "5 de 5 hoje" pareciam dois
          números sem relação.
        */}
        <span className="tela-de-configuracoes__resultado">
          Meta de <b>{desafiosPorDia(usuario.minutosPorDia)}</b>{" "}
          {desafiosPorDia(usuario.minutosPorDia) === 1 ? "desafio" : "desafios"}{" "}
          por dia
        </span>
      </label>

      <label className="tela-de-configuracoes__campo">
        <span className="tela-de-configuracoes__rotulo">Tipo de correção</span>
        <span className="tela-de-configuracoes__ajuda">
          Quanto detalhe aparece no resumo ao final da sessão.
        </span>
        <select
          value={usuario.tipoDeCorrecao}
          onChange={(evento) =>
            setUsuario({
              ...usuario,
              tipoDeCorrecao: evento.target.value as TipoDeCorrecao,
            })
          }
        >
          {TIPOS_DE_CORRECAO.map((opcao) => (
            <option key={opcao.valor} value={opcao.valor}>
              {opcao.rotulo}
            </option>
          ))}
        </select>
      </label>

      <div className="tela-de-configuracoes__acoes">
        <button
          type="button"
          className="botao-primario"
          onClick={() => void salvar()}
          disabled={salvando}
        >
          {salvando ? "Salvando..." : "Salvar preferências"}
        </button>
        {mensagem && (
          <span className="tela-de-configuracoes__mensagem">{mensagem}</span>
        )}
        {erro && <span className="tela-de-configuracoes__erro">{erro}</span>}
      </div>

      {/*
       * O nivelamento decide por onde a trilha comeca, entao errar nele prende a pessoa
       * no lugar errado. Refazer precisa estar a um clique — inclusive para quem pulou.
       */}
      <section className="tela-de-configuracoes__nivelamento">
        <h2>Ponto de partida</h2>
        {/*
          O nível saía do nivelamento, era gravado e não aparecia em lugar nenhum:
          quem fazia o teste nunca via o resultado. Mostrar aqui é o que dá base
          para decidir se vale refazer — sem ele, o botão abaixo é um palpite.
        */}
        <p className="tela-de-configuracoes__nivel">
          {usuario.nivelEstimado
            ? `O nivelamento estimou o seu nível em ${usuario.nivelEstimado}.`
            : "Você ainda não fez o nivelamento."}
        </p>
        <p>
          Se a trilha começou no lugar errado — cedo demais ou adiantado demais
          — refaça o nivelamento. As notas que você já conquistou praticando não
          são apagadas.
        </p>
        <button
          type="button"
          className="botao-secundario"
          onClick={onRefazerNivelamento}
        >
          Refazer o nivelamento
        </button>
      </section>
    </div>
  );
}
