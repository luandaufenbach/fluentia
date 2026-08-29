import type { ErroDetectado } from "../tipos";
import "./CartaoDaResposta.css";

/**
 * Mostra lado a lado o que o aluno escreveu e a mesma frase com as correções.
 *
 * <p>Existe porque o campo de resposta some quando a correção chega: dá para ler o
 * veredito sem ter mais na frente o que se escreveu, e comparar de cabeça é justamente
 * a parte que ensina. Aqui a comparação fica pronta.
 *
 * <p>A frase corrigida é montada no navegador, trocando cada trecho errado pela
 * correção que o avaliador apontou — não existe uma "frase corrigida" vinda do
 * servidor, e a resposta de referência nunca chega ao frontend de propósito.
 */

interface Props {
  respostaDoAluno: string;
  erros: ErroDetectado[];
  acertou: boolean;
}

interface Pedaco {
  texto: string;
  marcado: boolean;
}

/** Pares utilizáveis: sem um dos lados não há o que trocar nem o que marcar. */
function trocasUtilizaveis(erros: ErroDetectado[]) {
  return erros
    .filter((erro) => erro.trechoErrado?.trim() && erro.correcao?.trim())
    .map((erro) => ({
      errado: erro.trechoErrado as string,
      certo: erro.correcao as string,
    }));
}

/**
 * Quebra o texto marcando os trechos encontrados.
 *
 * Os trechos são procurados do mais longo para o mais curto: quando um é pedaço de
 * outro ("the dog" dentro de "the dog was brown"), casar o curto primeiro partiria o
 * longo ao meio e a marcação sairia picada.
 */
function segmentar(texto: string, trechos: string[]): Pedaco[] {
  const alvos = [...trechos].filter(Boolean).sort((a, b) => b.length - a.length);
  const pedacos: Pedaco[] = [];
  let restante = texto;

  while (restante.length > 0) {
    let posicao = -1;
    let encontrado = "";

    for (const alvo of alvos) {
      const onde = restante.toLowerCase().indexOf(alvo.toLowerCase());
      if (onde !== -1 && (posicao === -1 || onde < posicao)) {
        posicao = onde;
        encontrado = alvo;
      }
    }

    if (posicao === -1) {
      pedacos.push({ texto: restante, marcado: false });
      break;
    }

    if (posicao > 0) {
      pedacos.push({ texto: restante.slice(0, posicao), marcado: false });
    }
    // Recorta pelo tamanho do alvo, mas preserva o texto original: a busca ignora
    // maiúsculas, e devolver o alvo no lugar mudaria o que o aluno escreveu.
    pedacos.push({
      texto: restante.slice(posicao, posicao + encontrado.length),
      marcado: true,
    });
    restante = restante.slice(posicao + encontrado.length);
  }

  return pedacos;
}

/** Aplica as trocas. Devolve nulo quando nenhuma casou: melhor não mostrar do que mentir. */
function corrigir(texto: string, trocas: { errado: string; certo: string }[]) {
  let resultado = texto;
  let trocou = false;

  for (const troca of [...trocas].sort((a, b) => b.errado.length - a.errado.length)) {
    const posicao = resultado.toLowerCase().indexOf(troca.errado.toLowerCase());
    if (posicao === -1) {
      continue;
    }
    resultado =
      resultado.slice(0, posicao) +
      troca.certo +
      resultado.slice(posicao + troca.errado.length);
    trocou = true;
  }

  return trocou ? resultado : null;
}

function Frase({ pedacos, estilo }: { pedacos: Pedaco[]; estilo: "errado" | "certo" }) {
  return (
    <p className="cartao-da-resposta__frase" lang="en">
      {pedacos.map((pedaco, indice) =>
        pedaco.marcado ? (
          <mark key={indice} className={`cartao-da-resposta__marca--${estilo}`}>
            {pedaco.texto}
          </mark>
        ) : (
          <span key={indice}>{pedaco.texto}</span>
        ),
      )}
    </p>
  );
}

export function CartaoDaResposta({ respostaDoAluno, erros, acertou }: Props) {
  const trocas = trocasUtilizaveis(erros);
  const corrigida = acertou ? null : corrigir(respostaDoAluno, trocas);

  return (
    <section className="cartao-da-resposta">
      <div className="cartao-da-resposta__bloco">
        <span className="cartao-da-resposta__rotulo">
          {corrigida ? "Você escreveu" : "Sua resposta"}
        </span>
        <Frase
          pedacos={segmentar(
            respostaDoAluno,
            trocas.map((troca) => troca.errado),
          )}
          estilo="errado"
        />
      </div>

      {corrigida && (
        <div className="cartao-da-resposta__bloco">
          <span className="cartao-da-resposta__rotulo cartao-da-resposta__rotulo--certo">
            Com a correção
          </span>
          <Frase
            pedacos={segmentar(
              corrigida,
              trocas.map((troca) => troca.certo),
            )}
            estilo="certo"
          />
        </div>
      )}
    </section>
  );
}
