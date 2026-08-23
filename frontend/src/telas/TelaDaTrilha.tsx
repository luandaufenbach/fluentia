import { motion } from "motion/react";
import { useEffect, useState } from "react";
import { IndicadorDeNota } from "../componentes/IndicadorDeNota";
import { PainelDoDia } from "../componentes/PainelDoDia";
import { api } from "../servicos/api";
import type {
  FaseNaTrilha,
  Modulo,
  ResumoDoDia,
  Sugestao,
  Trilha,
} from "../tipos";
import "./TelaDaTrilha.css";

interface Props {
  /** Muda quando uma nota é atualizada, para a trilha recarregar. */
  versao: number;
  onEstudarModulo: (codigoDoModulo: string) => void;
  onIrParaDesafio: () => void;
  onPraticarModulo: (codigoDoModulo: string) => void;
}

/**
 * A trilha inteira como percurso, não como lista.
 *
 * A lista densa mostrava os módulos, mas não mostrava o caminho: dava para ver que
 * "Voz passiva" existia sem entender por que ele vem depois de tudo, nem o que se
 * ganha ao chegar lá. Aqui cada fase carrega a promessa do que ela destrava, e o
 * estado de cada nó vem da nota real — não de um checkbox que o aluno marca sozinho.
 */
export function TelaDaTrilha({
  versao,
  onEstudarModulo,
  onIrParaDesafio,
  onPraticarModulo,
}: Props) {
  const [trilha, setTrilha] = useState<Trilha | null>(null);
  const [sugestao, setSugestao] = useState<Sugestao | null>(null);
  const [resumoDoDia, setResumoDoDia] = useState<ResumoDoDia | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    let cancelado = false;

    Promise.all([
      api.listarTrilha(),
      api.buscarSugestao(),
      api.buscarResumoDoDia(),
    ])
      .then(([percurso, proxima, dia]) => {
        if (!cancelado) {
          setTrilha(percurso);
          setSugestao(proxima);
          setResumoDoDia(dia);
        }
      })
      .catch((falha: unknown) => {
        if (!cancelado) {
          setErro(
            falha instanceof Error
              ? falha.message
              : "Não foi possível carregar a trilha.",
          );
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

  // O percentual conta só o que foi demonstrado. O estimado entra numa faixa própria da
  // barra: some com o progresso visualmente, sem se passar por conquista.
  const progresso = Math.round(
    (trilha.modulosConsolidados / trilha.totalDeModulos) * 100,
  );
  const progressoPresumido = Math.round(
    (trilha.modulosPresumidos / trilha.totalDeModulos) * 100,
  );

  return (
    <div className="trilha">
      {resumoDoDia && (
        <PainelDoDia
          resumo={resumoDoDia}
          onPraticarModulo={onPraticarModulo}
          onIrParaDesafio={onIrParaDesafio}
        />
      )}

      <header className="trilha__cabecalho">
        <div className="trilha__medidor">
          <div className="trilha__medidor-texto">
            <span className="trilha__percentual">{progresso}%</span>
            <span className="trilha__contagem">
              {trilha.modulosConsolidados} de {trilha.totalDeModulos} conceitos
              vencidos
              {trilha.modulosPresumidos > 0 && (
                <span className="trilha__contagem-presumida">
                  {" "}
                  · {trilha.modulosPresumidos} estimados pelo nivelamento
                </span>
              )}
            </span>
          </div>
          <div className="trilha__barra">
            <motion.i
              initial={{ width: 0 }}
              animate={{ width: `${progresso}%` }}
              transition={{ type: "spring", visualDuration: 0.5, bounce: 0 }}
            />
            {progressoPresumido > 0 && (
              <motion.i
                className="trilha__barra-presumida"
                initial={{ width: 0 }}
                animate={{ width: `${progressoPresumido}%` }}
                transition={{ type: "spring", visualDuration: 0.5, bounce: 0 }}
              />
            )}
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
              <button
                type="button"
                className="botao-secundario"
                onClick={onIrParaDesafio}
              >
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
          <span className="trilha__bandeira">
            Inglês que você usa sem pensar
          </span>
        </div>
      </div>

      <section className="trilha__regras">
        <h2>O que vale mais que o material</h2>
        <ol>
          <li>
            <b>Constância ganha de intensidade.</b> Quinze minutos todo dia
            batem três horas no domingo. O inimigo não é a dificuldade, é a
            semana pulada.
          </li>
          <li>
            <b>Errar é o mecanismo, não a falha.</b> Aqui o erro vira nota, e a
            nota escolhe o seu próximo desafio. Chutar uma resposta ensina mais
            do que fechar a tela.
          </li>
          <li>
            <b>Pare de traduzir na cabeça.</b> Leia o exemplo em inglês antes da
            tradução e tente entender pela cena, não pela palavra.
          </li>
          <li>
            <b>O desafio certo é o que você quase acerta.</b> Se está fácil
            demais, você não está aprendendo; se está impossível, a base ainda
            não fechou — volte um conceito.
          </li>
          <li>
            <b>Troque o ambiente, não só o estudo.</b> Celular em inglês,
            receita em inglês. O estudo são minutos; a exposição pode ser o dia
            inteiro, de graça.
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
  const alcancado = fase.situacaoDoMarco === "ALCANCADO";
  const presumido = fase.situacaoDoMarco === "PRESUMIDO";

  /*
   * Recolher só existe no celular — no desktop a fase inteira cabe na tela e o
   * percurso completo é o que a trilha tem de melhor. O CSS ignora este estado
   * acima de 52rem; aqui ele é só um dado.
   *
   * Abre na fase onde o aluno está. As cinco abertas somam seis telas de rolagem
   * num aparelho comum, e achar a fase 4 vira trabalho de polegar.
   */
  const [aberta, setAberta] = useState(fase.emAndamento);

  const classes = [
    "fase",
    alcancado ? "fase--concluida" : "",
    presumido ? "fase--presumida" : "",
    fase.emAndamento ? "fase--em-andamento" : "",
  ]
    .filter(Boolean)
    .join(" ");

  const conteudoId = `fase-${fase.codigo}`;

  return (
    <section className={classes} data-aberta={aberta}>
      <header className="fase__cabecalho">
        {/* O tique é só de quem praticou. Presumido ganha um traço: nem vazio, nem
            fechado — que é exatamente o estado de quem foi estimado e não provou. */}
        <span className="fase__numero">
          {alcancado ? "✓" : presumido ? "~" : numero}
        </span>
        <div className="fase__titulo">
          <h2>{fase.nome}</h2>
          <p className="fase__promessa">{fase.promessa}</p>
        </div>
        <span className="fase__contagem">
          {fase.modulosConsolidados}/{fase.totalDeModulos}
          {fase.modulosPresumidos > 0 && (
            <span className="fase__contagem-presumida">
              +{fase.modulosPresumidos} estimados
            </span>
          )}
        </span>

        <button
          type="button"
          className="fase__alternar"
          onClick={() => setAberta((estava) => !estava)}
          aria-expanded={aberta}
          aria-controls={conteudoId}
          aria-label={`${aberta ? "Recolher" : "Abrir"} os conceitos de ${fase.nome}`}
        >
          <span aria-hidden="true">▾</span>
        </button>
      </header>

      {fase.emAndamento && <span className="fase__aqui">Você está aqui</span>}

      <ol className="fase__modulos" id={conteudoId}>
        {fase.modulos.map((modulo, indice) => (
          <NoDoModulo
            key={modulo.codigo}
            modulo={modulo}
            lado={indice % 2 === 0 ? "esquerda" : "direita"}
            onEstudar={() => onEstudarModulo(modulo.codigo)}
          />
        ))}
      </ol>

      <p
        className={`fase__marco ${alcancado ? "fase__marco--alcancado" : ""} ${
          presumido ? "fase__marco--presumido" : ""
        }`}
      >
        <span className="fase__marco-rotulo">Marco</span>
        {fase.marco}
        {presumido && (
          <span className="fase__marco-ressalva">
            O nivelamento estimou que você já sabe isto, mas você ainda não demonstrou
            aqui. Praticar os conceitos da fase fecha o marco.
          </span>
        )}
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
  // Nota sem nenhuma prática só pode ter vindo do nivelamento: quem responde um desafio
  // ganha a prática junto com a nota. Sem essa distinção, um 7,0 estimado fica idêntico
  // a um 7,0 conquistado — e a trilha inteira passa a mentir sobre o que foi provado.
  const presumido = modulo.nota !== null && modulo.quantidadeDePraticas === 0;

  const classes = [
    "no",
    `no--${lado}`,
    `no--${modulo.faixa.toLowerCase()}`,
    presumido ? "no--presumido" : "",
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
          <span className="no__nome">
            {modulo.nome}
            {presumido && (
              <span className="no__estimado" title="Estimado pelo nivelamento, ainda não praticado">
                estimado
              </span>
            )}
          </span>
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
