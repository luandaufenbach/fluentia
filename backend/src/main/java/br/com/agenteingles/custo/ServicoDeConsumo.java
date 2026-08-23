package br.com.agenteingles.custo;

import br.com.agenteingles.desafio.DesafioRepositorio;
import br.com.agenteingles.desafio.StatusDoDesafio;
import br.com.agenteingles.usuario.Usuario;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Monta o extrato de consumo da conta. */
@Service
public class ServicoDeConsumo {

    private static final int DIAS_DA_JANELA = 7;

    /** Casas suficientes para o custo por desafio, que fica na ordem de milesimos de dolar. */
    private static final int CASAS_DO_CUSTO_UNITARIO = 5;

    private final ConsumoDeApiRepositorio consumoRepositorio;
    private final DesafioRepositorio desafioRepositorio;

    public ServicoDeConsumo(ConsumoDeApiRepositorio consumoRepositorio,
                            DesafioRepositorio desafioRepositorio) {
        this.consumoRepositorio = consumoRepositorio;
        this.desafioRepositorio = desafioRepositorio;
    }

    @Transactional(readOnly = true)
    public ResumoDeConsumo doUsuario(Usuario usuario) {
        Long usuarioId = usuario.getId();
        LocalDateTime inicioDeHoje = LocalDate.now().atStartOfDay();

        TotalDeConsumo hoje = consumoRepositorio.somarDoUsuarioDesde(usuarioId, inicioDeHoje);
        TotalDeConsumo semana = consumoRepositorio.somarDoUsuarioDesde(
                usuarioId, inicioDeHoje.minusDays(DIAS_DA_JANELA - 1L));
        TotalDeConsumo total = consumoRepositorio.somarDoUsuario(usuarioId);

        Map<StatusDoDesafio, Long> porStatus = contarDesafiosPorStatus(usuarioId);
        long respondidos = porStatus.getOrDefault(StatusDoDesafio.AVALIADO, 0L);
        long gerados = porStatus.values().stream().mapToLong(Long::longValue).sum();

        return new ResumoDeConsumo(
                hoje,
                semana,
                total,
                consumoRepositorio.agruparPorTipo(usuarioId),
                custoPorDesafio(total.custoUsd(), respondidos),
                gerados,
                respondidos,
                porStatus.getOrDefault(StatusDoDesafio.NA_FILA, 0L),
                consumoRepositorio.modelosSemPreco(usuarioId));
    }

    private Map<StatusDoDesafio, Long> contarDesafiosPorStatus(Long usuarioId) {
        Map<StatusDoDesafio, Long> contagem = new EnumMap<>(StatusDoDesafio.class);
        for (Object[] linha : desafioRepositorio.contarPorStatus(usuarioId)) {
            contagem.put((StatusDoDesafio) linha[0], (Long) linha[1]);
        }
        return contagem;
    }

    /** Nulo enquanto nao houver desafio respondido: dividir por zero nao vira "custo zero". */
    private BigDecimal custoPorDesafio(BigDecimal custoTotal, long desafiosRespondidos) {
        if (custoTotal == null || desafiosRespondidos == 0) {
            return null;
        }
        return custoTotal.divide(BigDecimal.valueOf(desafiosRespondidos),
                CASAS_DO_CUSTO_UNITARIO, RoundingMode.HALF_UP);
    }
}
