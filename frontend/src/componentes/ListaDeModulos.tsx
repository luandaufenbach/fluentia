import type { NivelComModulos, Modulo } from "../tipos";
import { IndicadorDeNota } from "./IndicadorDeNota";
import "./ListaDeModulos.css";

interface Props {
  niveis: NivelComModulos[];
  moduloEmDestaque?: string;
}

/**
 * Lista densa do curriculo, agrupada por nivel CEFR: linhas separadas por borda fina,
 * nao cards arredondados soltos. E o curriculo em si, entao a progressao precisa
 * ficar legivel de cima a baixo.
 */
export function ListaDeModulos({ niveis, moduloEmDestaque }: Props) {
  return (
    <div className="lista-de-modulos">
      {niveis.map((nivel) => (
        <section key={nivel.nivel} className="lista-de-modulos__nivel">
          <header className="lista-de-modulos__cabecalho">
            <h2>{nivel.nivel}</h2>
            <span className="lista-de-modulos__contagem">
              {nivel.modulos.length} {nivel.modulos.length === 1 ? "modulo" : "modulos"}
            </span>
          </header>

          <ul className="lista-de-modulos__linhas">
            {nivel.modulos.map((modulo) => (
              <LinhaDeModulo
                key={modulo.id}
                modulo={modulo}
                emDestaque={modulo.codigo === moduloEmDestaque}
              />
            ))}
          </ul>
        </section>
      ))}
    </div>
  );
}

function LinhaDeModulo({ modulo, emDestaque }: { modulo: Modulo; emDestaque: boolean }) {
  const classes = [
    "linha-de-modulo",
    modulo.liberado ? "" : "linha-de-modulo--bloqueado",
    emDestaque ? "linha-de-modulo--destaque" : "",
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <li className={classes}>
      <div className="linha-de-modulo__identificacao">
        <span className="linha-de-modulo__nome">{modulo.nome}</span>
        <span className="linha-de-modulo__descricao">{modulo.descricao}</span>

        {!modulo.liberado && modulo.preRequisitosPendentes.length > 0 && (
          <span className="linha-de-modulo__bloqueio">
            Depende de: {modulo.preRequisitosPendentes.join(", ")}
          </span>
        )}
      </div>

      <IndicadorDeNota nota={modulo.nota} faixa={modulo.faixa} />
    </li>
  );
}
