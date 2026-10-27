package org.example;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class Usuario {

    private final String nome;
    private final Set<Papel> papeis;

    public Usuario(String nome, Papel... papeis) {
        this.nome = nome;
        Set<Papel> conjunto = EnumSet.noneOf(Papel.class);
        conjunto.addAll(Arrays.asList(papeis));
        this.papeis = Collections.unmodifiableSet(conjunto);
    }

    public String getNome() {
        return nome;
    }

    public Set<Papel> getPapeis() {
        return papeis;
    }

    public boolean possuiPapel(Papel papel) {
        return papeis.contains(papel);
    }
}
