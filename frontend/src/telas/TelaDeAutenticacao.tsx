import { motion } from "motion/react";
import { useState } from "react";
import { CampoDeSenha } from "../componentes/CampoDeSenha";
import { api } from "../servicos/api";
import type { UsuarioAutenticado } from "../tipos";
import "./TelaDeAutenticacao.css";

interface Props {
  onEntrou: (usuario: UsuarioAutenticado) => void;
  /** Mostrado quando a pessoa acabou de trocar a senha pelo link do e-mail. */
  avisoDeSenhaRedefinida?: boolean;
}

type Modo = "entrar" | "cadastrar" | "recuperar";

/** Espelha o mínimo exigido pelo backend — a validação de verdade é lá. */
const TAMANHO_MINIMO_DA_SENHA = 10;

/**
 * Entrada e cadastro.
 *
 * A senha nunca sai daqui a não ser dentro da requisição: não vai para estado global,
 * nem para armazenamento local, nem para log. O que fica depois do login é o cookie
 * de sessão, que o próprio navegador guarda fora do alcance de JavaScript.
 */
export function TelaDeAutenticacao({ onEntrou, avisoDeSenhaRedefinida }: Props) {
  const [modo, setModo] = useState<Modo>("entrar");
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  /** Confirmação do pedido de recuperação. Nunca diz se a conta existe. */
  const [pedidoEnviado, setPedidoEnviado] = useState(false);
  const [confirmacao, setConfirmacao] = useState("");

  const cadastrando = modo === "cadastrar";
  const recuperando = modo === "recuperar";
  const senhaCurta =
    cadastrando && senha.length > 0 && senha.length < TAMANHO_MINIMO_DA_SENHA;
  const naoConfere =
    cadastrando && confirmacao.length > 0 && senha !== confirmacao;

  const podeEnviar =
    email.trim().length > 0 &&
    // Recuperar pede só o e-mail: quem esqueceu a senha não tem o que digitar aqui.
    (recuperando ||
      (senha.length > 0 &&
        (!cadastrando ||
          (nome.trim().length > 0 &&
            senha.length >= TAMANHO_MINIMO_DA_SENHA &&
            senha === confirmacao))));

  async function enviar(evento: React.FormEvent) {
    evento.preventDefault();
    if (!podeEnviar || enviando) {
      return;
    }

    setEnviando(true);
    setErro(null);
    try {
      if (recuperando) {
        await api.pedirRecuperacao(email);
        setPedidoEnviado(true);
        return;
      }
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
      setConfirmacao("");
    } finally {
      setEnviando(false);
    }
  }

  function irPara(destino: Modo) {
    setModo(destino);
    setErro(null);
    setSenha("");
    setConfirmacao("");
    setPedidoEnviado(false);
  }

  function trocarModo() {
    irPara(cadastrando ? "entrar" : "cadastrar");
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
          {recuperando
            ? "Recuperar a senha"
            : cadastrando
              ? "Criar sua conta"
              : "Entrar"}
        </h1>
        <p className="autenticacao__subtitulo">
          {recuperando
            ? "Informe o e-mail da conta. Se houver cadastro, mandamos um link para você definir uma senha nova."
            : cadastrando
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

          {!recuperando && (
            <CampoDeSenha
              rotulo="Senha"
              valor={senha}
              onChange={setSenha}
              autoComplete={cadastrando ? "new-password" : "current-password"}
              desabilitado={enviando}
              ajuda={
                cadastrando && (
                  <small
                    className={
                      senhaCurta
                        ? "autenticacao__ajuda--alerta"
                        : "autenticacao__ajuda"
                    }
                  >
                    Mínimo de {TAMANHO_MINIMO_DA_SENHA} caracteres. Uma frase que
                    só você lembra vale mais que símbolos embaralhados.
                  </small>
                )
              }
            />
          )}

          {/*
            Confirmação só no cadastro. Em quem já tem conta ela não protege de nada:
            senha errada é recusada na hora, com mensagem. No cadastro é diferente —
            um erro de digitação vira uma conta cuja senha ninguém sabe, e a pessoa
            só descobre no login seguinte.

            O aviso aparece enquanto digita, não ao enviar: descobrir depois de
            clicar significa preencher os dois campos de novo.
          */}
          {cadastrando && (
            <CampoDeSenha
              rotulo="Repita a senha"
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
          )}

          {/*
            A confirmação é deliberadamente ambígua: "se houver cadastro". Dizer
            "enviamos" confirmaria que o e-mail tem conta, e este endpoint é público —
            viraria uma consulta de quem está cadastrado, sem precisar de senha.
          */}
          {pedidoEnviado && (
            <p className="autenticacao__aviso" role="status">
              Se houver uma conta com esse e-mail, o link acabou de sair. Ele vale
              por 15 minutos. Confira também a caixa de spam.
            </p>
          )}

          {/*
            Sem isto, quem acabou de redefinir a senha voltaria para a tela de entrada
            sem nenhum sinal de que deu certo — e ficaria em dúvida se deve digitar a
            senha antiga ou a nova.
          */}
          {avisoDeSenhaRedefinida && modo === "entrar" && !pedidoEnviado && (
            <p className="autenticacao__aviso" role="status">
              Senha alterada. Entre com a nova.
            </p>
          )}

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
            {enviando
              ? "Aguarde..."
              : recuperando
                ? "Enviar o link"
                : cadastrando
                  ? "Criar conta"
                  : "Entrar"}
          </button>
        </form>

        {/*
          A troca de modo vem dentro de uma pergunta, não solta num botão.

          "Criar uma conta" sozinho não diz a quem serve; "Não tem uma conta?" diz — e
          quem já tem passa direto sem precisar ler o resto. O botão fica sendo só a
          resposta da frase, que é o papel que ele tem de verdade.
        */}
        <div className="autenticacao__rodape">
          <p className="autenticacao__pergunta">
            {recuperando
              ? "Lembrou a senha?"
              : cadastrando
                ? "Já tem uma conta?"
                : "Não tem uma conta?"}{" "}
            <button
              type="button"
              className="autenticacao__link"
              onClick={() => (recuperando ? irPara("entrar") : trocarModo())}
              disabled={enviando}
            >
              {recuperando || cadastrando ? "Entrar" : "Criar uma conta"}
            </button>
          </p>

          {/*
            Só na entrada: quem está criando conta não tem senha para esquecer, e quem
            já está recuperando não precisa do atalho para onde já está.
          */}
          {modo === "entrar" && (
            <button
              type="button"
              className="autenticacao__link autenticacao__link--discreto"
              onClick={() => irPara("recuperar")}
              disabled={enviando}
            >
              Esqueci minha senha
            </button>
          )}
        </div>
      </motion.div>
    </div>
  );
}
