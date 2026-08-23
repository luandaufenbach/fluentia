import { useEffect, useState } from "react";
import {
  aoCarregarVozes,
  falar,
  pararDeFalar,
  sinteseDisponivel,
} from "../audio/vozDoNavegador";
import "./BotaoDeOuvir.css";

interface Props {
  /** O texto em inglês a ser falado. */
  texto: string;
  /** Descrição para leitor de tela — diz o que será ouvido, não só "ouvir". */
  descricao?: string;
}

/**
 * Ouve um trecho em inglês.
 *
 * Some quando não há voz em inglês instalada. Não é preciosismo: sem voz inglesa o
 * navegador lê o inglês com a voz que tiver — em português, num Windows brasileiro — e
 * um app de idioma ensinando pronúncia errada é pior do que um app sem áudio.
 */
export function BotaoDeOuvir({ texto, descricao }: Props) {
  const [disponivel, setDisponivel] = useState(sinteseDisponivel);

  // As vozes carregam de forma assíncrona: na primeira renderização a lista costuma vir
  // vazia, e sem reagir ao evento o botão nunca apareceria mesmo em quem tem voz inglesa.
  useEffect(() => aoCarregarVozes(() => setDisponivel(sinteseDisponivel())), []);

  // Sair da tela no meio da fala deixaria a voz tocando sobre a tela seguinte.
  useEffect(() => pararDeFalar, []);

  if (!disponivel || !texto.trim()) {
    return null;
  }

  return (
    <button
      type="button"
      className="botao-de-ouvir"
      onClick={() => falar(texto)}
      aria-label={descricao ?? `Ouvir: ${texto}`}
      title="Ouvir em inglês"
    >
      <span aria-hidden="true">♪</span>
      <span className="botao-de-ouvir__rotulo">Ouvir</span>
    </button>
  );
}
