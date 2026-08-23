import { motion } from "motion/react";
import { useState } from "react";
import { api } from "../servicos/api";
import type { UsuarioAutenticado } from "../tipos";
import "./TelaDeAutenticacao.css";

interface Props {
  onEntrou: (usuario: UsuarioAutenticado) => void;
}

type Modo = "entrar" | "cadastrar";

/** Espelha o mínimo exigido pelo backend — a validação de verdade é lá. */
const TAMANHO_MINIMO_DA_SENHA = 10;

/**
 * Entrada e cadastro.
 *
 * A senha nunca sai daqui a não ser dentro da requisição: não vai para estado global,
 * nem para armazenamento local, nem para log. O que fica depois do login é o cookie
 * de sessão, que o próprio navegador guarda fora do alcance de JavaScript.
 */
export function TelaDeAutenticacao({ onEntrou }: Props) {
  const [modo, setModo] = useState<Modo>("entrar");
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  const cadastrando = modo === "cadastrar";
  const senhaCurta =
    cadastrando && senha.length > 0 && senha.length < TAMANHO_MINIMO_DA_SENHA;

  const podeEnviar =
    email.trim().length > 0 &&
    senha.length > 0 &&
    (!cadastrando ||
      (nome.trim().length > 0 && senha.length >= TAMANHO_MINIMO_DA_SENHA));

  async function enviar(evento: React.FormEvent) {
    evento.preventDefault();
    if (!podeEnviar || enviando) {
      return;
    }

    setEnviando(true);
    setErro(null);
    try {
      const usuario = cadastrando
        ? await api.cadastrar(nome, email, senha)
        : await api.entrar(email, senha);
      setSenha("");
      onEntrou(usuario);
    } catch (falha) {
      setErro(
        falha instanceof Error ? falha.message : "Não foi possível continuar.",
      );
      setSenha("");
    } finally {
      setEnviando(false);
    }
  }

  function trocarModo() {
    setModo(cadastrando ? "entrar" : "cadastrar");
    setErro(null);
    setSenha("");
  }

  return (
    <div className="autenticacao">
      <motion.div
        className="autenticacao__cartao"
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ type: "spring", visualDuration: 0.35, bounce: 0 }}
      >
        <div className="autenticacao__marca">
          <span className="autenticacao__ponto" aria-hidden="true" />
          <span className="autenticacao__nome">Fluentia</span>
        </div>

        <h1 className="autenticacao__titulo">
          {cadastrando ? "Criar sua conta" : "Entrar"}
        </h1>
        <p className="autenticacao__subtitulo">
          {cadastrando
            ? "Sua trilha começa no primeiro conceito e avança conforme os seus acertos."
            : "Continue de onde parou."}
        </p>

        <form className="autenticacao__formulario" onSubmit={enviar} noValidate>
          {cadastrando && (
            <label className="autenticacao__campo">
              <span>Nome</span>
              <input
                type="text"
                value={nome}
                onChange={(evento) => setNome(evento.target.value)}
                autoComplete="name"
                maxLength={120}
                disabled={enviando}
                autoFocus
              />
            </label>
          )}

          <label className="autenticacao__campo">
            <span>E-mail</span>
            <input
              type="email"
              value={email}
              onChange={(evento) => setEmail(evento.target.value)}
              autoComplete="username"
              maxLength={180}
              disabled={enviando}
              autoFocus={!cadastrando}
            />
          </label>

          <label className="autenticacao__campo">
            <span>Senha</span>
            <input
              type="password"
              value={senha}
              onChange={(evento) => setSenha(evento.target.value)}
              /* Diz ao gerenciador de senhas se é para criar ou preencher. */
              autoComplete={cadastrando ? "new-password" : "current-password"}
              maxLength={128}
              disabled={enviando}
            />
            {cadastrando && (
              <small
                className={
                  senhaCurta
                    ? "autenticacao__ajuda--alerta"
                    : "autenticacao__ajuda"
                }
              >
                Mínimo de {TAMANHO_MINIMO_DA_SENHA} caracteres. Uma frase que só
                você lembra vale mais que símbolos embaralhados.
              </small>
            )}
          </label>

          {erro && (
            <p className="autenticacao__erro" role="alert">
              {erro}
            </p>
          )}

          <button
            type="submit"
            className="botao-primario"
            disabled={!podeEnviar || enviando}
          >
            {enviando ? "Aguarde..." : cadastrando ? "Criar conta" : "Entrar"}
          </button>
        </form>

        <button
          type="button"
          className="autenticacao__troca"
          onClick={trocarModo}
          disabled={enviando}
        >
          {cadastrando ? "Já tenho conta" : "Criar uma conta"}
        </button>
      </motion.div>
    </div>
  );
}
