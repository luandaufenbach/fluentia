import type { Correcao } from "../tipos";
import { formatarNota } from "./IndicadorDeNota";
import "./PainelDeCorrecao.css";

interface Props {
  correcao: Correcao;
  onProximoDesafio: () => void;
  carregandoProximo: boolean;
}

/**
 * Correcao resumida, exibida so ao final da sessao — nunca interrompendo a resposta.
 * Mostra a nota da resposta, os erros especificos e como a nota do modulo ficou.
 */
export function PainelDeCorrecao({ correcao, onProximoDesafio, carregandoProximo }: Props) {
  const acertou = correcao.erros.length === 0;

  return (
    <section
      className={`painel-de-correcao painel-de-correcao--${correcao.faixaDoModulo.toLowerCase()}`}
      aria-live="polite"
    >
      <header className="painel-de-correcao__cabecalho">
        <h2>{acertou ? "Correto" : "Vamos revisar"}</h2>
        <span className="painel-de-correcao__nota">
          {formatarNota(correcao.notaDaResposta)}
          <span className="painel-de-correcao__nota-total"> / 10</span>
        </span>
      </header>

      <p className="painel-de-correcao__feedback">{correcao.feedback}</p>

      {correcao.erros.length > 0 && (
        <ul className="painel-de-correcao__erros">
          {correcao.erros.map((erro, indice) => (
            <li key={indice} className="painel-de-correcao__erro">
              <span className="painel-de-correcao__tipo">{erro.tipo.replaceAll("_", " ")}</span>

              {erro.trechoErrado && erro.correcao && (
                <p className="painel-de-correcao__troca">
                  <span className="painel-de-correcao__errado">{erro.trechoErrado}</span>
                  <span aria-hidden="true"> → </span>
                  <span className="painel-de-correcao__certo">{erro.correcao}</span>
                </p>
              )}

              <p className="painel-de-correcao__explicacao">{erro.explicacao}</p>
            </li>
          ))}
        </ul>
      )}

      <footer className="painel-de-correcao__rodape">
        <p className="painel-de-correcao__modulo">
          {correcao.moduloNome} agora esta em{" "}
          <strong>{formatarNota(correcao.notaDoModulo)}</strong>
        </p>

        <button
          type="button"
          className="botao-primario"
          onClick={onProximoDesafio}
          disabled={carregandoProximo}
        >
          {carregandoProximo ? "Gerando..." : "Proximo desafio"}
        </button>
      </footer>
    </section>
  );
}
