import { useState } from "react";
import { api } from "../servicos/api";
import "./TelaDeAutenticacao.css";

/**
 * Define a nova senha a partir do link recebido por e-mail.
 *
 * <p>Reaproveita o CSS da tela de entrada de propósito: é o mesmo momento do fluxo — a
 * pessoa está fora do app tentando entrar — e dar outra aparência aqui faria parecer
 * outro site, justamente na tela em que ela chega por um link de e-mail e precisa
 * confiar no que está vendo.
 */

interface Props {
  token: string;
  /** Chamado quando a senha é trocada: a tela de entrada assume com o aviso. */
  onRedefinida: () => void;
  onCancelar: () => void;
}

const TAMANHO_MINIMO_DA_SENHA = 10;

export function TelaDeRedefinicao({ token, onRedefinida, onCancelar }: Props) {
  const [senha, setSenha] = useState("");
  const [confirmacao, setConfirmacao] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const curtaDemais = senha.length > 0 && senha.length < TAMANHO_MINIMO_DA_SENHA;
  const naoConfere = confirmacao.length > 0 && senha !== confirmacao;
  const podeEnviar =
    senha.length >= TAMANHO_MINIMO_DA_SENHA && senha === confirmacao && !enviando;

  async function enviar(evento: React.FormEvent) {
    evento.preventDefault();
    if (!podeEnviar) {
      return;
    }
    setEnviando(true);
    setErro(null);
    try {
      await api.redefinirSenha(token, senha);
      onRedefinida();
    } catch (falha) {
      setErro(
        falha instanceof Error ? falha.message : "Não foi possível redefinir.",
      );
    } finally {
      setEnviando(false);
      setSenha("");
      setConfirmacao("");
    }
  }

  return (
    <div className="autenticacao">
      <form className="autenticacao__cartao" onSubmit={(e) => void enviar(e)}>
        <h1>Nova senha</h1>
        <p className="autenticacao__subtitulo">
          Escolha uma senha de pelo menos {TAMANHO_MINIMO_DA_SENHA} caracteres.
          Uma frase que só você lembra vale mais do que símbolos embaralhados.
        </p>

        <label className="autenticacao__campo">
          <span>Nova senha</span>
          <input
            type="password"
            value={senha}
            autoComplete="new-password"
            onChange={(evento) => setSenha(evento.target.value)}
            disabled={enviando}
          />
        </label>

        <label className="autenticacao__campo">
          <span>Repita a nova senha</span>
          <input
            type="password"
            value={confirmacao}
            autoComplete="new-password"
            onChange={(evento) => setConfirmacao(evento.target.value)}
            disabled={enviando}
          />
        </label>

        {/*
          Avisos enquanto digita, e não só ao enviar: descobrir que as senhas não
          conferem depois de clicar significa digitar as duas de novo.
        */}
        {curtaDemais && (
          <p className="autenticacao__erro" role="alert">
            Faltam {TAMANHO_MINIMO_DA_SENHA - senha.length} caracteres.
          </p>
        )}
        {naoConfere && (
          <p className="autenticacao__erro" role="alert">
            As duas senhas não são iguais.
          </p>
        )}
        {erro && (
          <p className="autenticacao__erro" role="alert">
            {erro}
          </p>
        )}

        <button type="submit" className="botao-primario" disabled={!podeEnviar}>
          {enviando ? "Salvando..." : "Salvar a nova senha"}
        </button>

        <button
          type="button"
          className="autenticacao__alternar"
          onClick={onCancelar}
        >
          Voltar para a entrada
        </button>
      </form>
    </div>
  );
}
