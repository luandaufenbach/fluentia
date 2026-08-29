import { useState } from "react";
import "./CampoDeSenha.css";

/**
 * Campo de senha com botão de ver.
 *
 * <p>Existe como componente porque o mesmo campo aparece em três lugares — entrar,
 * criar conta e redefinir — e o botão de ver tem detalhes fáceis de errar em cópia:
 * `type="button"` para não enviar o formulário, o rótulo mudando junto do estado, e o
 * `autoComplete` certo para o gerenciador de senhas saber se preenche ou gera uma nova.
 *
 * <p>Digitar senha às cegas é a maior causa de erro de digitação em cadastro, e no
 * celular — onde o teclado erra sozinho — é pior. Poder conferir resolve mais do que
 * qualquer mensagem de erro depois.
 */

interface Props {
  rotulo: string;
  valor: string;
  onChange: (valor: string) => void;
  /**
   * Diz ao gerenciador de senhas o que fazer: preencher a existente
   * (`current-password`) ou guardar uma nova (`new-password`).
   */
  autoComplete: "current-password" | "new-password";
  desabilitado?: boolean;
  /** Texto de apoio abaixo do campo. */
  ajuda?: React.ReactNode;
  autoFocus?: boolean;
}

export function CampoDeSenha({
  rotulo,
  valor,
  onChange,
  autoComplete,
  desabilitado,
  ajuda,
  autoFocus,
}: Props) {
  const [visivel, setVisivel] = useState(false);

  return (
    <label className="autenticacao__campo campo-de-senha">
      <span>{rotulo}</span>

      <div className="campo-de-senha__caixa">
        <input
          type={visivel ? "text" : "password"}
          value={valor}
          onChange={(evento) => onChange(evento.target.value)}
          autoComplete={autoComplete}
          maxLength={128}
          disabled={desabilitado}
          autoFocus={autoFocus}
        />

        {/*
          `type="button"` é obrigatório: dentro de um formulário, botão sem tipo é
          submit por padrão, e clicar em "ver" enviaria o cadastro pela metade.

          O rótulo acompanha o estado e é lido por leitor de tela — só o ícone diria
          "olho", que não informa se a senha está visível agora ou não.
        */}
        <button
          type="button"
          className="campo-de-senha__ver"
          onClick={() => setVisivel((estava) => !estava)}
          disabled={desabilitado}
          aria-label={visivel ? "Ocultar a senha" : "Mostrar a senha"}
          aria-pressed={visivel}
          // Fora da ordem de tabulação: quem navega por teclado quer ir do campo
          // para o próximo campo, não parar num controle acessório no caminho.
          tabIndex={-1}
        >
          <span aria-hidden="true">{visivel ? "◍" : "◌"}</span>
        </button>
      </div>

      {ajuda}
    </label>
  );
}
