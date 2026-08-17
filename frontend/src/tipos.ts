/** Tipos espelhando os contratos da API do backend. */

export type NivelCefr = "A1" | "A2" | "B1" | "B2" | "C1" | "C2";

export type FaixaDeNota = "NOVO" | "VERMELHO" | "AMARELO" | "VERDE";

/** O campo formato ja preve audio na fase 2, sem refazer a tela de desafio. */
export type FormatoDoDesafio = "TEXTO" | "CONVERSA" | "AUDIO";

export type StatusDoDesafio = "AGUARDANDO_RESPOSTA" | "AVALIADO" | "DESCARTADO";

export type ObjetivoDoUsuario = "VIAGEM" | "TRABALHO" | "DEV" | "CONVERSACAO_GERAL";

export type TipoDeCorrecao = "RESUMIDA" | "DETALHADA";

export interface Modulo {
  id: number;
  codigo: string;
  nome: string;
  descricao: string;
  nivel: NivelCefr;
  /** null quando o modulo nunca foi praticado — a lista mostra "novo". */
  nota: number | null;
  faixa: FaixaDeNota;
  liberado: boolean;
  preRequisitosPendentes: string[];
  dataDaUltimaPratica: string | null;
  quantidadeDePraticas: number;
}

export interface NivelComModulos {
  nivel: NivelCefr;
  modulos: Modulo[];
}

export interface Desafio {
  id: number;
  enunciado: string;
  contextoDaCena: string | null;
  formato: FormatoDoDesafio;
  status: StatusDoDesafio;
  moduloCodigo: string;
  moduloNome: string;
  temaNome: string;
  motivoDaEscolha: string;
  criadoEm: string;
}

export interface ErroDetectado {
  tipo: string;
  trechoErrado: string | null;
  correcao: string | null;
  explicacao: string;
}

export interface Correcao {
  desafioId: number;
  notaDaResposta: number;
  feedback: string;
  erros: ErroDetectado[];
  notaDoModulo: number;
  faixaDoModulo: FaixaDeNota;
  moduloNome: string;
}

export interface Progresso {
  quantidadePorFaixa: Record<FaixaDeNota, number>;
  totalDeModulos: number;
  modulosLiberados: number;
  precisamDeAtencao: Modulo[];
}

export interface Sugestao {
  moduloCodigo: string;
  moduloNome: string;
  temaNome: string;
  motivo: string;
}

export interface Tema {
  id: number;
  codigo: string;
  nome: string;
  descricao: string;
}

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  objetivo: ObjetivoDoUsuario;
  minutosPorDia: number;
  tipoDeCorrecao: TipoDeCorrecao;
  nivelEstimado: NivelCefr | null;
}

export interface Preferencias {
  objetivo?: ObjetivoDoUsuario;
  minutosPorDia?: number;
  tipoDeCorrecao?: TipoDeCorrecao;
  nivelEstimado?: NivelCefr;
}
