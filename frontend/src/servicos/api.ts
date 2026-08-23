import type {
  ConteudoDoModulo,
  Correcao,
  Desafio,
  EtapaDoNivelamento,
  NivelComModulos,
  Preferencias,
  Progresso,
  ResumoDoDia,
  Sugestao,
  Tema,
  Trilha,
  Usuario,
  UsuarioAutenticado,
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

/**
 * Lê o token CSRF do cookie que o backend enviou.
 *
 * O token vive num cookie legível de propósito: o navegador só deixa a página da
 * mesma origem lê-lo, então um site atacante consegue *disparar* a requisição (o
 * cookie de sessão vai junto sozinho) mas não consegue descobrir o token para
 * completá-la. A sessão em si continua fora do alcance de qualquer script, por ser
 * `HttpOnly`.
 */
function tokenDeCsrf(): string | null {
  const encontrado = document.cookie
    .split("; ")
    .find((parte) => parte.startsWith("XSRF-TOKEN="));
  return encontrado
    ? decodeURIComponent(encontrado.slice("XSRF-TOKEN=".length))
    : null;
}

/** Só requisição que altera estado precisa do token. */
const METODOS_SEGUROS = new Set(["GET", "HEAD", "OPTIONS"]);

async function requisitar<T>(
  caminho: string,
  opcoes?: RequestInit,
): Promise<T> {
  const metodo = (opcoes?.method ?? "GET").toUpperCase();
  const cabecalhos: Record<string, string> = {
    "Content-Type": "application/json",
  };

  if (!METODOS_SEGUROS.has(metodo)) {
    const token = tokenDeCsrf();
    if (token) {
      cabecalhos["X-XSRF-TOKEN"] = token;
    }
  }

  const resposta = await fetch(`${BASE}${caminho}`, {
    ...opcoes,
    // O cookie de sessão precisa acompanhar a requisição.
    credentials: "same-origin",
    headers: { ...cabecalhos, ...(opcoes?.headers as Record<string, string>) },
  });

  if (!resposta.ok) {
    // O backend devolve { status, mensagem, momento } no tratador de erros.
    const corpo = await resposta.json().catch(() => null);
    throw new ErroDaApi(
      resposta.status,
      corpo?.mensagem ?? `Falha na requisição (${resposta.status})`,
    );
  }

  if (resposta.status === 204) {
    return undefined as T;
  }
  return (await resposta.json()) as T;
}

export const api = {
  cadastrar: (nome: string, email: string, senha: string) =>
    requisitar<UsuarioAutenticado>("/autenticacao/cadastro", {
      method: "POST",
      body: JSON.stringify({ nome, email, senha }),
    }),

  entrar: (email: string, senha: string) =>
    requisitar<UsuarioAutenticado>("/autenticacao/login", {
      method: "POST",
      body: JSON.stringify({ email, senha }),
    }),

  sair: () => requisitar<void>("/autenticacao/logout", { method: "POST" }),

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

  buscarResumoDoDia: () => requisitar<ResumoDoDia>("/hoje"),

  situacaoDoNivelamento: () => requisitar<{ jaFez: boolean }>("/nivelamento"),

  /** Retoma o nivelamento aberto ou comeca um novo. */
  iniciarNivelamento: () =>
    requisitar<EtapaDoNivelamento>("/nivelamento", { method: "POST" }),

  /** Resposta em branco significa pular, que e um sinal legitimo de teto. */
  responderNivelamento: (
    nivelamentoId: number,
    ordem: number,
    resposta: string,
  ) =>
    requisitar<EtapaDoNivelamento>(`/nivelamento/${nivelamentoId}/resposta`, {
      method: "POST",
      body: JSON.stringify({ ordem, resposta }),
    }),

  abandonarNivelamento: (nivelamentoId: number) =>
    requisitar<void>(`/nivelamento/${nivelamentoId}`, { method: "DELETE" }),
};

export { ErroDaApi };
