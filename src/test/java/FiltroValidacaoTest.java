import org.example.FiltroValidacao;
import org.example.Requisicao;
import org.example.Resposta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FiltroValidacaoTest {

    FiltroValidacao filtro;

    @BeforeEach
    void setUp() {
        filtro = new FiltroValidacao("titulo", "conteudo");
    }

    @Test
    void deveAprovarRequisicaoGetValida() {
        assertTrue(filtro.processar(new Requisicao("GET", "/artigos")).isSucesso());
    }

    @Test
    void deveAprovarRequisicaoPostComTodosOsCamposObrigatorios() {
        Requisicao requisicao = new Requisicao("POST", "/artigos")
                .comParametro("titulo", "Padrões de projeto")
                .comParametro("conteudo", "Chain of Responsibility");

        assertTrue(filtro.processar(requisicao).isSucesso());
    }

    @Test
    void deveRetornarRequisicaoInvalidaListandoCamposAusentes() {
        Requisicao requisicao = new Requisicao("POST", "/artigos");

        Resposta resposta = filtro.processar(requisicao);

        assertEquals(400, resposta.getStatus());
        assertEquals("Campos obrigatórios ausentes: titulo, conteudo", resposta.getMensagem());
    }

    @Test
    void deveListarApenasOsCamposQueEstaoAusentes() {
        Requisicao requisicao = new Requisicao("PUT", "/artigos/1")
                .comParametro("titulo", "Padrões de projeto");

        Resposta resposta = filtro.processar(requisicao);

        assertEquals(400, resposta.getStatus());
        assertEquals("Campos obrigatórios ausentes: conteudo", resposta.getMensagem());
    }

    @Test
    void deveConsiderarCampoEmBrancoComoAusente() {
        Requisicao requisicao = new Requisicao("PATCH", "/artigos/1")
                .comParametro("titulo", "   ")
                .comParametro("conteudo", "Texto");

        Resposta resposta = filtro.processar(requisicao);

        assertEquals(400, resposta.getStatus());
        assertEquals("Campos obrigatórios ausentes: titulo", resposta.getMensagem());
    }

    @Test
    void naoDeveExigirCamposObrigatoriosEmRequisicaoSemCorpo() {
        assertTrue(filtro.processar(new Requisicao("GET", "/artigos")).isSucesso());
        assertTrue(filtro.processar(new Requisicao("DELETE", "/artigos/1")).isSucesso());
    }

    @Test
    void deveAprovarPostQuandoNaoHaCamposObrigatoriosConfigurados() {
        FiltroValidacao semCampos = new FiltroValidacao();

        assertTrue(semCampos.processar(new Requisicao("POST", "/artigos")).isSucesso());
    }

    @Test
    void deveRetornarRequisicaoInvalidaParaMetodoDesconhecido() {
        Resposta resposta = filtro.processar(new Requisicao("TRACE", "/artigos"));

        assertEquals(400, resposta.getStatus());
        assertEquals("Método HTTP inválido: TRACE", resposta.getMensagem());
    }

    @Test
    void deveRetornarRequisicaoInvalidaParaMetodoNulo() {
        assertEquals(400, filtro.processar(new Requisicao(null, "/artigos")).getStatus());
    }

    @Test
    void deveAceitarMetodoEmMinusculas() {
        assertTrue(filtro.processar(new Requisicao("get", "/artigos")).isSucesso());
    }

    @Test
    void deveRetornarRequisicaoInvalidaParaCaminhoSemBarraInicial() {
        Resposta resposta = filtro.processar(new Requisicao("GET", "artigos"));

        assertEquals(400, resposta.getStatus());
        assertEquals("Caminho inválido: artigos", resposta.getMensagem());
    }

    @Test
    void deveRetornarRequisicaoInvalidaParaCaminhoNulo() {
        assertEquals(400, filtro.processar(new Requisicao("GET", null)).getStatus());
    }
}
