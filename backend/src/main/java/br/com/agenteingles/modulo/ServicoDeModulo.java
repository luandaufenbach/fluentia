package br.com.agenteingles.modulo;

import br.com.agenteingles.comum.RecursoNaoEncontradoException;
import br.com.agenteingles.nota.NotaDoModulo;
import br.com.agenteingles.nota.NotaDoModuloRepositorio;
import br.com.agenteingles.nota.ServicoDeNota;
import br.com.agenteingles.usuario.Usuario;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Monta a situacao de cada modulo para um usuario: nota com decaimento, faixa de cor
 * e liberacao por pre-requisito.
 */
@Service
public class ServicoDeModulo {

    /**
     * Nota minima que um pre-requisito precisa ter para liberar o modulo seguinte.
     * Fica no limite do amarelo: o usuario avanca sem ter que zerar o conceito anterior,
     * mas nao destrava um nivel novo carregando um conceito em vermelho.
     */
    public static final BigDecimal NOTA_QUE_LIBERA_O_PROXIMO = new BigDecimal("6");

    private final ModuloRepositorio moduloRepositorio;
    private final NotaDoModuloRepositorio notaRepositorio;
    private final ServicoDeNota servicoDeNota;

    public ServicoDeModulo(ModuloRepositorio moduloRepositorio,
                           NotaDoModuloRepositorio notaRepositorio,
                           ServicoDeNota servicoDeNota) {
        this.moduloRepositorio = moduloRepositorio;
        this.notaRepositorio = notaRepositorio;
        this.servicoDeNota = servicoDeNota;
    }

    /** Situacao de todos os modulos do curriculo, na ordem de progressao. */
    @Transactional(readOnly = true)
    public List<SituacaoDoModulo> situacaoDeTodosOsModulos(Usuario usuario) {
        LocalDateTime agora = LocalDateTime.now();
        List<Modulo> modulos = moduloRepositorio.listarTodosComPreRequisitos();

        Map<Long, NotaDoModulo> notasPorModulo = new HashMap<>();
        for (NotaDoModulo nota : notaRepositorio.listarPorUsuario(usuario.getId())) {
            notasPorModulo.put(nota.getModulo().getId(), nota);
        }

        // Primeiro as notas ja descontadas, porque a liberacao consulta a nota do pre-requisito.
        Map<Long, BigDecimal> notaComDecaimentoPorModulo = new HashMap<>();
        for (Modulo modulo : modulos) {
            NotaDoModulo notaGravada = notasPorModulo.get(modulo.getId());
            BigDecimal nota = notaGravada == null ? null : servicoDeNota.aplicarDecaimento(
                    notaGravada.getNota(), notaGravada.getDataDaUltimaPratica(), agora);
            notaComDecaimentoPorModulo.put(modulo.getId(), nota);
        }

        List<SituacaoDoModulo> situacoes = new ArrayList<>();
        for (Modulo modulo : modulos) {
            BigDecimal nota = notaComDecaimentoPorModulo.get(modulo.getId());
            NotaDoModulo notaGravada = notasPorModulo.get(modulo.getId());

            List<String> pendentes = new ArrayList<>();
            for (Modulo preRequisito : modulo.getPreRequisitos()) {
                BigDecimal notaDoPreRequisito = notaComDecaimentoPorModulo.get(preRequisito.getId());
                if (notaDoPreRequisito == null
                        || notaDoPreRequisito.compareTo(NOTA_QUE_LIBERA_O_PROXIMO) < 0) {
                    pendentes.add(preRequisito.getNome());
                }
            }

            situacoes.add(new SituacaoDoModulo(
                    modulo,
                    nota,
                    servicoDeNota.faixaDa(nota),
                    pendentes.isEmpty(),
                    pendentes,
                    notaGravada == null ? null : notaGravada.getDataDaUltimaPratica(),
                    notaGravada == null ? 0 : notaGravada.getQuantidadeDePraticas()));
        }

        situacoes.sort(Comparator.comparing(situacao -> situacao.modulo().getOrdem()));
        return situacoes;
    }

    @Transactional(readOnly = true)
    public Modulo buscarPorCodigo(String codigo) {
        return moduloRepositorio.buscarPorCodigo(codigo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Modulo nao encontrado: " + codigo));
    }
}
