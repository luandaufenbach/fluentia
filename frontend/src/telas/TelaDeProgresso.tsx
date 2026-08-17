import { useEffect, useState } from "react";
import { IndicadorDeNota } from "../componentes/IndicadorDeNota";
import { api } from "../servicos/api";
import type { FaixaDeNota, Progresso } from "../tipos";
import "./TelaDeProgresso.css";

interface Props {
  versao: number;
}

const ROTULO_DA_FAIXA: Record<FaixaDeNota, string> = {
  VERDE: "Consolidados",
  AMARELO: "Em andamento",
  VERMELHO: "Precisam de reforco",
  NOVO: "Ainda nao praticados",
};

/** Quantos modulos estao em cada faixa e quais conceitos precisam de atencao agora. */
export function TelaDeProgresso({ versao }: Props) {
  const [progresso, setProgresso] = useState<Progresso | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    let cancelado = false;

    api
      .buscarProgresso()
      .then((dados) => {
        if (!cancelado) {
          setProgresso(dados);
        }
      })
      .catch((falha: unknown) => {
        if (!cancelado) {
          setErro(falha instanceof Error ? falha.message : "Nao foi possivel carregar o progresso.");
        }
      });

    return () => {
      cancelado = true;
    };
  }, [versao]);

  if (erro) {
    return <p className="tela-de-progresso__estado">{erro}</p>;
  }

  if (!progresso) {
    return <p className="tela-de-progresso__estado">Carregando o progresso...</p>;
  }

  const faixasNaOrdem: FaixaDeNota[] = ["VERDE", "AMARELO", "VERMELHO", "NOVO"];

  return (
    <div className="tela-de-progresso">
      <section>
        <h2 className="tela-de-progresso__titulo">Visao geral</h2>
        <p className="tela-de-progresso__resumo">
          {progresso.modulosLiberados} de {progresso.totalDeModulos} modulos liberados para pratica.
        </p>

        <ul className="tela-de-progresso__faixas">
          {faixasNaOrdem.map((faixa) => (
            <li
              key={faixa}
              className={`tela-de-progresso__faixa tela-de-progresso__faixa--${faixa.toLowerCase()}`}
            >
              <span className="tela-de-progresso__quantidade">
                {progresso.quantidadePorFaixa[faixa] ?? 0}
              </span>
              <span className="tela-de-progresso__rotulo">{ROTULO_DA_FAIXA[faixa]}</span>
            </li>
          ))}
        </ul>
      </section>

      <section>
        <h2 className="tela-de-progresso__titulo">Precisam de atencao agora</h2>

        {progresso.precisamDeAtencao.length === 0 ? (
          <p className="tela-de-progresso__estado">
            Nenhum conceito praticado esta abaixo do verde. Continue praticando para nao perder
            terreno: a nota cai sozinha com o tempo sem pratica.
          </p>
        ) : (
          <ul className="tela-de-progresso__atencao">
            {progresso.precisamDeAtencao.map((modulo) => (
              <li key={modulo.id} className="tela-de-progresso__linha">
                <div>
                  <span className="tela-de-progresso__modulo">{modulo.nome}</span>
                  <span className="tela-de-progresso__nivel">{modulo.nivel}</span>
                </div>
                <IndicadorDeNota nota={modulo.nota} faixa={modulo.faixa} />
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
