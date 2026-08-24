-- Indice que sustenta o limite de cadastro e o de recusas por origem.
--
-- As duas contagens filtram pelo mesmo formato: uma origem, um tipo de evento e uma
-- janela de tempo. Sem indice, cada cadastro faria varredura completa numa tabela que
-- so cresce — o custo da protecao aumentaria junto com a auditoria que ela alimenta,
-- e a protecao acabaria removida por lentidao.
--
-- A ordem das colunas segue a seletividade: origem restringe muito, tipo restringe
-- pouco, e ocorrido_em fecha a janela.
CREATE INDEX idx_evento_por_origem
    ON evento_de_autenticacao (origem, tipo, ocorrido_em DESC);
