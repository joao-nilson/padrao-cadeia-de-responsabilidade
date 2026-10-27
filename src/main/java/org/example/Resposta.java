package org.example;

public class Resposta {

    private final int status;
    private final String mensagem;

    private Resposta(int status, String mensagem) {
        this.status = status;
        this.mensagem = mensagem;
    }

    public static Resposta ok(String mensagem) {
        return new Resposta(200, mensagem);
    }

    public static Resposta requisicaoInvalida(String mensagem) {
        return new Resposta(400, mensagem);
    }

    public static Resposta naoAutenticado(String mensagem) {
        return new Resposta(401, mensagem);
    }

    public static Resposta acessoNegado(String mensagem) {
        return new Resposta(403, mensagem);
    }

    public int getStatus() {
        return status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public boolean isSucesso() {
        return status >= 200 && status < 300;
    }

    @Override
    public String toString() {
        return status + " - " + mensagem;
    }
}
