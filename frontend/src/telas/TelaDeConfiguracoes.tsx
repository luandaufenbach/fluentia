import { useEffect, useState } from "react";
import { api } from "../servicos/api";
import type { ObjetivoDoUsuario, TipoDeCorrecao, Usuario } from "../tipos";
import "./TelaDeConfiguracoes.css";

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
  { valor: "DEV", rotulo: "Inglês para dev" },
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
          Define o tema das cenas que o agente usa nos desafios.
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
        <span className="tela-de-configuracoes__rotulo">Ritmo</span>
        <span className="tela-de-configuracoes__ajuda">
          Minutos de prática por dia.
        </span>
        <input
          type="number"
          min={5}
          max={240}
          value={usuario.minutosPorDia}
          onChange={(evento) =>
            setUsuario({
              ...usuario,
              minutosPorDia: Number(evento.target.value),
            })
          }
        />
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
        <p>
          Se a trilha comecou no lugar errado — cedo demais ou adiantado demais
          — refaca o nivelamento. As notas que voce ja conquistou praticando nao
          sao apagadas.
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
