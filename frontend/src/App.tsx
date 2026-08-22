import { useCallback, useEffect, useState } from "react";
import { BarraLateral, type Aba } from "./componentes/BarraLateral";
import { api } from "./servicos/api";
import { TelaDaTrilha } from "./telas/TelaDaTrilha";
import { TelaDeAutenticacao } from "./telas/TelaDeAutenticacao";
import { TelaDeConfiguracoes } from "./telas/TelaDeConfiguracoes";
import { TelaDeConteudo } from "./telas/TelaDeConteudo";
import { TelaDeDesafio } from "./telas/TelaDeDesafio";
import { TelaDeProgresso } from "./telas/TelaDeProgresso";
import type { UsuarioAutenticado } from "./tipos";
import "./App.css";

const TITULO_DA_ABA: Record<Aba, string> = {
  modulos: "Trilha",
  conteudo: "Conteúdo",
  desafio: "Desafio",
  progresso: "Progresso",
  configuracoes: "Configurações",
};

/** `undefined` enquanto a sessão está sendo verificada; `null` quando não há sessão. */
type SessaoConhecida = UsuarioAutenticado | null | undefined;

export default function App() {
  const [usuario, setUsuario] = useState<SessaoConhecida>(undefined);
  const [abaAtiva, setAbaAtiva] = useState<Aba>("modulos");

  /**
   * Quem manda sobre a sessão é o servidor: o app pergunta a ele quem está logado em
   * vez de guardar um sinalizador local. Assim uma sessão expirada ou revogada leva
   * de volta para a entrada na primeira requisição, sem depender de o cliente ser
   * honesto sobre o próprio estado.
   */
  useEffect(() => {
    let cancelado = false;

    api
      .buscarUsuario()
      .then((perfil) => {
        if (!cancelado) {
          setUsuario({ id: perfil.id, nome: perfil.nome, email: perfil.email });
        }
      })
      .catch(() => {
        if (!cancelado) setUsuario(null);
      });

    return () => {
      cancelado = true;
    };
  }, []);

  /**
   * Qual módulo está aberto para estudo. Fica aqui e não na tela de conteúdo porque
   * o desafio também precisa apontar para ele no "voltar para o conteúdo".
   */
  const [moduloEmEstudo, setModuloEmEstudo] = useState<string | null>(null);

  /**
   * Módulo que o aluno pediu para praticar logo depois de estudar. Separado de
   * moduloEmEstudo porque abrir o conteúdo não deve, sozinho, mudar o desafio da vez.
   */
  const [moduloParaPraticar, setModuloParaPraticar] = useState<string | null>(null);

  /**
   * Contador de invalidação: quando uma nota muda, a trilha e o progresso
   * precisam recarregar, porque a nota nova já altera a próxima decisão do agente.
   */
  const [versaoDosDados, setVersaoDosDados] = useState(0);
  const invalidarDados = useCallback(() => setVersaoDosDados((versao) => versao + 1), []);

  const abrirConteudo = useCallback((codigoDoModulo: string) => {
    setModuloEmEstudo(codigoDoModulo);
    setAbaAtiva("conteudo");
  }, []);

  const praticarModulo = useCallback((codigoDoModulo: string) => {
    setModuloParaPraticar(codigoDoModulo);
    setAbaAtiva("desafio");
  }, []);

  /** Entrar pelo menu é pedir o desafio da vez, então a escolha volta ao orquestrador. */
  const trocarAba = useCallback((aba: Aba) => {
    if (aba === "desafio") {
      setModuloParaPraticar(null);
    }
    setAbaAtiva(aba);
  }, []);

  const sair = useCallback(async () => {
    // Mesmo se a chamada falhar, o estado local volta para a entrada: manter a tela
    // do app aberta depois de um pedido de saída é pior do que uma saída incompleta.
    await api.sair().catch(() => undefined);
    setUsuario(null);
    setAbaAtiva("modulos");
    setModuloEmEstudo(null);
    setModuloParaPraticar(null);
  }, []);

  if (usuario === undefined) {
    return <div className="aplicacao__carregando">Carregando...</div>;
  }

  if (usuario === null) {
    return <TelaDeAutenticacao onEntrou={setUsuario} />;
  }

  return (
    <div className="aplicacao">
      <BarraLateral
        abaAtiva={abaAtiva}
        onTrocarAba={trocarAba}
        nomeDoUsuario={usuario.nome}
        onSair={() => void sair()}
      />

      <main className="aplicacao__conteudo">
        <h1 className="aplicacao__titulo">{TITULO_DA_ABA[abaAtiva]}</h1>

        {abaAtiva === "modulos" && (
          <TelaDaTrilha
            versao={versaoDosDados}
            onEstudarModulo={abrirConteudo}
            onIrParaDesafio={() => trocarAba("desafio")}
          />
        )}

        {abaAtiva === "conteudo" && moduloEmEstudo && (
          <TelaDeConteudo
            codigoDoModulo={moduloEmEstudo}
            onComecarExercicios={() => praticarModulo(moduloEmEstudo)}
            onVoltarParaTrilha={() => setAbaAtiva("modulos")}
          />
        )}

        {/* Sem módulo escolhido a aba de conteúdo não tem o que mostrar. */}
        {abaAtiva === "conteudo" && !moduloEmEstudo && (
          <p className="aplicacao__vazio">
            Escolha um módulo na trilha para ver o conteúdo dele.
          </p>
        )}

        {abaAtiva === "desafio" && (
          <TelaDeDesafio
            moduloParaPraticar={moduloParaPraticar}
            onNotaAtualizada={invalidarDados}
            onVerConteudo={abrirConteudo}
          />
        )}

        {abaAtiva === "progresso" && <TelaDeProgresso versao={versaoDosDados} />}

        {abaAtiva === "configuracoes" && (
          <TelaDeConfiguracoes onPreferenciasSalvas={invalidarDados} />
        )}
      </main>
    </div>
  );
}
