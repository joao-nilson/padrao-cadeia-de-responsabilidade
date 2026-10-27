import org.example.Resposta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RespostaTest {

    @Test
    void deveCriarRespostasComStatusHttpCorretos() {
        assertEquals(200, Resposta.ok("ok").getStatus());
        assertEquals(400, Resposta.requisicaoInvalida("erro").getStatus());
        assertEquals(401, Resposta.naoAutenticado("erro").getStatus());
        assertEquals(403, Resposta.acessoNegado("erro").getStatus());
    }

    @Test
    void deveConsiderarSucessoApenasRespostasNaFaixa2xx() {
        assertTrue(Resposta.ok("ok").isSucesso());
        assertFalse(Resposta.requisicaoInvalida("erro").isSucesso());
        assertFalse(Resposta.naoAutenticado("erro").isSucesso());
        assertFalse(Resposta.acessoNegado("erro").isSucesso());
    }

    @Test
    void deveExibirStatusEMensagem() {
        assertEquals("403 - Acesso negado", Resposta.acessoNegado("Acesso negado").toString());
    }
}
