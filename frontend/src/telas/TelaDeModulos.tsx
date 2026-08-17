import { useEffect, useState } from "react";
import { ListaDeModulos } from "../componentes/ListaDeModulos";
import { api } from "../servicos/api";
import type { NivelComModulos, Sugestao } from "../tipos";
import "./TelaDeModulos.css";

interface Props {
  /** Muda quando uma nota é atualizada, para a lista recarregar. */
  versao: number;
  onEstudarModulo: (codigoDoModulo: string) => void;
  onIrParaDesafio: () => void;
}

/** Trilha completa por nível CEFR, com a sugestão do orquestrador no topo. */
export function TelaDeModulos({ versao, onEstudarModulo, onIrParaDesafio }: Props) {
  const [niveis, setNiveis] = useState<NivelComModulos[]>([]);
  const [sugestao, setSugestao] = useState<Sugestao | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    let cancelado = false;

    async function carregar() {
      setCarregando(true);
      setErro(null);
      try {
        const [modulos, proximaPratica] = await Promise.all([
          api.listarModulos(),
          api.buscarSugestao(),
        ]);
        if (!cancelado) {
          setNiveis(modulos);
          setSugestao(proximaPratica);
        }
      } catch (falha) {
        if (!cancelado) {
          setErro(falha instanceof Error ? falha.message : "Não foi possível carregar a trilha.");
        }
      } finally {
        if (!cancelado) {
          setCarregando(false);
        }
      }
    }

    void carregar();
    return () => {
      cancelado = true;
    };
  }, [versao]);

  if (carregando) {
    return <p className="tela-de-modulos__estado">Carregando a trilha...</p>;
  }

  if (erro) {
    return <p className="tela-de-modulos__estado">{erro}</p>;
  }

  return (
    <div className="tela-de-modulos">
      {sugestao && (
        <section className="tela-de-modulos__sugestao">
          <div className="tela-de-modulos__sugestao-texto">
            <span className="tela-de-modulos__sugestao-rotulo">Sugestão de agora</span>
            <h2>{sugestao.moduloNome}</h2>
            <p>{sugestao.motivo}</p>
          </div>

          {/*
           * Estudar vem primeiro por ser o caminho que ensina; quem já sabe o
           * conceito pula direto para o exercício pelo botão secundário.
           */}
          <div className="tela-de-modulos__sugestao-acoes">
            <button
              type="button"
              className="botao-primario"
              onClick={() => onEstudarModulo(sugestao.moduloCodigo)}
            >
              Estudar o conteúdo
            </button>
            <button type="button" className="botao-secundario" onClick={onIrParaDesafio}>
              Ir direto ao desafio
            </button>
          </div>
        </section>
      )}

      <ListaDeModulos
        niveis={niveis}
        moduloEmDestaque={sugestao?.moduloCodigo}
        onEstudarModulo={onEstudarModulo}
      />
    </div>
  );
}
