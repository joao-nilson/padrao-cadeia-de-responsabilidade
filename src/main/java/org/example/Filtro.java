package org.example;

import java.util.Optional;

public abstract class Filtro {

    private Filtro proximo;

    public Filtro getProximo() {
        return proximo;
    }

    public void setProximo(Filtro proximo) {
        this.proximo = proximo;
    }

    public Filtro encadear(Filtro proximo) {
        setProximo(proximo);
        return proximo;
    }

    public final Resposta processar(Requisicao requisicao) {
        Optional<Resposta> rejeicao = aplicar(requisicao);
        if (rejeicao.isPresent()) {
            return rejeicao.get();
        }
        if (proximo != null) {
            return proximo.processar(requisicao);
        }
        return Resposta.ok("Requisição processada com sucesso");
    }

    protected abstract Optional<Resposta> aplicar(Requisicao requisicao);
}
