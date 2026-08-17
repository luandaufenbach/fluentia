import { motion } from "motion/react";
import { useEffect, useState } from "react";
import { api } from "../servicos/api";
import type { ConteudoDoModulo } from "../tipos";
import "./TelaDeConteudo.css";

interface Props {
  codigoDoModulo: string;
  onComecarExercicios: () => void;
  onVoltarParaTrilha: () => void;
}

/**
 * O material de estudo do módulo. Vem antes do exercício de propósito: errar sem ter
 * lido nada não ensina, só mede. É também para onde o aluno volta quando trava no meio
 * de um desafio.
 */
export function TelaDeConteudo({ codigoDoModulo, onComecarExercicios, onVoltarParaTrilha }: Props) {
  const [conteudo, setConteudo] = useState<ConteudoDoModulo | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    let cancelado = false;
    setCarregando(true);
    setErro(null);

    api
      .buscarConteudo(codigoDoModulo)
      .then((resultado) => {
        if (!cancelado) setConteudo(resultado);
      })
      .catch((falha: unknown) => {
        if (!cancelado) {
          setErro(falha instanceof Error ? falha.message : "Não foi possível carregar o conteúdo.");
        }
      })
      .finally(() => {
        if (!cancelado) setCarregando(false);
      });

    return () => {
      cancelado = true;
    };
  }, [codigoDoModulo]);

  if (carregando) {
    return <p className="tela-de-conteudo__estado">Abrindo o conteúdo...</p>;
  }

  if (erro || !conteudo) {
    return (
      <div className="tela-de-conteudo__estado">
        <p>{erro ?? "Conteúdo indisponível."}</p>
        <button type="button" className="botao-secundario" onClick={onVoltarParaTrilha}>
          Voltar para a trilha
        </button>
      </div>
    );
  }

  return (
    <motion.div
      className="tela-de-conteudo"
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ type: "spring", visualDuration: 0.35, bounce: 0 }}
    >
      <div className="tela-de-conteudo__leitura">
        <p className="tela-de-conteudo__resumo">{conteudo.resumo}</p>

        {/* Parágrafos vêm separados por linha em branco, sem markdown. */}
        <div className="tela-de-conteudo__explicacao">
          {conteudo.explicacao
            .split(/\n\s*\n/)
            .map((paragrafo) => paragrafo.trim())
            .filter(Boolean)
            .map((paragrafo, indice) => (
              <p key={indice}>{paragrafo}</p>
            ))}
        </div>

        {conteudo.errosComuns.length > 0 && (
          <section className="tela-de-conteudo__secao">
            <h2>Onde brasileiro costuma errar</h2>
            <ul className="tela-de-conteudo__erros">
              {conteudo.errosComuns.map((erro, indice) => (
                <li key={indice}>
                  <p className="tela-de-conteudo__troca">
                    <span className="tela-de-conteudo__errado">{erro.errado}</span>
                    <span aria-hidden="true"> → </span>
                    <span className="tela-de-conteudo__certo">{erro.certo}</span>
                  </p>
                  <p className="tela-de-conteudo__explicacao-do-erro">{erro.explicacao}</p>
                </li>
              ))}
            </ul>
          </section>
        )}
      </div>

      <aside className="tela-de-conteudo__exemplos">
        <h2>Exemplos</h2>
        <ol className="tela-de-conteudo__lista-de-exemplos">
          {conteudo.exemplos.map((exemplo, indice) => (
            <li key={indice}>
              <p className="tela-de-conteudo__ingles" lang="en">
                {exemplo.emIngles}
              </p>
              <p className="tela-de-conteudo__portugues">{exemplo.emPortugues}</p>
              {exemplo.observacao && (
                <p className="tela-de-conteudo__observacao">{exemplo.observacao}</p>
              )}
            </li>
          ))}
        </ol>

        <div className="tela-de-conteudo__acoes">
          <button type="button" className="botao-primario" onClick={onComecarExercicios}>
            Começar os exercícios
          </button>
          <button type="button" className="botao-secundario" onClick={onVoltarParaTrilha}>
            Voltar para a trilha
          </button>
        </div>
      </aside>
    </motion.div>
  );
}
