import { motion } from "motion/react";
import { BotaoDeOuvir } from "./BotaoDeOuvir";
import type { Correcao } from "../tipos";
import { formatarNota } from "./IndicadorDeNota";
import "./PainelDeCorrecao.css";

interface Props {
  correcao: Correcao;
  onProximoDesafio: () => void;
  carregandoProximo: boolean;
  /** Abre o material do conceito que vem sendo errado. */
  onVerConteudo?: (codigoDoModulo: string) => void;
}

/**
 * Correção resumida, exibida só ao final da sessão — nunca interrompendo a resposta.
 * Mostra a nota da resposta, os erros específicos e como a nota do módulo ficou.
 */
export function PainelDeCorrecao({
  correcao,
  onProximoDesafio,
  carregandoProximo,
  onVerConteudo,
}: Props) {
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
              <span className="painel-de-correcao__tipo">
                {erro.tipo.replaceAll("_", " ")}
              </span>

              {erro.trechoErrado && erro.correcao && (
                <p className="painel-de-correcao__troca">
                  <span className="painel-de-correcao__errado">
                    {erro.trechoErrado}
                  </span>
                  <span aria-hidden="true"> → </span>
                  <span className="painel-de-correcao__certo" lang="en">
                    {erro.correcao}
                  </span>{" "}
                  <BotaoDeOuvir
                    texto={erro.correcao}
                    descricao={`Ouvir a forma correta: ${erro.correcao}`}
                  />
                </p>
              )}

              <p className="painel-de-correcao__explicacao">
                {erro.explicacao}
              </p>
            </li>
          ))}
        </ul>
      )}

      {/*
       * Erro que insiste ganha espaço próprio, abaixo da correção da vez. Corrigir de
       * novo, do mesmo jeito, já se mostrou insuficiente nas duas vezes anteriores —
       * o que muda alguma coisa aqui é mostrar que existe um padrão e onde ele é ensinado.
       */}
      {correcao.reforco && (
        <section className="painel-de-correcao__reforco">
          <h3>
            {correcao.reforco.rotulo}: {correcao.reforco.vezes}ª vez
          </h3>
          <p className="painel-de-correcao__reforco-texto">
            Não é distração — é um conceito que ainda não entrou.
            {correcao.reforco.moduloDoConceitoNome && (
              <>
                {" "}
                Ele é ensinado em <b>{correcao.reforco.moduloDoConceitoNome}</b>
                .
              </>
            )}
          </p>

          {correcao.reforco.anteriores.length > 0 && (
            <>
              <span className="painel-de-correcao__reforco-rotulo">
                As vezes anteriores
              </span>
              <ul className="painel-de-correcao__reforco-lista">
                {correcao.reforco.anteriores.map((anterior, indice) => (
                  <li key={indice}>
                    <span className="painel-de-correcao__errado">
                      {anterior.trechoErrado}
                    </span>
                    <span aria-hidden="true"> → </span>
                    <span className="painel-de-correcao__certo">
                      {anterior.correcao}
                    </span>
                  </li>
                ))}
              </ul>
            </>
          )}

          {correcao.reforco.moduloDoConceito && onVerConteudo && (
            <button
              type="button"
              className="botao-secundario"
              onClick={() => onVerConteudo(correcao.reforco!.moduloDoConceito!)}
            >
              Rever o conteúdo
            </button>
          )}
        </section>
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
