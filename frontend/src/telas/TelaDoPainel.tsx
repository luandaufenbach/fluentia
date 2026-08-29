import { useEffect, useState } from "react";
import { api } from "../servicos/api";
import type { LinhaDoPainel, PainelDoAdministrador } from "../tipos";
import "./TelaDoPainel.css";

/**
 * Painel do administrador: quanto o app está gastando e quem está usando.
 *
 * Só lê. Não há botão para desativar conta, resetar senha ou apagar dado — poder sobre
 * a conta alheia merece um caminho próprio, pensado à parte, e não um botão ao lado de
 * um número que alguém clica sem querer enquanto lê um relatório.
 */

/** Dólar com quatro casas: o custo por conta vive na terceira e na quarta. */
function emDolar(valor: number | null): string {
  if (valor === null) {
    return "—";
  }
  return `US$ ${valor.toFixed(4)}`;
}

function emMilhares(valor: number): string {
  return valor.toLocaleString("pt-BR");
}

function emData(iso: string | null): string {
  if (!iso) {
    return "nunca";
  }
  return new Date(iso).toLocaleDateString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "2-digit",
  });
}

function Cartao({
  rotulo,
  valor,
  detalhe,
}: {
  rotulo: string;
  valor: string;
  detalhe: string;
}) {
  return (
    <div className="painel__cartao">
      <span className="painel__cartao-rotulo">{rotulo}</span>
      <strong className="painel__cartao-valor">{valor}</strong>
      <span className="painel__cartao-detalhe">{detalhe}</span>
    </div>
  );
}

export function TelaDoPainel() {
  const [painel, setPainel] = useState<PainelDoAdministrador | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    api
      .buscarPainel()
      .then(setPainel)
      .catch((falha: unknown) =>
        setErro(
          falha instanceof Error
            ? falha.message
            : "Não foi possível carregar o painel.",
        ),
      );
  }, []);

  if (erro) {
    return (
      <div className="painel__estado">
        <p>{erro}</p>
        {/*
          O 403 aqui quase sempre tem uma causa só, e ela não é falta de permissão: o
          papel entra na autenticação no login, então uma conta promovida enquanto a
          sessão estava aberta continua sendo aluno até sair e entrar de novo. A aba
          aparece (o perfil é lido do banco a cada requisição) e o painel recusa —
          sem esta frase, a contradição não tem explicação na tela.
        */}
        {erro.includes("permiss") && (
          <p className="painel__dica">
            Se a sua conta virou administradora agora, saia e entre de novo: o
            papel é lido no momento do login.
          </p>
        )}
      </div>
    );
  }

  if (!painel) {
    return <p className="painel__estado">Carregando o painel...</p>;
  }

  return (
    <div className="painel">
      {/*
        O aviso vem ANTES dos números, não depois. Um total incompleto lido como se
        fosse o gasto inteiro é pior do que não ter total nenhum: a diferença só
        aparece na fatura, quando não dá mais para agir.
      */}
      {painel.modelosSemPreco.length > 0 && (
        <p className="painel__alerta" role="alert">
          <b>Os totais abaixo estão incompletos.</b> Estes modelos foram usados sem
          preço configurado, então o custo deles não entra na conta:{" "}
          {painel.modelosSemPreco.join(", ")}.
        </p>
      )}

      <section className="painel__resumo">
        <Cartao
          rotulo="Hoje"
          valor={emDolar(painel.hoje.custoUsd)}
          detalhe={`${painel.hoje.chamadas} ${painel.hoje.chamadas === 1 ? "chamada" : "chamadas"}`}
        />
        <Cartao
          rotulo="Últimos 7 dias"
          valor={emDolar(painel.ultimosSeteDias.custoUsd)}
          detalhe={`${painel.ultimosSeteDias.chamadas} chamadas`}
        />
        <Cartao
          rotulo="Desde o início"
          valor={emDolar(painel.total.custoUsd)}
          detalhe={`${emMilhares(painel.total.tokensDeEntrada + painel.total.tokensDeSaida)} tokens`}
        />
        <Cartao
          rotulo="Contas"
          valor={String(painel.contasAtivas)}
          detalhe={`${painel.contasNoTotal} no total`}
        />
      </section>

      {painel.porTipo.length > 0 && (
        <section className="painel__secao">
          <h2>Para onde o dinheiro vai</h2>
          <ul className="painel__tipos">
            {painel.porTipo.map((tipo) => (
              <li key={tipo.tipo}>
                <span className="painel__tipo-nome">
                  {tipo.tipo === "GERACAO_DE_DESAFIO"
                    ? "Gerar desafios"
                    : tipo.tipo === "AVALIACAO_DE_RESPOSTA"
                      ? "Corrigir respostas"
                      : tipo.tipo.replaceAll("_", " ").toLowerCase()}
                </span>
                <span className="painel__tipo-detalhe">
                  {tipo.chamadas} chamadas · {tipo.itensProduzidos} itens
                </span>
                <span className="painel__tipo-custo">
                  {emDolar(tipo.custoUsd)}
                </span>
              </li>
            ))}
          </ul>
        </section>
      )}

      <section className="painel__secao">
        <h2>Contas</h2>
        <p className="painel__ajuda">
          Ordenadas pelo gasto, do maior para o menor.
        </p>

        {/* A tabela rola dentro do próprio quadro: a página nunca rola de lado. */}
        <div className="painel__rolagem">
          <table className="painel__tabela">
            <thead>
              <tr>
                <th scope="col">Conta</th>
                <th scope="col">Situação</th>
                <th scope="col" className="painel__numero">
                  Respondidos
                </th>
                <th scope="col" className="painel__numero">
                  Chamadas
                </th>
                <th scope="col" className="painel__numero">
                  Tokens
                </th>
                <th scope="col" className="painel__numero">
                  Custo
                </th>
                <th scope="col">Último acesso</th>
              </tr>
            </thead>
            <tbody>
              {painel.contas.map((linha) => (
                <LinhaDaConta key={linha.usuarioId} linha={linha} />
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}

function LinhaDaConta({ linha }: { linha: LinhaDoPainel }) {
  return (
    <tr>
      <td>
        <span className="painel__nome">{linha.nome}</span>
        <span className="painel__email">{linha.email}</span>
      </td>
      <td>
        <span className={`painel__marca painel__marca--${situacao(linha).cor}`}>
          {situacao(linha).texto}
        </span>
      </td>
      <td className="painel__numero">{linha.desafiosRespondidos}</td>
      <td className="painel__numero">{linha.chamadas}</td>
      <td className="painel__numero">
        {emMilhares(linha.tokensDeEntrada + linha.tokensDeSaida)}
      </td>
      <td className="painel__numero painel__custo">
        {emDolar(linha.custoUsd)}
      </td>
      <td className="painel__data">{emData(linha.ultimoAcessoEm)}</td>
    </tr>
  );
}

/**
 * Um estado por conta, na ordem em que importam.
 *
 * Bloqueada vence administrador porque é o que exige atenção agora; papel é rótulo,
 * bloqueio é acontecimento.
 */
function situacao(linha: LinhaDoPainel): { texto: string; cor: string } {
  if (!linha.ativo) {
    return { texto: "inativa", cor: "cinza" };
  }
  if (linha.bloqueada) {
    return { texto: "bloqueada", cor: "vermelho" };
  }
  if (linha.papel === "ADMINISTRADOR") {
    return { texto: "admin", cor: "amarelo" };
  }
  return { texto: "ativa", cor: "verde" };
}
