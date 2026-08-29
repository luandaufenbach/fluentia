package br.com.agenteingles.admin;

/** Quantos desafios a conta de fato respondeu — a medida de uso que importa. */
public record RespondidosDoUsuario(Long usuarioId, long total) {
}
