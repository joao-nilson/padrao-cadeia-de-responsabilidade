package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class FiltroValidacao extends Filtro {

    private static final Set<String> METODOS_VALIDOS =
            new HashSet<>(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE"));
    private static final Set<String> METODOS_COM_CORPO =
            new HashSet<>(Arrays.asList("POST", "PUT", "PATCH"));

    private final List<String> camposObrigatorios;

    public FiltroValidacao(String... camposObrigatorios) {
        this.camposObrigatorios = Arrays.asList(camposObrigatorios);
    }

    @Override
    protected Optional<Resposta> aplicar(Requisicao requisicao) {
        String metodo = requisicao.getMetodo();
        if (metodo == null || !METODOS_VALIDOS.contains(metodo)) {
            return Optional.of(Resposta.requisicaoInvalida("Método HTTP inválido: " + metodo));
        }

        String caminho = requisicao.getCaminho();
        if (caminho == null || !caminho.startsWith("/")) {
            return Optional.of(Resposta.requisicaoInvalida("Caminho inválido: " + caminho));
        }

        if (METODOS_COM_CORPO.contains(metodo)) {
            List<String> ausentes = new ArrayList<>();
            for (String campo : camposObrigatorios) {
                String valor = requisicao.getParametro(campo);
                if (valor == null || valor.isBlank()) {
                    ausentes.add(campo);
                }
            }
            if (!ausentes.isEmpty()) {
                return Optional.of(Resposta.requisicaoInvalida(
                        "Campos obrigatórios ausentes: " + String.join(", ", ausentes)));
            }
        }
        return Optional.empty();
    }
}
