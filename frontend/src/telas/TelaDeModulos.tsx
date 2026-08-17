import { useEffect, useState } from "react";
import { ListaDeModulos } from "../componentes/ListaDeModulos";
import { api } from "../servicos/api";
import type { NivelComModulos, Sugestao } from "../tipos";
import "./TelaDeModulos.css";

interface Props {
  /** Muda quando uma nota e atualizada, para a lista recarregar. */
  versao: number;
  onIrParaDesafio: () => void;
}

/** Curriculo completo por nivel CEFR, com a sugestao do orquestrador no topo. */
export function TelaDeModulos({ versao, onIrParaDesafio }: Props) {
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
          setErro(falha instanceof Error ? falha.message : "Nao foi possivel carregar o curriculo.");
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
    return <p className="tela-de-modulos__estado">Carregando o curriculo...</p>;
  }

  if (erro) {
    return <p className="tela-de-modulos__estado">{erro}</p>;
  }

  return (
    <div className="tela-de-modulos">
      {sugestao && (
        <section className="tela-de-modulos__sugestao">
          <div>
            <span className="tela-de-modulos__sugestao-rotulo">Sugestao de agora</span>
            <h2>{sugestao.moduloNome}</h2>
            <p>{sugestao.motivo}</p>
          </div>
          <button type="button" className="botao-primario" onClick={onIrParaDesafio}>
            Praticar
          </button>
        </section>
      )}

      <ListaDeModulos niveis={niveis} moduloEmDestaque={sugestao?.moduloCodigo} />
    </div>
  );
}
