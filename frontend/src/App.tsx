import { useCallback, useState } from "react";
import { SidebarDeTemas, type Aba } from "./componentes/SidebarDeTemas";
import { TelaDeConfiguracoes } from "./telas/TelaDeConfiguracoes";
import { TelaDeDesafio } from "./telas/TelaDeDesafio";
import { TelaDeModulos } from "./telas/TelaDeModulos";
import { TelaDeProgresso } from "./telas/TelaDeProgresso";
import "./App.css";

const TITULO_DA_ABA: Record<Aba, string> = {
  modulos: "Curriculo",
  desafio: "Desafio",
  progresso: "Progresso",
  configuracoes: "Configuracoes",
};

export default function App() {
  const [abaAtiva, setAbaAtiva] = useState<Aba>("modulos");

  /**
   * Contador de invalidacao: quando uma nota muda, o curriculo e o progresso
   * precisam recarregar, porque a nota nova ja altera a proxima decisao do agente.
   */
  const [versaoDosDados, setVersaoDosDados] = useState(0);
  const invalidarDados = useCallback(() => setVersaoDosDados((versao) => versao + 1), []);

  return (
    <div className="aplicacao">
      <SidebarDeTemas abaAtiva={abaAtiva} onTrocarAba={setAbaAtiva} />

      <main className="aplicacao__conteudo">
        <h1 className="aplicacao__titulo">{TITULO_DA_ABA[abaAtiva]}</h1>

        {abaAtiva === "modulos" && (
          <TelaDeModulos versao={versaoDosDados} onIrParaDesafio={() => setAbaAtiva("desafio")} />
        )}

        {abaAtiva === "desafio" && <TelaDeDesafio onNotaAtualizada={invalidarDados} />}

        {abaAtiva === "progresso" && <TelaDeProgresso versao={versaoDosDados} />}

        {abaAtiva === "configuracoes" && (
          <TelaDeConfiguracoes onPreferenciasSalvas={invalidarDados} />
        )}
      </main>
    </div>
  );
}
