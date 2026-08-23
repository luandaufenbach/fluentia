import { useEffect, useState } from "react";
import { api } from "../servicos/api";
import type { ConteudoDoModulo } from "../tipos";
import "./ResumoDoConteudo.css";

interface Props {
  codigoDoModulo: string;
  onVerConteudoCompleto: () => void;
}

/** Quantos exemplos cabem ao lado do exercício sem virar a leitura principal. */
const EXEMPLOS_NO_RESUMO = 3;

/**
 * Lembrete do conteúdo ao lado do desafio. Não substitui a tela de conteúdo:
 * mostra o essencial para destravar e oferece o caminho de volta ao material inteiro.
 */
export function ResumoDoConteudo({
  codigoDoModulo,
  onVerConteudoCompleto,
}: Props) {
  const [conteudo, setConteudo] = useState<ConteudoDoModulo | null>(null);

  useEffect(() => {
    let cancelado = false;

    // Módulo sem conteúdo ainda não é erro de tela: o painel simplesmente não aparece.
    api
      .buscarConteudo(codigoDoModulo)
      .then((resultado) => {
        if (!cancelado) setConteudo(resultado);
      })
      .catch(() => {
        if (!cancelado) setConteudo(null);
      });

    return () => {
      cancelado = true;
    };
  }, [codigoDoModulo]);

  if (!conteudo) {
    return null;
  }

  return (
    <aside className="resumo-do-conteudo">
      <h2 className="resumo-do-conteudo__titulo">Relembrando</h2>

      <p className="resumo-do-conteudo__resumo">{conteudo.resumo}</p>

      <ul className="resumo-do-conteudo__exemplos">
        {conteudo.exemplos
          .slice(0, EXEMPLOS_NO_RESUMO)
          .map((exemplo, indice) => (
            <li key={indice}>
              <span className="resumo-do-conteudo__ingles" lang="en">
                {exemplo.emIngles}
              </span>
              <span className="resumo-do-conteudo__portugues">
                {exemplo.emPortugues}
              </span>
            </li>
          ))}
      </ul>

      <button
        type="button"
        className="botao-secundario"
        onClick={onVerConteudoCompleto}
      >
        Voltar para o conteúdo
      </button>
    </aside>
  );
}
