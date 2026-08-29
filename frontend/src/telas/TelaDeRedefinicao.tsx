import { useState } from "react";
import { CampoDeSenha } from "../componentes/CampoDeSenha";
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

        {/*
          O mesmo componente da tela de entrada: o botão de ver precisa existir nos
          três lugares onde se digita senha, senão a pessoa aprende que ele existe e
          descobre que aqui não.

          Os avisos ficam presos ao campo a que se referem, e aparecem enquanto digita:
          descobrir que as senhas não conferem depois de clicar significa digitar as
          duas de novo.
        */}
        <CampoDeSenha
          rotulo="Nova senha"
          valor={senha}
          onChange={setSenha}
          autoComplete="new-password"
          desabilitado={enviando}
          autoFocus
          ajuda={
            curtaDemais && (
              <small className="autenticacao__ajuda--alerta">
                Faltam {TAMANHO_MINIMO_DA_SENHA - senha.length} caracteres.
              </small>
            )
          }
        />

        <CampoDeSenha
          rotulo="Repita a nova senha"
          valor={confirmacao}
          onChange={setConfirmacao}
          autoComplete="new-password"
          desabilitado={enviando}
          ajuda={
            naoConfere && (
              <small className="autenticacao__ajuda--alerta">
                As duas senhas não são iguais.
              </small>
            )
          }
        />
        {erro && (
          <p className="autenticacao__erro" role="alert">
            {erro}
          </p>
        )}

        <button type="submit" className="botao-primario" disabled={!podeEnviar}>
          {enviando ? "Salvando..." : "Salvar a nova senha"}
        </button>

        {/* Mesmo rodapé da tela de entrada: um único caminho de volta, discreto. */}
        <div className="autenticacao__rodape">
          <button
            type="button"
            className="autenticacao__link autenticacao__link--discreto"
            onClick={onCancelar}
          >
            Voltar para a entrada
          </button>
        </div>
      </form>
    </div>
  );
}
