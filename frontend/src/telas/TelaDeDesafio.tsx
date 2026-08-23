import { useCallback, useEffect, useState } from "react";
import { CampoDeResposta } from "../componentes/CampoDeResposta";
import { PainelDeCorrecao } from "../componentes/PainelDeCorrecao";
import { ResumoDoConteudo } from "../componentes/ResumoDoConteudo";
import { api } from "../servicos/api";
import type { Correcao, Desafio, ResumoDoDia } from "../tipos";
import "./TelaDeDesafio.css";

interface Props {
  /** Módulo que o aluno acabou de estudar; null deixa a escolha com o orquestrador. */
  moduloParaPraticar: string | null;
  /** Avisa o resto do app que a nota mudou, para a lista de módulos recarregar. */
  onNotaAtualizada: () => void;
  onVerConteudo: (codigoDoModulo: string) => void;
  onVoltarParaTrilha: () => void;
}

/**
 * Tela do desafio da vez. A correção só aparece depois do envio, resumida,
 * nunca no meio da escrita da resposta.
 */
export function TelaDeDesafio({
  moduloParaPraticar,
  onNotaAtualizada,
  onVerConteudo,
  onVoltarParaTrilha,
}: Props) {
  const [desafio, setDesafio] = useState<Desafio | null>(null);
  const [resposta, setResposta] = useState("");
  const [correcao, setCorrecao] = useState<Correcao | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [dia, setDia] = useState<ResumoDoDia | null>(null);

  /**
   * O contador do dia vem do servidor a cada resposta, em vez de ser somado aqui.
   * Somar no cliente daria números diferentes em duas abas, e o "dia fechado" é
   * exatamente o tipo de coisa que não pode depender de qual aba estava aberta.
   */
  const atualizarDia = useCallback(() => {
    api
      .buscarResumoDoDia()
      .then(setDia)
      // O contador é acessório: uma falha aqui não pode atrapalhar quem está praticando.
      .catch(() => undefined);
  }, []);

  const carregarDesafio = useCallback(async (codigoDoModulo?: string) => {
    setCarregando(true);
    setErro(null);
    setCorrecao(null);
    setResposta("");
    try {
      setDesafio(await api.proximoDesafio(codigoDoModulo));
    } catch (falha) {
      setErro(
        falha instanceof Error
          ? falha.message
          : "Não foi possível carregar o desafio.",
      );
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    // Só a entrada na tela respeita o módulo estudado. Depois de responder, a escolha
    // do próximo volta a ser do orquestrador, que é quem enxerga a trilha inteira.
    void carregarDesafio(moduloParaPraticar ?? undefined);
    atualizarDia();
  }, [carregarDesafio, moduloParaPraticar, atualizarDia]);

  async function enviarResposta() {
    if (!desafio || resposta.trim().length === 0) {
      return;
    }
    setEnviando(true);
    setErro(null);
    try {
      const resultado = await api.responderDesafio(desafio.id, resposta);
      setCorrecao(resultado);
      onNotaAtualizada();
      atualizarDia();
    } catch (falha) {
      setErro(
        falha instanceof Error
          ? falha.message
          : "Não foi possível enviar a resposta.",
      );
    } finally {
      setEnviando(false);
    }
  }

  if (carregando) {
    return (
      <p className="tela-de-desafio__estado">Escolhendo o próximo desafio...</p>
    );
  }

  if (erro && !desafio) {
    return (
      <div className="tela-de-desafio__estado">
        <p>{erro}</p>
        <button
          type="button"
          className="botao-secundario"
          onClick={() => void carregarDesafio()}
        >
          Tentar de novo
        </button>
      </div>
    );
  }

  if (!desafio) {
    return (
      <p className="tela-de-desafio__estado">Nenhum desafio disponível.</p>
    );
  }

  return (
    <div className="tela-de-desafio">
      <div className="tela-de-desafio__principal">
        <div className="tela-de-desafio__contexto">
          <span className="tela-de-desafio__etiqueta">
            {desafio.moduloNome}
          </span>
          <span className="tela-de-desafio__separador" aria-hidden="true">
            ·
          </span>
          <span className="tela-de-desafio__tema">{desafio.temaNome}</span>
          {dia && (
            <span className="tela-de-desafio__contador">
              {Math.min(dia.concluidos + (correcao ? 0 : 1), dia.meta)} de{" "}
              {dia.meta} hoje
            </span>
          )}
        </div>

        <article className="tela-de-desafio__cartao">
          {desafio.contextoDaCena && (
            <p className="tela-de-desafio__cena">{desafio.contextoDaCena}</p>
          )}
          <h2 className="tela-de-desafio__enunciado">{desafio.enunciado}</h2>
        </article>

        {!correcao && (
          <div className="tela-de-desafio__resposta">
            <CampoDeResposta
              formato={desafio.formato}
              valor={resposta}
              onChange={setResposta}
              desabilitado={enviando}
            />

            {erro && <p className="tela-de-desafio__erro">{erro}</p>}

            <button
              type="button"
              className="botao-primario"
              onClick={() => void enviarResposta()}
              disabled={enviando || resposta.trim().length === 0}
            >
              {enviando ? "Avaliando..." : "Enviar resposta"}
            </button>
          </div>
        )}

        {correcao && (
          <PainelDeCorrecao
            correcao={correcao}
            onProximoDesafio={() => void carregarDesafio()}
            carregandoProximo={carregando}
            onVerConteudo={onVerConteudo}
          />
        )}

        {/*
         * A sessão tem fim. Sem isso é um desafio atrás do outro sem chegada, e a
         * pessoa para por cansaço em vez de parar por ter cumprido — o que é a
         * diferença entre voltar amanhã e não voltar.
         *
         * O fim é permissão para parar, não parede: continuar é um clique.
         */}
        {correcao && dia?.metaAlcancada && (
          <section className="tela-de-desafio__fechamento">
            <h2>Dia cumprido</h2>
            <p>
              {dia.concluidos} {dia.concluidos === 1 ? "desafio" : "desafios"}{" "}
              hoje
              {dia.sequencia.atual > 1 && (
                <>
                  , {dia.sequencia.atual} dias seguidos
                  {dia.sequencia.atual >= dia.sequencia.melhor &&
                    " — a sua melhor sequência"}
                </>
              )}
              .
            </p>
            <div className="tela-de-desafio__fechamento-acoes">
              <button
                type="button"
                className="botao-primario"
                onClick={onVoltarParaTrilha}
              >
                Voltar para a trilha
              </button>
              <button
                type="button"
                className="botao-secundario"
                onClick={() => void carregarDesafio()}
              >
                Fazer mais um
              </button>
            </div>
          </section>
        )}

        <details className="tela-de-desafio__motivo">
          <summary>Por que este desafio agora?</summary>
          <p>{desafio.motivoDaEscolha}</p>
        </details>
      </div>

      {/*
       * O conteúdo do módulo fica ao lado do exercício: travar no meio de uma
       * resposta e ter que sair da tela para reler a regra é onde o aluno desiste.
       */}
      <ResumoDoConteudo
        codigoDoModulo={desafio.moduloCodigo}
        onVerConteudoCompleto={() => onVerConteudo(desafio.moduloCodigo)}
      />
    </div>
  );
}
