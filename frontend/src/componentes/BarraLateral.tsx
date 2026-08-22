import { useEffect, useState } from "react";
import { api } from "../servicos/api";
import type { Tema } from "../tipos";
import "./BarraLateral.css";

export type Aba = "modulos" | "conteudo" | "desafio" | "progresso" | "configuracoes";

interface Props {
  abaAtiva: Aba;
  onTrocarAba: (aba: Aba) => void;
}

/**
 * "Conteúdo" não entra aqui porque depende de um módulo escolhido: chega-se a ele
 * pela trilha ou pelo desafio, nunca do nada.
 */
const ABAS: { id: Aba; rotulo: string }[] = [
  { id: "modulos", rotulo: "Trilha" },
  { id: "desafio", rotulo: "Desafio" },
  { id: "progresso", rotulo: "Progresso" },
  { id: "configuracoes", rotulo: "Configurações" },
];

/**
 * Navegação lateral. Os temas aparecem como contexto do desafio — são cena,
 * não a trilha: a trilha é a lista de módulos por nível.
 */
export function BarraLateral({ abaAtiva, onTrocarAba }: Props) {
  const [temas, setTemas] = useState<Tema[]>([]);

  useEffect(() => {
    api.listarTemas().then(setTemas).catch(() => setTemas([]));
  }, []);

  return (
    <aside className="barra-lateral">
      <div className="barra-lateral__marca">
        <span className="barra-lateral__ponto" aria-hidden="true" />
        <span className="barra-lateral__nome">Fluentia</span>
      </div>

      <nav className="barra-lateral__navegacao" aria-label="Navegação principal">
        {ABAS.map((aba) => (
          <button
            key={aba.id}
            type="button"
            className={`barra-lateral__aba ${
              abaAtiva === aba.id ? "barra-lateral__aba--ativa" : ""
            }`}
            onClick={() => onTrocarAba(aba.id)}
            aria-current={abaAtiva === aba.id ? "page" : undefined}
          >
            {aba.rotulo}
          </button>
        ))}
      </nav>

      {temas.length > 0 && (
        <section className="barra-lateral__temas">
          <h2 className="barra-lateral__titulo">Temas das cenas</h2>
          <ul className="barra-lateral__lista-de-temas">
            {temas.map((tema) => (
              <li key={tema.id} title={tema.descricao}>
                {tema.nome}
              </li>
            ))}
          </ul>
          <p className="barra-lateral__nota">
            O tema dá a roupagem do desafio. O que recebe nota é o conceito, na lista da trilha.
          </p>
        </section>
      )}
    </aside>
  );
}
