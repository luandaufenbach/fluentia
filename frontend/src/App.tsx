import { useCallback, useEffect, useState } from "react";
import { BarraLateral, type Aba } from "./componentes/BarraLateral";
import { api, registrarPerdaDeSessao } from "./servicos/api";
import { TelaDaTrilha } from "./telas/TelaDaTrilha";
import { TelaDeAutenticacao } from "./telas/TelaDeAutenticacao";
import { TelaDeConfiguracoes } from "./telas/TelaDeConfiguracoes";
import { TelaDeConteudo } from "./telas/TelaDeConteudo";
import { TelaDeDesafio } from "./telas/TelaDeDesafio";
import { TelaDeNivelamento } from "./telas/TelaDeNivelamento";
import { TelaDeProgresso } from "./telas/TelaDeProgresso";
import { TelaDeRedefinicao } from "./telas/TelaDeRedefinicao";
import { TelaDoPainel } from "./telas/TelaDoPainel";
import type { UsuarioAutenticado } from "./tipos";
import "./App.css";

const TITULO_DA_ABA: Record<Aba, string> = {
  modulos: "Trilha",
  conteudo: "Conteúdo",
  desafio: "Desafio",
  progresso: "Progresso",
  configuracoes: "Configurações",
  admin: "Painel",
};

/** `undefined` enquanto a sessão está sendo verificada; `null` quando não há sessão. */
type SessaoConhecida = UsuarioAutenticado | null | undefined;

