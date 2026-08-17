import type { NivelComModulos, Modulo } from "../tipos";
import { IndicadorDeNota } from "./IndicadorDeNota";
import "./ListaDeModulos.css";

interface Props {
  niveis: NivelComModulos[];
  moduloEmDestaque?: string;
  onEstudarModulo: (codigoDoModulo: string) => void;
}

/**
 * Lista densa da trilha, agrupada por nível CEFR: linhas separadas por borda fina,
 * não cards arredondados soltos. É a trilha em si, então a progressão precisa
 * ficar legível de cima a baixo.
 */
export function ListaDeModulos({ niveis, moduloEmDestaque, onEstudarModulo }: Props) {
  return (
    <div className="lista-de-modulos">
      {niveis.map((nivel) => (
        <section key={nivel.nivel} className="lista-de-modulos__nivel">
          <header className="lista-de-modulos__cabecalho">
            <h2>{nivel.nivel}</h2>
            <span className="lista-de-modulos__contagem">
              {nivel.modulos.length} {nivel.modulos.length === 1 ? "módulo" : "módulos"}
            </span>
          </header>

          <ul className="lista-de-modulos__linhas">
            {nivel.modulos.map((modulo) => (
              <LinhaDeModulo
                key={modulo.id}
                modulo={modulo}
                emDestaque={modulo.codigo === moduloEmDestaque}
                onEstudar={() => onEstudarModulo(modulo.codigo)}
              />
            ))}
          </ul>
        </section>
      ))}
    </div>
  );
}

interface PropsDaLinha {
  modulo: Modulo;
  emDestaque: boolean;
  onEstudar: () => void;
}

function LinhaDeModulo({ modulo, emDestaque, onEstudar }: PropsDaLinha) {
  const classes = [
    "linha-de-modulo",
    modulo.liberado ? "" : "linha-de-modulo--bloqueado",
    emDestaque ? "linha-de-modulo--destaque" : "",
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <li className={classes}>
      {/*
       * A linha inteira é o botão: o conteúdo do módulo é o destino natural de
       * clicar num conceito da trilha. Módulo bloqueado não abre — ainda não há
       * o que estudar antes dos pré-requisitos.
       */}
      <button
        type="button"
        className="linha-de-modulo__gatilho"
        onClick={onEstudar}
        disabled={!modulo.liberado}
        aria-label={`Ver o conteúdo de ${modulo.nome}`}
      >
        <span className="linha-de-modulo__identificacao">
          <span className="linha-de-modulo__nome">{modulo.nome}</span>
          <span className="linha-de-modulo__descricao">{modulo.descricao}</span>

          {!modulo.liberado && modulo.preRequisitosPendentes.length > 0 && (
            <span className="linha-de-modulo__bloqueio">
              Depende de: {modulo.preRequisitosPendentes.join(", ")}
            </span>
          )}
        </span>

        <IndicadorDeNota nota={modulo.nota} faixa={modulo.faixa} />
      </button>
    </li>
  );
}
