import org.example.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FiltroAutenticacaoTest {

    Usuario ana;
    FiltroAutenticacao filtro;

    @BeforeEach
    void setUp() {
        ana = new Usuario("Ana", Papel.EDITOR);
        Map<String, Usuario> tokens = new HashMap<>();
        tokens.put("token-ana", ana);
        filtro = new FiltroAutenticacao(tokens);
    }

    @Test
    void deveAutenticarUsuarioComTokenValido() {
        Requisicao requisicao = new Requisicao("GET", "/artigos")
                .comCabecalho("Authorization", "Bearer token-ana");

        Resposta resposta = filtro.processar(requisicao);

        assertTrue(resposta.isSucesso());
        assertSame(ana, requisicao.getUsuario());
    }

    @Test
    void deveReconhecerCabecalhoIndependenteDeMaiusculas() {
        Requisicao requisicao = new Requisicao("GET", "/artigos")
                .comCabecalho("authorization", "Bearer token-ana");

        assertTrue(filtro.processar(requisicao).isSucesso());
        assertSame(ana, requisicao.getUsuario());
    }

    @Test
    void deveRetornarNaoAutenticadoSemCabecalhoAuthorization() {
        Resposta resposta = filtro.processar(new Requisicao("GET", "/artigos"));

        assertEquals(401, resposta.getStatus());
        assertEquals("Token de autenticação não informado", resposta.getMensagem());
    }

    @Test
    void deveRetornarNaoAutenticadoComCabecalhoEmBranco() {
        Requisicao requisicao = new Requisicao("GET", "/artigos")
                .comCabecalho("Authorization", "   ");

        assertEquals(401, filtro.processar(requisicao).getStatus());
    }

    @Test
    void deveRetornarNaoAutenticadoParaFormatoDeTokenInvalido() {
        Requisicao requisicao = new Requisicao("GET", "/artigos")
                .comCabecalho("Authorization", "Basic token-ana");

        Resposta resposta = filtro.processar(requisicao);

        assertEquals(401, resposta.getStatus());
        assertEquals("Formato do token inválido", resposta.getMensagem());
    }

    @Test
    void deveRetornarNaoAutenticadoParaTokenDesconhecido() {
        Requisicao requisicao = new Requisicao("GET", "/artigos")
                .comCabecalho("Authorization", "Bearer token-falso");

        Resposta resposta = filtro.processar(requisicao);

        assertEquals(401, resposta.getStatus());
        assertEquals("Token inválido ou expirado", resposta.getMensagem());
    }

    @Test
    void naoDevePreencherUsuarioQuandoAutenticacaoFalha() {
        Requisicao requisicao = new Requisicao("GET", "/artigos")
                .comCabecalho("Authorization", "Bearer token-falso");

        filtro.processar(requisicao);

        assertNull(requisicao.getUsuario());
    }

    @Test
    void naoDeveSofrerAlteracaoSeMapaOriginalDeTokensMudar() {
        Map<String, Usuario> tokens = new HashMap<>();
        FiltroAutenticacao filtroIsolado = new FiltroAutenticacao(tokens);
        tokens.put("token-novo", ana);

        Requisicao requisicao = new Requisicao("GET", "/artigos")
                .comCabecalho("Authorization", "Bearer token-novo");

        assertEquals(401, filtroIsolado.processar(requisicao).getStatus());
    }
}
