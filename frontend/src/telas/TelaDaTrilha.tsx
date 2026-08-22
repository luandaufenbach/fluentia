import { motion } from "motion/react";
import { useEffect, useState } from "react";
import { IndicadorDeNota } from "../componentes/IndicadorDeNota";
import { api } from "../servicos/api";
import type { FaseNaTrilha, Modulo, Sugestao, Trilha } from "../tipos";
import "./TelaDaTrilha.css";

interface Props {
  /** Muda quando uma nota é atualizada, para a trilha recarregar. */
  versao: number;
  onEstudarModulo: (codigoDoModulo: string) => void;
  onIrParaDesafio: () => void;
}

/**
 * A trilha inteira como percurso, não como lista.
 *
 * A lista densa mostrava os módulos, mas não mostrava o caminho: dava para ver que
 * "Voz passiva" existia sem entender por que ele vem depois de tudo, nem o que se
 * ganha ao chegar lá. Aqui cada fase carrega a promessa do que ela destrava, e o
 * estado de cada nó vem da nota real — não de um checkbox que o aluno marca sozinho.
 */
export function TelaDaTrilha({ versao, onEstudarModulo, onIrParaDesafio }: Props) {
  const [trilha, setTrilha] = useState<Trilha | null>(null);
  const [sugestao, setSugestao] = useState<Sugestao | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    let cancelado = false;

    Promise.all([api.listarTrilha(), api.buscarSugestao()])
      .then(([percurso, proxima]) => {
        if (!cancelado) {
          setTrilha(percurso);
          setSugestao(proxima);
        }
      })
      .catch((falha: unknown) => {
        if (!cancelado) {
          setErro(falha instanceof Error ? falha.message : "Não foi possível carregar a trilha.");
        }
      });

    return () => {
      cancelado = true;
    };
  }, [versao]);

  if (erro) {
    return <p className="trilha__estado">{erro}</p>;
  }

  if (!trilha) {
    return <p className="trilha__estado">Carregando a trilha...</p>;
  }

  const progresso = Math.round((trilha.modulosConsolidados / trilha.totalDeModulos) * 100);

  return (
    <div className="trilha">
      <header className="trilha__cabecalho">
        <div className="trilha__medidor">
          <div className="trilha__medidor-texto">
            <span className="trilha__percentual">{progresso}%</span>
            <span className="trilha__contagem">
              {trilha.modulosConsolidados} de {trilha.totalDeModulos} conceitos vencidos
            </span>
          </div>
          <div className="trilha__barra">
            <motion.i
              initial={{ width: 0 }}
              animate={{ width: `${progresso}%` }}
              transition={{ type: "spring", visualDuration: 0.5, bounce: 0 }}
            />
          </div>
        </div>

        {sugestao && (
          <section className="trilha__sugestao">
            <span className="trilha__sugestao-rotulo">Próximo passo</span>
            <h2>{sugestao.moduloNome}</h2>
            <p>{sugestao.motivo}</p>
            <div className="trilha__sugestao-acoes">
              <button
                type="button"
                className="botao-primario"
                onClick={() => onEstudarModulo(sugestao.moduloCodigo)}
              >
                Estudar o conteúdo
              </button>
              <button type="button" className="botao-secundario" onClick={onIrParaDesafio}>
                Ir direto ao desafio
              </button>
            </div>
          </section>
        )}
      </header>

      <div className="trilha__percurso">
        {trilha.fases.map((fase, indice) => (
          <BlocoDaFase
            key={fase.codigo}
            fase={fase}
            numero={indice + 1}
            onEstudarModulo={onEstudarModulo}
          />
        ))}

        <div className="trilha__fim">
          <span className="trilha__bandeira">Inglês que você usa sem pensar</span>
        </div>
      </div>

      <section className="trilha__regras">
        <h2>O que vale mais que o material</h2>
        <ol>
          <li>
            <b>Constância ganha de intensidade.</b> Quinze minutos todo dia batem três horas no
            domingo. O inimigo não é a dificuldade, é a semana pulada.
          </li>
          <li>
            <b>Errar é o mecanismo, não a falha.</b> Aqui o erro vira nota, e a nota escolhe o seu
            próximo desafio. Chutar uma resposta ensina mais do que fechar a tela.
          </li>
          <li>
            <b>Pare de traduzir na cabeça.</b> Leia o exemplo em inglês antes da tradução e tente
            entender pela cena, não pela palavra.
          </li>
          <li>
            <b>O desafio certo é o que você quase acerta.</b> Se está fácil demais, você não está
            aprendendo; se está impossível, a base ainda não fechou — volte um conceito.
          </li>
          <li>
            <b>Troque o ambiente, não só o estudo.</b> Celular em inglês, receita em inglês. O
            estudo são minutos; a exposição pode ser o dia inteiro, de graça.
          </li>
        </ol>
      </section>
    </div>
  );
}

interface PropsDaFase {
  fase: FaseNaTrilha;
  numero: number;
  onEstudarModulo: (codigoDoModulo: string) => void;
}

function BlocoDaFase({ fase, numero, onEstudarModulo }: PropsDaFase) {
  const classes = [
    "fase",
    fase.marcoAlcancado ? "fase--concluida" : "",
    fase.emAndamento ? "fase--em-andamento" : "",
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <section className={classes}>
      <header className="fase__cabecalho">
        <span className="fase__numero">{fase.marcoAlcancado ? "✓" : numero}</span>
        <div className="fase__titulo">
          <h2>{fase.nome}</h2>
          <p className="fase__promessa">{fase.promessa}</p>
        </div>
        <span className="fase__contagem">
          {fase.modulosConsolidados}/{fase.totalDeModulos}
        </span>
      </header>

      {fase.emAndamento && <span className="fase__aqui">Você está aqui</span>}

      <ol className="fase__modulos">
        {fase.modulos.map((modulo, indice) => (
          <NoDoModulo
            key={modulo.codigo}
            modulo={modulo}
            lado={indice % 2 === 0 ? "esquerda" : "direita"}
            onEstudar={() => onEstudarModulo(modulo.codigo)}
          />
        ))}
      </ol>

      <p className={`fase__marco ${fase.marcoAlcancado ? "fase__marco--alcancado" : ""}`}>
        <span className="fase__marco-rotulo">Marco</span>
        {fase.marco}
      </p>
    </section>
  );
}

interface PropsDoNo {
  modulo: Modulo;
  lado: "esquerda" | "direita";
  onEstudar: () => void;
}

function NoDoModulo({ modulo, lado, onEstudar }: PropsDoNo) {
  const classes = [
    "no",
    `no--${lado}`,
    `no--${modulo.faixa.toLowerCase()}`,
    modulo.liberado ? "" : "no--bloqueado",
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <li className={classes}>
      <button
        type="button"
        className="no__gatilho"
        onClick={onEstudar}
        disabled={!modulo.liberado}
        aria-label={`Ver o conteúdo de ${modulo.nome}`}
      >
        <span className="no__identificacao">
          <span className="no__nome">{modulo.nome}</span>
          <span className="no__descricao">{modulo.descricao}</span>
          {!modulo.liberado && modulo.preRequisitosPendentes.length > 0 && (
            <span className="no__bloqueio">
              Abre depois de: {modulo.preRequisitosPendentes.join(", ")}
            </span>
          )}
        </span>

        <IndicadorDeNota nota={modulo.nota} faixa={modulo.faixa} />
      </button>
    </li>
  );
}