export default function App() {
  const [usuario, setUsuario] = useState<SessaoConhecida>(undefined);

  /*
   * Lido uma vez, na montagem. O token some da URL assim que a senha é trocada — não
   * pode ficar no endereço, onde entra no histórico do navegador e em qualquer print
   * de tela que a pessoa venha a compartilhar.
   */
  const [tokenDeRecuperacao, setTokenDeRecuperacao] = useState<string | null>(
    () => new URLSearchParams(window.location.search).get("recuperacao"),
  );
  const [senhaRedefinida, setSenhaRedefinida] = useState(false);

  /*
   * O papel vem do perfil, não do login: a resposta de entrada não traz papel de
   * propósito. Fica separado do usuário autenticado porque as duas informações têm
   * origens diferentes e chegam em momentos diferentes.
   */
  const [ehAdministrador, setEhAdministrador] = useState(false);

  const limparTokenDaUrl = useCallback(() => {
    setTokenDeRecuperacao(null);
    window.history.replaceState(null, "", window.location.pathname);
  }, []);
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
          setEhAdministrador(perfil.ehAdministrador);
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
   * Sessão recusada pelo servidor leva de volta para a entrada.
   *
   * A sessão expira em 60 minutos, e no celular isso é rotina: a pessoa volta ao
   * app no dia seguinte e via "Autenticação necessária" impressa no meio de uma
   * tela, sem nenhum caminho de volta. Registrado antes de qualquer requisição
   * para que nem a primeira chamada da sessão caia nesse buraco.
   */
  useEffect(() => {
    registrarPerdaDeSessao(() => {
      setUsuario(null);
      setAbaAtiva("modulos");
    });
  }, []);

  /**
   * Se a conta ainda precisa passar pelo nivelamento. `undefined` enquanto não se sabe.
   *
   * Quem pergunta é o servidor, como na sessão: um sinalizador local seria contornável
   * e, pior, mandaria de novo para o nivelamento quem já o fez em outro dispositivo.
   */
  const [precisaNivelar, setPrecisaNivelar] = useState<boolean | undefined>(
    undefined,
  );

  useEffect(() => {
    if (!usuario) {
      setPrecisaNivelar(undefined);
      return;
    }

    let cancelado = false;
    api
      .situacaoDoNivelamento()
      // Falha aqui não pode bloquear a entrada: na dúvida o app segue para a trilha,
      // que é onde a pessoa consegue usar o produto de qualquer jeito.
      .then((situacao) => !cancelado && setPrecisaNivelar(!situacao.jaFez))
      .catch(() => !cancelado && setPrecisaNivelar(false));

    return () => {
      cancelado = true;
    };
  }, [usuario]);

  /**
   * Qual módulo está aberto para estudo. Fica aqui e não na tela de conteúdo porque
   * o desafio também precisa apontar para ele no "voltar para o conteúdo".
   */
  const [moduloEmEstudo, setModuloEmEstudo] = useState<string | null>(null);

  /**
   * Módulo que o aluno pediu para praticar logo depois de estudar. Separado de
   * moduloEmEstudo porque abrir o conteúdo não deve, sozinho, mudar o desafio da vez.
   */
  const [moduloParaPraticar, setModuloParaPraticar] = useState<string | null>(
    null,
  );

  /**
   * Contador de invalidação: quando uma nota muda, a trilha e o progresso
   * precisam recarregar, porque a nota nova já altera a próxima decisão do agente.
   */
  const [versaoDosDados, setVersaoDosDados] = useState(0);
  const invalidarDados = useCallback(
    () => setVersaoDosDados((versao) => versao + 1),
    [],
  );

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

  /*
   * O token do e-mail chega pela query string, e não por rota: o app é uma página só,
   * sem roteador. Ler `?recuperacao=` aqui resolve o caso inteiro sem trazer uma
   * dependência de roteamento para um app que tem exatamente uma URL.
   */
  if (tokenDeRecuperacao) {
    return (
      <TelaDeRedefinicao
        token={tokenDeRecuperacao}
        onRedefinida={() => {
          // Limpa a URL antes de sair da tela: recarregar a página com o token ainda
          // no endereço traria de volta um formulário para um link já queimado.
          limparTokenDaUrl();
          setSenhaRedefinida(true);
        }}
        onCancelar={limparTokenDaUrl}
      />
    );
  }

  if (usuario === null) {
    return (
      <TelaDeAutenticacao
        onEntrou={setUsuario}
        avisoDeSenhaRedefinida={senhaRedefinida}
      />
    );
  }

  if (precisaNivelar === undefined) {
    return <div className="aplicacao__carregando">Carregando...</div>;
  }

  // O nivelamento ocupa a tela inteira: é a primeira coisa que acontece depois do
  // cadastro, e mostrar a trilha por trás convidaria a ignorá-lo — que é justamente
  // como todo mundo acabava começando em A1.
  if (precisaNivelar) {
    return (
      <TelaDeNivelamento
        onConcluido={() => {
          setPrecisaNivelar(false);
          invalidarDados();
        }}
      />
    );
  }

  return (
    <div className="aplicacao">
      <BarraLateral
        abaAtiva={abaAtiva}
        onTrocarAba={trocarAba}
        nomeDoUsuario={usuario.nome}
        ehAdministrador={ehAdministrador}
        onSair={() => void sair()}
      />

      <main className="aplicacao__conteudo">
        <h1 className="aplicacao__titulo">{TITULO_DA_ABA[abaAtiva]}</h1>

        {abaAtiva === "modulos" && (
          <TelaDaTrilha
            versao={versaoDosDados}
            onEstudarModulo={abrirConteudo}
            onIrParaDesafio={() => trocarAba("desafio")}
            onPraticarModulo={praticarModulo}
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
            onVoltarParaTrilha={() => setAbaAtiva("modulos")}
          />
        )}

        {abaAtiva === "progresso" && (
          <TelaDeProgresso versao={versaoDosDados} />
        )}

        {/*
          A condição do papel também aqui, e não só na barra: sem ela, quem já esteve
          na aba e depois perdesse o papel continuaria na tela até trocar de aba — e
          veria a mensagem de erro do servidor em vez de simplesmente não estar lá.
        */}
        {abaAtiva === "admin" && ehAdministrador && <TelaDoPainel />}

        {abaAtiva === "configuracoes" && (
          <TelaDeConfiguracoes
            onPreferenciasSalvas={invalidarDados}
            onRefazerNivelamento={() => setPrecisaNivelar(true)}
          />
        )}
      </main>
    </div>
  );
}
