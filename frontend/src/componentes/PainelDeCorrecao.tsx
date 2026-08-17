import { motion } from "motion/react";
import type { Correcao } from "../tipos";
import { formatarNota } from "./IndicadorDeNota";
import "./PainelDeCorrecao.css";

interface Props {
  correcao: Correcao;
  onProximoDesafio: () => void;
  carregandoProximo: boolean;
}

/**
 * Correção resumida, exibida só ao final da sessão — nunca interrompendo a resposta.
 * Mostra a nota da resposta, os erros específicos e como a nota do módulo ficou.
 */
export function PainelDeCorrecao({ correcao, onProximoDesafio, carregandoProximo }: Props) {
  const acertou = correcao.erros.length === 0;

  return (
    /*
     * Mola em vez de duração fixa, e interrompível: se a correção chegar enquanto
     * a anterior ainda entra, a nova assume do ponto e da velocidade atuais em vez
     * de reiniciar do zero. bounce 0 porque não há gesto com momento aqui.
     */
    <motion.section
      className={`painel-de-correcao painel-de-correcao--${correcao.faixaDoModulo.toLowerCase()}`}
      aria-live="polite"
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ type: "spring", visualDuration: 0.32, bounce: 0 }}
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
          {correcao.moduloNome} agora está em{" "}
          <strong>{formatarNota(correcao.notaDoModulo)}</strong>
        </p>

        <button
          type="button"
          className="botao-primario"
          onClick={onProximoDesafio}
          disabled={carregandoProximo}
        >
          {carregandoProximo ? "Gerando..." : "Próximo desafio"}
        </button>
      </footer>
    </motion.section>
  );
}
