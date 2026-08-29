import { useEffect, useState } from "react";
import { api } from "../servicos/api";
import type { Tema } from "../tipos";
import "./BarraLateral.css";

export type Aba =
  | "modulos"
  | "conteudo"
  | "desafio"
  | "progresso"
  | "configuracoes"
  | "admin";

interface Props {
  abaAtiva: Aba;
  onTrocarAba: (aba: Aba) => void;
  nomeDoUsuario: string;
  /** Mostra a aba do painel. Quem nao e administrador nem sabe que ela existe. */
  ehAdministrador: boolean;
  onSair: () => void;
}

/**
 * "Conteúdo" não entra aqui porque depende de um módulo escolhido: chega-se a ele
 * pela trilha ou pelo desafio, nunca do nada.
 *
 * O ícone é um glifo, não uma imagem: no celular o rótulo sozinho fica pequeno
 * demais para distinguir de relance, e carregar uma fonte de ícones por quatro
 * símbolos custaria mais do que resolve.
 */
const ABAS: { id: Aba; rotulo: string; icone: string }[] = [
  { id: "modulos", rotulo: "Trilha", icone: "◈" },
  { id: "desafio", rotulo: "Desafio", icone: "✎" },
  { id: "progresso", rotulo: "Progresso", icone: "◑" },
  { id: "configuracoes", rotulo: "Ajustes", icone: "⚙" },
];

/** Fora do array acima para deixar claro que ela não é para todo mundo. */
const ABA_DO_ADMIN = { id: "admin" as const, rotulo: "Painel", icone: "▤" };

/**
 * Navegação do app.
 *
 * São dois desenhos com o mesmo conteúdo, trocados por largura. No desktop, coluna
 * à esquerda. No celular, uma barra fixa no rodapé: navegação que rola junto com a
 * página some assim que o aluno desce a tela, e a trilha tem seis telas de altura —
 * ele teria que voltar ao topo só para trocar de aba. O rodapé também é onde o
 * polegar alcança sem trocar a mão de posição.
 */
export function BarraLateral({
  ehAdministrador,
  abaAtiva,
  onTrocarAba,
  nomeDoUsuario,
  onSair,
}: Props) {
  const [temas, setTemas] = useState<Tema[]>([]);

  useEffect(() => {
    api
      .listarTemas()
      .then(setTemas)
      .catch(() => setTemas([]));
  }, []);

  return (
    <aside className="barra-lateral">
      <div className="barra-lateral__topo">
        <div className="barra-lateral__marca">
          <span className="barra-lateral__ponto" aria-hidden="true" />
          <span className="barra-lateral__nome">Fluentia</span>
        </div>

        {/* No celular a conta sobe para o topo: o rodapé é só navegação, e
            "Sair" ao lado das abas viraria toque errado. */}
        <div className="barra-lateral__conta">
          <span className="barra-lateral__quem" title={nomeDoUsuario}>
            {nomeDoUsuario}
          </span>
          <button
            type="button"
            className="barra-lateral__sair"
            onClick={onSair}
          >
            Sair
          </button>
        </div>
      </div>

      <nav className="barra-lateral__navegacao" aria-label="Navegação principal">
        {/*
          A aba do painel só existe para quem é administrador. Isto é conveniência de
          interface, não segurança: quem souber o endereço da API ainda bate nela
          direto — e quem barra é o papel exigido no servidor, que é onde a regra
          precisa morar.
        */}
        {(ehAdministrador ? [...ABAS, ABA_DO_ADMIN] : ABAS).map((aba) => (
          <button
            key={aba.id}
            type="button"
            className={`barra-lateral__aba ${
              abaAtiva === aba.id ? "barra-lateral__aba--ativa" : ""
            }`}
            onClick={() => onTrocarAba(aba.id)}
            aria-current={abaAtiva === aba.id ? "page" : undefined}
          >
            <span className="barra-lateral__icone" aria-hidden="true">
              {aba.icone}
            </span>
            <span className="barra-lateral__rotulo">{aba.rotulo}</span>
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
            O tema dá a roupagem do desafio. O que recebe nota é o conceito, na
            lista da trilha.
          </p>
        </section>
      )}
    </aside>
  );
}
