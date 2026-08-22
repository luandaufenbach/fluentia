package br.com.agenteingles.seguranca;

/** O que a trilha de auditoria registra. */
public enum TipoDeEventoDeAutenticacao {
    CADASTRO,
    LOGIN_COM_SUCESSO,
    LOGIN_RECUSADO,
    CONTA_BLOQUEADA,
    LOGOUT,
    SENHA_TROCADA
}
