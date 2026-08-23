import { motion } from "motion/react";
import { useEffect, useRef, useState } from "react";
import { api } from "../servicos/api";
import type { EtapaDoNivelamento } from "../tipos";
import "./TelaDeNivelamento.css";

interface Props {
  /** Chamado quando a conversa termina ou o aluno decide começar do início. */
  onConcluido: () => void;
}

/**
 * O nivelamento de entrada.
 *
 * Não é prova de múltipla escolha: são perguntas abertas em ordem crescente, e o que
 * conta é o que a pessoa consegue produzir — que é exatamente o que o app vai cobrar
 * depois. Pular é um botão de primeira classe, e não uma saída escondida: parar numa
 * pergunta é o sinal mais limpo de onde está o teto.
 */
export function TelaDeNivelamento({ onConcluido }: Props) {
  const [etapa, setEtapa] = useState<EtapaDoNivelamento | null>(null);
  const [resposta, setResposta] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const campo = useRef<HTMLTextAreaElement>(null);

  // O modo estrito do React chama o efeito duas vezes em desenvolvimento. A trava fica
  // aqui tambem, e nao so no backend, para nao gastar uma requisicao a toa.
  const jaPediu = useRef(false);

  useEffect(() => {
    if (jaPediu.current) {
      return;
    }
    jaPediu.current = true;

    api
      .iniciarNivelamento()
      .then(setEtapa)
      .catch((falha) =>
        setErro(
          falha instanceof Error ? falha.message : "Não foi possível começar.",
        ),
      );
  }, []);

  // O foco volta ao campo a cada pergunta: sem isso a pessoa precisa clicar de novo
  // toda vez, e a conversa perde o ritmo de conversa.
  useEffect(() => {
    if (etapa?.perguntaAtual) {
      campo.current?.focus();
    }
  }, [etapa?.ordem, etapa?.perguntaAtual]);

  async function responder(texto: string) {
    if (!etapa || enviando) {
      return;
    }

    setEnviando(true);
    setErro(null);
    try {
      const proxima = await api.responderNivelamento(
        etapa.id,
        etapa.ordem,
        texto,
      );
      setEtapa(proxima);
      setResposta("");
    } catch (falha) {
      setErro(
        falha instanceof Error ? falha.message : "Não foi possível enviar.",
      );
    } finally {
      setEnviando(false);
    }
  }

  async function comecarDoInicio() {
    if (etapa) {
      await api.abandonarNivelamento(etapa.id).catch(() => undefined);
    }
    onConcluido();
  }

  if (erro && !etapa) {
    return (
      <div className="nivelamento">
        <p className="nivelamento__erro">{erro}</p>
        <button
          type="button"
          className="nivelamento__secundario"
          onClick={onConcluido}
        >
          Ir para a trilha
        </button>
      </div>
    );
  }

  if (!etapa) {
    return (
      <div className="nivelamento nivelamento--carregando">Preparando…</div>
    );
  }

  const resultado = etapa.resultado;
  const pergunta = etapa.perguntaAtual;

  return (
    <div className="nivelamento">
      {/*
       * Sem AnimatePresence de propósito. Com `mode="wait"` a pergunta seguinte só
       * aparece depois que a anterior termina de sair — e a saída depende do
       * requestAnimationFrame. Numa aba em segundo plano o quadro não roda, a saída
       * nunca termina e o aluno fica preso no "Enviando…" com a resposta já entregue.
       * Animar só a entrada não tem esse ponto de parada.
       */}
      {pergunta ? (
        <motion.div
          key={`pergunta-${etapa.ordem}`}
          className="nivelamento__cartao"
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ type: "spring", visualDuration: 0.3, bounce: 0 }}
        >
          <header className="nivelamento__topo">
            <p className="nivelamento__passo">
              Pergunta {etapa.ordem} de {etapa.total}
            </p>
            <div
              className="nivelamento__barra"
              role="progressbar"
              aria-valuenow={etapa.ordem}
              aria-valuemin={1}
              aria-valuemax={etapa.total}
            >
              <span
                style={{ width: `${(etapa.ordem / etapa.total) * 100}%` }}
              />
            </div>
          </header>

          <h1 className="nivelamento__pergunta" lang="en">
            {pergunta.pergunta}
          </h1>
          <p className="nivelamento__apoio">{pergunta.apoio}</p>

          <textarea
            ref={campo}
            className="nivelamento__campo"
            lang="en"
            rows={4}
            value={resposta}
            onChange={(evento) => setResposta(evento.target.value)}
            placeholder="Escreva em inglês…"
            disabled={enviando}
          />

          {erro && <p className="nivelamento__erro">{erro}</p>}

          <div className="nivelamento__acoes">
            <button
              type="button"
              className="nivelamento__primario"
              onClick={() => responder(resposta)}
              disabled={enviando || resposta.trim().length === 0}
            >
              {enviando ? "Enviando…" : "Responder"}
            </button>
            {/* Pular é explícito de propósito: parar numa pergunta é o dado, não uma falha. */}
            <button
              type="button"
              className="nivelamento__secundario"
              onClick={() => responder("")}
              disabled={enviando}
            >
              Não sei esta
            </button>
          </div>

          <button
            type="button"
            className="nivelamento__sair"
            onClick={comecarDoInicio}
          >
            Prefiro começar do início
          </button>
        </motion.div>
      ) : (
        resultado && (
          <motion.div
            key="resultado"
            className="nivelamento__cartao"
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ type: "spring", visualDuration: 0.4, bounce: 0 }}
          >
            <p className="nivelamento__passo">Seu ponto de partida</p>
            <h1 className="nivelamento__nivel">{resultado.nivel}</h1>
            <p className="nivelamento__resumo">{resultado.resumo}</p>

            <dl className="nivelamento__leitura">
              <div>
                <dt>Já sustenta</dt>
                <dd>{resultado.pontoForte}</dd>
              </div>
              <div>
                <dt>Vale atacar primeiro</dt>
                <dd>{resultado.pontoAFortalecer}</dd>
              </div>
            </dl>

            {resultado.modulosLiberados > 0 && (
              <p className="nivelamento__nota">
                {resultado.modulosLiberados} conceitos anteriores entraram como{" "}
                <strong>presumidos</strong> — aparecem em amarelo até você
                demonstrá-los.
                {resultado.primeiroModulo && (
                  <> A trilha começa em {resultado.primeiroModulo}.</>
                )}
              </p>
            )}

            <button
              type="button"
              className="nivelamento__primario"
              onClick={onConcluido}
            >
              Ver a minha trilha
            </button>
          </motion.div>
        )
      )}
    </div>
  );
}
