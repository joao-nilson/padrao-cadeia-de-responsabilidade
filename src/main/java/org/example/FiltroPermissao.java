package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class FiltroPermissao extends Filtro {

    private final List<Regra> regras = new ArrayList<>();

    // Exige o papel para qualquer método HTTP no caminho (e seus subcaminhos)
    public FiltroPermissao exigir(String prefixoCaminho, Papel papel) {
        return exigir(null, prefixoCaminho, papel);
    }

    // Exige o papel apenas para o método HTTP informado
    public FiltroPermissao exigir(String metodo, String prefixoCaminho, Papel papel) {
        regras.add(new Regra(metodo, prefixoCaminho, papel));
        return this;
    }

    @Override
    protected Optional<Resposta> aplicar(Requisicao requisicao) {
        Usuario usuario = requisicao.getUsuario();
        if (usuario == null) {
            return Optional.of(Resposta.naoAutenticado("Usuário não autenticado"));
        }

        for (Regra regra : regras) {
            if (regra.seAplicaA(requisicao) && !usuario.possuiPapel(regra.papel)) {
                return Optional.of(Resposta.acessoNegado("Acesso negado: requer o papel " + regra.papel));
            }
        }
        return Optional.empty();
    }

    private static class Regra {

        private final String metodo;
        private final String prefixoCaminho;
        private final Papel papel;

        Regra(String metodo, String prefixoCaminho, Papel papel) {
            this.metodo = metodo == null ? null : metodo.trim().toUpperCase(Locale.ROOT);
            this.prefixoCaminho = prefixoCaminho;
            this.papel = papel;
        }

        boolean seAplicaA(Requisicao requisicao) {
            if (metodo != null && !metodo.equals(requisicao.getMetodo())) {
                return false;
            }
            String caminho = requisicao.getCaminho();
            if (caminho == null) {
                return false;
            }
            // "/admin" cobre "/admin" e "/admin/usuarios", mas não "/administrador"
            String prefixo = prefixoCaminho.endsWith("/") ? prefixoCaminho : prefixoCaminho + "/";
            return caminho.equals(prefixoCaminho) || caminho.startsWith(prefixo);
        }
    }
}
