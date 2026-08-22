import type {
  ConteudoDoModulo,
  Correcao,
  Desafio,
  NivelComModulos,
  Preferencias,
  Progresso,
  Sugestao,
  Tema,
  Trilha,
  Usuario,
} from "../tipos";

/** O Vite faz proxy de /api para o backend em desenvolvimento (ver vite.config.ts). */
const BASE = "/api";

class ErroDaApi extends Error {
  readonly status: number;

  constructor(status: number, mensagem: string) {
    super(mensagem);
    this.name = "ErroDaApi";
    this.status = status;
  }
}

async function requisitar<T>(caminho: string, opcoes?: RequestInit): Promise<T> {
  const resposta = await fetch(`${BASE}${caminho}`, {
    headers: { "Content-Type": "application/json" },
    ...opcoes,
  });

  if (!resposta.ok) {
    // O backend devolve { status, mensagem, momento } no tratador de erros.
    const corpo = await resposta.json().catch(() => null);
    throw new ErroDaApi(
      resposta.status,
      corpo?.mensagem ?? `Falha na requisicao (${resposta.status})`,
    );
  }

  if (resposta.status === 204) {
    return undefined as T;
  }
  return (await resposta.json()) as T;
}

export const api = {
  buscarUsuario: () => requisitar<Usuario>("/usuario"),

  salvarPreferencias: (preferencias: Preferencias) =>
    requisitar<Usuario>("/usuario/preferencias", {
      method: "PUT",
      body: JSON.stringify(preferencias),
    }),

  listarTrilha: () => requisitar<Trilha>("/trilha"),

  listarModulos: () => requisitar<NivelComModulos[]>("/modulos"),

  buscarConteudo: (codigoDoModulo: string) =>
    requisitar<ConteudoDoModulo>(`/modulos/${codigoDoModulo}/conteudo`),

  listarTemas: () => requisitar<Tema[]>("/temas"),

  buscarProgresso: () => requisitar<Progresso>("/progresso"),

  buscarSugestao: () => requisitar<Sugestao>("/dashboard/sugestao"),

  /** Sem modulo, o orquestrador escolhe; com modulo, pratica o que o aluno acabou de estudar. */
  proximoDesafio: (codigoDoModulo?: string) =>
    requisitar<Desafio>(
      codigoDoModulo
        ? `/desafios/proximo?modulo=${encodeURIComponent(codigoDoModulo)}`
        : "/desafios/proximo",
    ),

  responderDesafio: (desafioId: number, resposta: string) =>
    requisitar<Correcao>(`/desafios/${desafioId}/resposta`, {
      method: "POST",
      body: JSON.stringify({ resposta }),
    }),

  historicoDeDesafios: (quantidade = 20) =>
    requisitar<Desafio[]>(`/desafios/historico?quantidade=${quantidade}`),
};

export { ErroDaApi };
