package org.example;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class Requisicao {

    private final String metodo;
    private final String caminho;
    // Nomes de cabeçalhos HTTP não diferenciam maiúsculas de minúsculas
    private final Map<String, String> cabecalhos = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, String> parametros = new HashMap<>();
    private Usuario usuario;

    public Requisicao(String metodo, String caminho) {
        this.metodo = metodo == null ? null : metodo.trim().toUpperCase(Locale.ROOT);
        this.caminho = caminho;
    }

    public Requisicao comCabecalho(String nome, String valor) {
        cabecalhos.put(nome, valor);
        return this;
    }

    public Requisicao comParametro(String nome, String valor) {
        parametros.put(nome, valor);
        return this;
    }

    public String getMetodo() {
        return metodo;
    }

    public String getCaminho() {
        return caminho;
    }

    public String getCabecalho(String nome) {
        return cabecalhos.get(nome);
    }

    public String getParametro(String nome) {
        return parametros.get(nome);
    }

    // Preenchido pelo filtro de autenticação e lido pelos filtros seguintes
    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
