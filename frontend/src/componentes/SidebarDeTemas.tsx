import { useEffect, useState } from "react";
import { api } from "../servicos/api";
import type { Tema } from "../tipos";
import "./SidebarDeTemas.css";

export type Aba = "modulos" | "desafio" | "progresso" | "configuracoes";

interface Props {
  abaAtiva: Aba;
  onTrocarAba: (aba: Aba) => void;
}

const ABAS: { id: Aba; rotulo: string }[] = [
  { id: "modulos", rotulo: "Curriculo" },
  { id: "desafio", rotulo: "Desafio" },
  { id: "progresso", rotulo: "Progresso" },
  { id: "configuracoes", rotulo: "Configuracoes" },
];

/**
 * Navegacao lateral. Os temas aparecem como contexto do desafio — sao filtro/cena,
 * nao o curriculo: o curriculo e a lista de modulos por nivel.
 */
export function SidebarDeTemas({ abaAtiva, onTrocarAba }: Props) {
  const [temas, setTemas] = useState<Tema[]>([]);

  useEffect(() => {
    api.listarTemas().then(setTemas).catch(() => setTemas([]));
  }, []);

  return (
    <aside className="sidebar">
      <div className="sidebar__marca">
        <span className="sidebar__ponto" aria-hidden="true" />
        <span className="sidebar__nome">Agente de Ingles</span>
      </div>

      <nav className="sidebar__navegacao" aria-label="Navegacao principal">
        {ABAS.map((aba) => (
          <button
            key={aba.id}
            type="button"
            className={`sidebar__aba ${abaAtiva === aba.id ? "sidebar__aba--ativa" : ""}`}
            onClick={() => onTrocarAba(aba.id)}
            aria-current={abaAtiva === aba.id ? "page" : undefined}
          >
            {aba.rotulo}
          </button>
        ))}
      </nav>

      {temas.length > 0 && (
        <section className="sidebar__temas">
          <h2 className="sidebar__titulo">Temas das cenas</h2>
          <ul className="sidebar__lista-de-temas">
            {temas.map((tema) => (
              <li key={tema.id} title={tema.descricao}>
                {tema.nome}
              </li>
            ))}
          </ul>
          <p className="sidebar__nota">
            O tema da a roupagem do desafio. O que recebe nota e o conceito, na lista do curriculo.
          </p>
        </section>
      )}
    </aside>
  );
}
