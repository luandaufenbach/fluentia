import { useCallback, useEffect, useState } from "react";
import { CampoDeResposta } from "../componentes/CampoDeResposta";
import { PainelDeCorrecao } from "../componentes/PainelDeCorrecao";
import { api } from "../servicos/api";
import type { Correcao, Desafio } from "../tipos";
import "./TelaDeDesafio.css";

interface Props {
  /** Avisa o resto do app que a nota mudou, para a lista de modulos recarregar. */
  onNotaAtualizada: () => void;
}

/**
 * Tela do desafio da vez. A correcao so aparece depois do envio, resumida,
 * nunca no meio da escrita da resposta.
 */
export function TelaDeDesafio({ onNotaAtualizada }: Props) {
  const [desafio, setDesafio] = useState<Desafio | null>(null);
  const [resposta, setResposta] = useState("");
  const [correcao, setCorrecao] = useState<Correcao | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const carregarDesafio = useCallback(async () => {
    setCarregando(true);
    setErro(null);
    setCorrecao(null);
    setResposta("");
    try {
      setDesafio(await api.proximoDesafio());
    } catch (falha) {
      setErro(falha instanceof Error ? falha.message : "Nao foi possivel carregar o desafio.");
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    void carregarDesafio();
  }, [carregarDesafio]);

  async function enviarResposta() {
    if (!desafio || resposta.trim().length === 0) {
      return;
    }
    setEnviando(true);
    setErro(null);
    try {
      const resultado = await api.responderDesafio(desafio.id, resposta);
      setCorrecao(resultado);
      onNotaAtualizada();
    } catch (falha) {
      setErro(falha instanceof Error ? falha.message : "Nao foi possivel enviar a resposta.");
    } finally {
      setEnviando(false);
    }
  }

  if (carregando) {
    return <p className="tela-de-desafio__estado">Escolhendo o proximo desafio...</p>;
  }

  if (erro && !desafio) {
    return (
      <div className="tela-de-desafio__estado">
        <p>{erro}</p>
        <button type="button" className="botao-secundario" onClick={() => void carregarDesafio()}>
          Tentar de novo
        </button>
      </div>
    );
  }

  if (!desafio) {
    return <p className="tela-de-desafio__estado">Nenhum desafio disponivel.</p>;
  }

  return (
    <div className="tela-de-desafio">
      <div className="tela-de-desafio__contexto">
        <span className="tela-de-desafio__etiqueta">{desafio.moduloNome}</span>
        <span className="tela-de-desafio__separador" aria-hidden="true">
          ·
        </span>
        <span className="tela-de-desafio__tema">{desafio.temaNome}</span>
      </div>

      <article className="tela-de-desafio__cartao">
        {desafio.contextoDaCena && (
          <p className="tela-de-desafio__cena">{desafio.contextoDaCena}</p>
        )}
        <h1 className="tela-de-desafio__enunciado">{desafio.enunciado}</h1>
      </article>

      {!correcao && (
        <div className="tela-de-desafio__resposta">
          <CampoDeResposta
            formato={desafio.formato}
            valor={resposta}
            onChange={setResposta}
            desabilitado={enviando}
          />

          {erro && <p className="tela-de-desafio__erro">{erro}</p>}

          <button
            type="button"
            className="botao-primario"
            onClick={() => void enviarResposta()}
            disabled={enviando || resposta.trim().length === 0}
          >
            {enviando ? "Avaliando..." : "Enviar resposta"}
          </button>
        </div>
      )}

      {correcao && (
        <PainelDeCorrecao
          correcao={correcao}
          onProximoDesafio={() => void carregarDesafio()}
          carregandoProximo={carregando}
        />
      )}

      <details className="tela-de-desafio__motivo">
        <summary>Por que este desafio agora?</summary>
        <p>{desafio.motivoDaEscolha}</p>
      </details>
    </div>
  );
}
