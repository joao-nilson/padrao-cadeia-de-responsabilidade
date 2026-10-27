package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FiltroAutenticacao extends Filtro {

    private static final String CABECALHO = "Authorization";
    private static final String PREFIXO = "Bearer ";

    private final Map<String, Usuario> usuariosPorToken;

    public FiltroAutenticacao(Map<String, Usuario> usuariosPorToken) {
        this.usuariosPorToken = new HashMap<>(usuariosPorToken);
    }

    @Override
    protected Optional<Resposta> aplicar(Requisicao requisicao) {
        String cabecalho = requisicao.getCabecalho(CABECALHO);

        if (cabecalho == null || cabecalho.isBlank()) {
            return Optional.of(Resposta.naoAutenticado("Token de autenticação não informado"));
        }
        if (!cabecalho.startsWith(PREFIXO)) {
            return Optional.of(Resposta.naoAutenticado("Formato do token inválido"));
        }

        Usuario usuario = usuariosPorToken.get(cabecalho.substring(PREFIXO.length()).trim());
        if (usuario == null) {
            return Optional.of(Resposta.naoAutenticado("Token inválido ou expirado"));
        }

        requisicao.setUsuario(usuario);
        return Optional.empty();
    }
}
