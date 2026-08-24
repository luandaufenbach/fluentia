package br.com.agenteingles.seguranca;

/** O que a trilha de auditoria registra. */
public enum TipoDeEventoDeAutenticacao {
    CADASTRO,
    LOGIN_COM_SUCESSO,
    LOGIN_RECUSADO,
    CONTA_BLOQUEADA,
    LOGOUT,
    SENHA_TROCADA,

    /**
     * Recusa por limite de origem. Existe como tipo proprio por dois motivos: um limite
     * que ninguem consegue observar e um limite que ninguem consegue calibrar, e separar
     * do LOGIN_RECUSADO evita que a recusa por limite realimente o contador que a causou.
     */
    LIMITE_DE_ORIGEM
}
