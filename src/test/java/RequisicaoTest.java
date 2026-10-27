import org.example.Requisicao;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequisicaoTest {

    @Test
    void deveNormalizarMetodoParaMaiusculas() {
        assertEquals("POST", new Requisicao(" post ", "/artigos").getMetodo());
    }

    @Test
    void deveBuscarCabecalhoIgnorandoMaiusculas() {
        Requisicao requisicao = new Requisicao("GET", "/").comCabecalho("Content-Type", "application/json");

        assertEquals("application/json", requisicao.getCabecalho("content-type"));
        assertNull(requisicao.getCabecalho("Authorization"));
    }

    @Test
    void deveArmazenarParametros() {
        Requisicao requisicao = new Requisicao("POST", "/").comParametro("titulo", "Teste");

        assertEquals("Teste", requisicao.getParametro("titulo"));
        assertNull(requisicao.getParametro("conteudo"));
    }

    @Test
    void deveIniciarSemUsuarioAutenticado() {
        assertNull(new Requisicao("GET", "/").getUsuario());
    }
}
