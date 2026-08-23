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

export interface ExemploDoConteudo {
  emIngles: string;
  emPortugues: string;
  /** null quando a frase dispensa explicacao. */
  observacao: string | null;
}

export interface ErroComumDoConteudo {
  errado: string;
  certo: string;
  explicacao: string;
}

/** O material de estudo do modulo: o aluno le antes de praticar e volta quando erra. */
export interface ConteudoDoModulo {
  moduloCodigo: string;
  moduloNome: string;
  nivel: NivelCefr;
  resumo: string;
  explicacao: string;
  exemplos: ExemploDoConteudo[];
  errosComuns: ErroComumDoConteudo[];
}

export interface FaseNaTrilha {
  codigo: string;
  nome: string;
  /** O que o aluno vai saber fazer ao terminar a fase. */
  promessa: string;
  /** A habilidade concreta que marca o fim da fase. */
  marco: string;
  marcoAlcancado: boolean;
  /** Fase onde o aluno esta agora: ja encostou e ainda nao fechou. */
  emAndamento: boolean;
  modulosConsolidados: number;
  totalDeModulos: number;
  modulos: Modulo[];
}

export interface Trilha {
  fases: FaseNaTrilha[];
  modulosConsolidados: number;
  totalDeModulos: number;
}

/** O que o backend devolve ao entrar. Nunca traz hash, papel nem estado de bloqueio. */
export interface UsuarioAutenticado {
  id: number;
  nome: string;
  email: string;
}

/** Uma pergunta da escada do nivelamento. */
export interface PerguntaDoNivelamento {
  nivelAlvo: NivelCefr;
  /** Em inglês: ler a pergunta já faz parte da medida. */
  pergunta: string;
  /** Uma linha em português, para quem trava antes de começar não desistir. */
  apoio: string;
}

export interface ResultadoDoNivelamento {
  nivel: NivelCefr;
  resumo: string;
  pontoForte: string;
  pontoAFortalecer: string;
  /** Quantos conceitos a estimativa já abriu na trilha. */
  modulosLiberados: number;
  primeiroModulo: string | null;
}

/** Um passo da conversa: ou vem pergunta, ou vem resultado — nunca os dois. */
export interface EtapaDoNivelamento {
  id: number;
  ordem: number;
  total: number;
  perguntaAtual: PerguntaDoNivelamento | null;
  resultado: ResultadoDoNivelamento | null;
}

/** Dias seguidos de prática. Um desafio conta o dia — não precisa ser a sessão inteira. */
export interface SequenciaDeDias {
  atual: number;
  melhor: number;
  praticouHoje: boolean;
}

/** Um conceito que está caindo por falta de prática, não por erro. */
export interface RevisaoPendente {
  moduloCodigo: string;
  moduloNome: string;
  notaQuandoPraticou: number;
  notaHoje: number;
  queda: number;
  faixaHoje: FaixaDeNota;
  /** A queda passou o conceito para uma faixa pior — pode ter fechado o módulo seguinte. */
  mudouDeFaixa: boolean;
  diasSemPraticar: number;
}

/** O dia do aluno: o que ele se propôs a fazer, o que já fez e o que o tempo derrubou. */
export interface ResumoDoDia {
  meta: number;
  concluidos: number;
  restantes: number;
  metaAlcancada: boolean;
  sequencia: SequenciaDeDias;
  revisoes: RevisaoPendente[];
}
