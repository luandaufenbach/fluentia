/**
 * Áudio pelo navegador, nos dois sentidos: falar o inglês e ouvir o aluno.
 *
 * Usa a Web Speech API, que já vem no navegador. A alternativa seria mandar áudio para
 * um serviço de fala — custo por minuto, latência de rede e a voz do aluno saindo da
 * máquina dele. Para ouvir uma frase e ditar uma resposta, nada disso se justifica.
 *
 * O suporte é desigual: síntese existe em praticamente todo lugar, reconhecimento é
 * coisa de Chrome e Edge. Por isso tudo aqui é consultável antes de usar — a interface
 * esconde o que o navegador não tem, em vez de oferecer um botão que não funciona.
 */

/** O reconhecimento de fala não está nos tipos padrão do DOM. */
interface ReconhecimentoDeFala extends EventTarget {
  lang: string;
  continuous: boolean;
  interimResults: boolean;
  start(): void;
  stop(): void;
  abort(): void;
  onresult: ((evento: EventoDeReconhecimento) => void) | null;
  onerror: ((evento: Event & { error?: string }) => void) | null;
  onend: (() => void) | null;
}

interface EventoDeReconhecimento extends Event {
  results: ArrayLike<ArrayLike<{ transcript: string }>>;
}

type ConstrutorDeReconhecimento = new () => ReconhecimentoDeFala;

interface JanelaComFala extends Window {
  SpeechRecognition?: ConstrutorDeReconhecimento;
  webkitSpeechRecognition?: ConstrutorDeReconhecimento;
}

/** Inglês americano: é o sotaque do material e o que o aluno vai ouvir nos exemplos. */
const IDIOMA = "en-US";

function janela(): JanelaComFala | null {
  return typeof window === "undefined" ? null : (window as JanelaComFala);
}

/**
 * A voz em inglês instalada na máquina, se houver alguma.
 *
 * Existe porque `lang = "en-US"` **não garante** voz em inglês: numa máquina que só tem
 * vozes em português — o caso comum de um Windows brasileiro — o navegador aceita a
 * marcação de idioma e lê o inglês com a voz portuguesa. Num app de idioma isso é pior
 * do que não ter áudio: ensina a pronúncia errada com a autoridade de um botão oficial.
 */
function vozEmIngles(): SpeechSynthesisVoice | null {
  const vozes = janela()?.speechSynthesis?.getVoices() ?? [];
  return vozes.find((voz) => voz.lang.toLowerCase().startsWith("en")) ?? null;
}

/**
 * Só há síntese utilizável quando existe uma voz em inglês de verdade.
 *
 * <p>As vozes carregam de forma assíncrona no Chrome: na primeira chamada a lista costuma
 * vir vazia. Quem consulta isto deve reagir a {@link aoCarregarVozes}.
 */
export function sinteseDisponivel(): boolean {
  return janela()?.speechSynthesis !== undefined && vozEmIngles() !== null;
}

/**
 * Avisa quando a lista de vozes muda.
 *
 * @returns função para cancelar a inscrição
 */
export function aoCarregarVozes(callback: () => void): () => void {
  const sintetizador = janela()?.speechSynthesis;
  if (!sintetizador) {
    return () => undefined;
  }
  sintetizador.addEventListener("voiceschanged", callback);
  return () => sintetizador.removeEventListener("voiceschanged", callback);
}

/**
 * Fala um texto em inglês.
 *
 * Cancela o que estiver falando antes de começar: dois botões clicados em sequência
 * enfileirariam as falas, e a segunda só começaria depois da primeira terminar — o que
 * parece travamento para quem clicou.
 */
export function falar(texto: string) {
  const sintetizador = janela()?.speechSynthesis;
  const voz = vozEmIngles();
  if (!sintetizador || !voz || !texto.trim()) {
    return;
  }

  sintetizador.cancel();

  const fala = new SpeechSynthesisUtterance(texto);
  // A voz é escolhida explicitamente, e não deixada para o navegador resolver pelo lang.
  fala.voice = voz;
  fala.lang = voz.lang || IDIOMA;
  // Um pouco abaixo do normal: a velocidade padrão atropela quem está aprendendo.
  fala.rate = 0.92;
  sintetizador.speak(fala);
}

export function pararDeFalar() {
  janela()?.speechSynthesis?.cancel();
}

export function reconhecimentoDisponivel(): boolean {
  const w = janela();
  return Boolean(w?.SpeechRecognition ?? w?.webkitSpeechRecognition);
}

/**
 * Começa a ouvir e devolve a função que interrompe.
 *
 * @param aoTranscrever recebe o que foi entendido. Pode ser chamado mais de uma vez:
 *                      o resultado parcial vai aparecendo enquanto a pessoa fala.
 * @param aoTerminar chamado quando o reconhecimento para, por qualquer motivo — inclusive
 *                   por silêncio. Sem isso a interface ficaria presa em "ouvindo".
 */
export function ouvir(
  aoTranscrever: (texto: string) => void,
  aoTerminar: (erro?: string) => void,
): () => void {
  const w = janela();
  const Construtor = w?.SpeechRecognition ?? w?.webkitSpeechRecognition;
  if (!Construtor) {
    aoTerminar("Este navegador não reconhece fala.");
    return () => undefined;
  }

  const reconhecimento = new Construtor();
  reconhecimento.lang = IDIOMA;
  reconhecimento.continuous = false;
  // Resultado parcial: a pessoa vê o texto aparecendo e sabe que está sendo ouvida.
  reconhecimento.interimResults = true;

  reconhecimento.onresult = (evento) => {
    let texto = "";
    for (let i = 0; i < evento.results.length; i++) {
      texto += evento.results[i][0].transcript;
    }
    aoTranscrever(texto.trim());
  };

  reconhecimento.onerror = (evento) => {
    const motivo = evento.error;
    aoTerminar(
      motivo === "not-allowed"
        ? "O navegador bloqueou o microfone. Libere o acesso e tente de novo."
        : "Não foi possível ouvir. Tente de novo ou escreva a resposta.",
    );
  };

  reconhecimento.onend = () => aoTerminar();

  try {
    reconhecimento.start();
  } catch {
    // start() lança se já estiver ouvindo — não é erro que o aluno precise ver.
    aoTerminar();
  }

  return () => reconhecimento.abort();
}
