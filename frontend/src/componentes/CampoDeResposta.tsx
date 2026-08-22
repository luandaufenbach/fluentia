import type { FormatoDoDesafio } from "../tipos";
import "./CampoDeResposta.css";

interface Props {
  formato: FormatoDoDesafio;
  valor: string;
  onChange: (valor: string) => void;
  desabilitado: boolean;
}

/**
 * Entrada da resposta. O componente decide pelo formato do desafio, entao entrar com
 * audio na fase 2 e adicionar um ramo aqui — a tela de desafio nao muda.
 */
export function CampoDeResposta({ formato, valor, onChange, desabilitado }: Props) {
  if (formato === "AUDIO") {
    return (
      <div className="campo-de-resposta__indisponivel">
        Desafios em áudio entram na fase 2. Este desafio ainda não pode ser respondido por aqui.
      </div>
    );
  }

  const rotulo =
    formato === "CONVERSA"
      ? "Sua fala nesta conversa (em inglês)"
      : "Sua resposta (em inglês)";

  return (
    <label className="campo-de-resposta">
      <span className="campo-de-resposta__rotulo">{rotulo}</span>
      <textarea
        className="campo-de-resposta__area"
        value={valor}
        onChange={(evento) => onChange(evento.target.value)}
        disabled={desabilitado}
        rows={formato === "CONVERSA" ? 5 : 3}
        placeholder="Escreva aqui..."
        autoFocus
      />
    </label>
  );
}
