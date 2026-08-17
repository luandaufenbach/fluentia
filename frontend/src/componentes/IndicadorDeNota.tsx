import type { FaixaDeNota } from "../tipos";
import "./IndicadorDeNota.css";

interface Props {
  nota: number | null;
  faixa: FaixaDeNota;
}

/**
 * Ponto colorido pequeno com a nota ao lado.
 * Modulo ainda nao praticado mostra "novo" em cinza, no lugar da nota.
 */
export function IndicadorDeNota({ nota, faixa }: Props) {
  const semNota = nota === null;

  return (
    <span
      className={`indicador-de-nota indicador-de-nota--${faixa.toLowerCase()}`}
      title={semNota ? "Modulo ainda nao praticado" : `Nota ${formatarNota(nota)} de 10`}
    >
      <span className="indicador-de-nota__ponto" aria-hidden="true" />
      <span className="indicador-de-nota__valor">{semNota ? "novo" : formatarNota(nota)}</span>
    </span>
  );
}

/** Nota com uma casa decimal e virgula, como no padrao brasileiro. */
export function formatarNota(nota: number): string {
  return nota.toFixed(1).replace(".", ",");
}
