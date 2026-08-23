import { useEffect, useRef, useState } from "react";
import { ouvir, reconhecimentoDisponivel } from "../audio/vozDoNavegador";
import type { FormatoDoDesafio } from "../tipos";
import "./CampoDeResposta.css";

interface Props {
  formato: FormatoDoDesafio;
  valor: string;
  onChange: (valor: string) => void;
  desabilitado: boolean;
}

/**
 * Entrada da resposta. O componente decide pelo formato do desafio, então a tela de
 * desafio não muda quando a forma de responder muda.
 *
 * Falar é opcional em qualquer formato: o que o reconhecimento entende vira texto no
 * campo, e o aluno pode corrigir antes de enviar. Mandar o texto direto puniria a pessoa
 * por falha do reconhecimento — e é o aluno que leva a nota, não o microfone.
 */
export function CampoDeResposta({
  formato,
  valor,
  onChange,
  desabilitado,
}: Props) {
  const [podeFalar] = useState(reconhecimentoDisponivel);
  const [ouvindo, setOuvindo] = useState(false);
  const [avisoDoMicrofone, setAvisoDoMicrofone] = useState<string | null>(null);
  const pararDeOuvir = useRef<(() => void) | null>(null);

  // Sair da tela ouvindo deixaria o microfone aberto sem nada na tela indicando isso.
  useEffect(() => () => pararDeOuvir.current?.(), []);

  function alternarMicrofone() {
    if (ouvindo) {
      pararDeOuvir.current?.();
      return;
    }

    setAvisoDoMicrofone(null);
    setOuvindo(true);
    pararDeOuvir.current = ouvir(
      (texto) => onChange(texto),
      (erro) => {
        setOuvindo(false);
        pararDeOuvir.current = null;
        if (erro) {
          setAvisoDoMicrofone(erro);
        }
      },
    );
  }

  if (formato === "AUDIO") {
    return (
      <div className="campo-de-resposta__indisponivel">
        Desafios em que o enunciado é falado ainda não são gerados. Este desafio
        não pode ser respondido por aqui.
      </div>
    );
  }

  const rotulo =
    formato === "CONVERSA"
      ? "Sua fala nesta conversa (em inglês)"
      : "Sua resposta (em inglês)";

  return (
    <div className="campo-de-resposta">
      <div className="campo-de-resposta__cabecalho">
        <label className="campo-de-resposta__rotulo" htmlFor="resposta">
          {rotulo}
        </label>

        {podeFalar && (
          <button
            type="button"
            className={`campo-de-resposta__microfone ${ouvindo ? "campo-de-resposta__microfone--ouvindo" : ""}`}
            onClick={alternarMicrofone}
            disabled={desabilitado}
            aria-pressed={ouvindo}
          >
            <span aria-hidden="true">●</span>
            {ouvindo ? "Ouvindo… toque para parar" : "Falar a resposta"}
          </button>
        )}
      </div>

      <textarea
        id="resposta"
        className="campo-de-resposta__area"
        value={valor}
        onChange={(evento) => onChange(evento.target.value)}
        disabled={desabilitado}
        rows={formato === "CONVERSA" ? 5 : 3}
        placeholder="Escreva aqui..."
        autoFocus
      />

      {avisoDoMicrofone && (
        <p className="campo-de-resposta__aviso">{avisoDoMicrofone}</p>
      )}

      {ouvindo && (
        <p className="campo-de-resposta__aviso" aria-live="polite">
          O que aparecer no campo é o que o navegador entendeu. Confira antes de
          enviar — a nota é da sua resposta, não do reconhecimento.
        </p>
      )}
    </div>
  );
}
