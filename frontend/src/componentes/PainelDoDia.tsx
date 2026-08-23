import { motion } from "motion/react";
import type { ResumoDoDia } from "../tipos";
import "./PainelDoDia.css";

interface Props {
  resumo: ResumoDoDia;
  onPraticarModulo: (codigoDoModulo: string) => void;
  onIrParaDesafio: () => void;
}

/** Quantos conceitos em queda cabem antes da lista virar parede de texto. */
const REVISOES_VISIVEIS = 3;

function plural(quantidade: number, singular: string, plural: string) {
  return quantidade === 1 ? singular : plural;
}

/**
 * O dia do aluno, no topo da trilha.
 *
 * Junta as três coisas que dão ritmo: quanto falta para fechar hoje, há quantos dias
 * seguidos ele aparece, e o que o tempo está derrubando sem que ele tenha errado nada.
 *
 * Sem pontos, sem ligas, sem troféu. O que o app mostra é o que ele mediu.
 */
export function PainelDoDia({
  resumo,
  onPraticarModulo,
  onIrParaDesafio,
}: Props) {
  const { meta, concluidos, restantes, metaAlcancada, sequencia, revisoes } =
    resumo;
  const percentual = Math.min(100, Math.round((concluidos / meta) * 100));

  return (
    <section className={`dia ${metaAlcancada ? "dia--fechado" : ""}`}>
      <div className="dia__principal">
        <div className="dia__texto">
          <span className="dia__rotulo">
            {metaAlcancada ? "Dia fechado" : "Hoje"}
          </span>
          <p className="dia__frase">
            {metaAlcancada ? (
              <>
                Você fez <b>{concluidos}</b>{" "}
                {plural(concluidos, "desafio", "desafios")} hoje. O resto é
                bônus.
              </>
            ) : (
              <>
                <b>
                  {concluidos} de {meta}
                </b>{" "}
                {plural(meta, "desafio", "desafios")} — faltam {restantes}.
              </>
            )}
          </p>
        </div>

        {/* A sequência só aparece depois do primeiro dia: um "0 dias" logo na entrada
            é uma cobrança antes de qualquer chance de cumprir. */}
        {sequencia.atual > 0 && (
          <div
            className="dia__sequencia"
            title={`Melhor sequência: ${sequencia.melhor} dias`}
          >
            <span className="dia__sequencia-numero">{sequencia.atual}</span>
            <span className="dia__sequencia-rotulo">
              {plural(sequencia.atual, "dia seguido", "dias seguidos")}
            </span>
            {!sequencia.praticouHoje && (
              <span className="dia__sequencia-aviso">mantenha hoje</span>
            )}
          </div>
        )}
      </div>

      <div
        className="dia__barra"
        role="progressbar"
        aria-valuenow={concluidos}
        aria-valuemin={0}
        aria-valuemax={meta}
      >
        <motion.i
          initial={{ width: 0 }}
          animate={{ width: `${percentual}%` }}
          transition={{ type: "spring", visualDuration: 0.5, bounce: 0 }}
        />
      </div>

      {!metaAlcancada && (
        <button
          type="button"
          className="botao-primario dia__acao"
          onClick={onIrParaDesafio}
        >
          {concluidos === 0 ? "Começar o dia" : "Continuar"}
        </button>
      )}

      {revisoes.length > 0 && (
        <div className="dia__revisoes">
          <span className="dia__rotulo">Caindo por tempo parado</span>
          <ul>
            {revisoes.slice(0, REVISOES_VISIVEIS).map((revisao) => (
              <li key={revisao.moduloCodigo}>
                <button
                  type="button"
                  onClick={() => onPraticarModulo(revisao.moduloCodigo)}
                >
                  <span className="dia__revisao-nome">
                    {revisao.moduloNome}
                  </span>
                  <span className="dia__revisao-queda">
                    {revisao.notaQuandoPraticou.toFixed(1).replace(".", ",")} →{" "}
                    <b>{revisao.notaHoje.toFixed(1).replace(".", ",")}</b>
                    {revisao.mudouDeFaixa && <em> mudou de faixa</em>}
                  </span>
                </button>
              </li>
            ))}
          </ul>
          {revisoes.length > REVISOES_VISIVEIS && (
            <p className="dia__revisoes-resto">
              e mais {revisoes.length - REVISOES_VISIVEIS}{" "}
              {plural(
                revisoes.length - REVISOES_VISIVEIS,
                "conceito",
                "conceitos",
              )}
            </p>
          )}
        </div>
      )}
    </section>
  );
}
