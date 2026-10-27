import org.example.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FiltroPermissaoTest {

    Usuario administrador;
    Usuario leitor;
    FiltroPermissao filtro;

    @BeforeEach
    void setUp() {
        administrador = new Usuario("Ana", Papel.ADMINISTRADOR);
        leitor = new Usuario("João", Papel.LEITOR);
        filtro = new FiltroPermissao()
                .exigir("/admin", Papel.ADMINISTRADOR)
                .exigir("DELETE", "/artigos", Papel.EDITOR);
    }

    private Requisicao requisicaoDe(Usuario usuario, String metodo, String caminho) {
        Requisicao requisicao = new Requisicao(metodo, caminho);
        requisicao.setUsuario(usuario);
        return requisicao;
    }

    @Test
    void devePermitirUsuarioComPapelExigido() {
        Resposta resposta = filtro.processar(requisicaoDe(administrador, "GET", "/admin"));

        assertTrue(resposta.isSucesso());
    }

    @Test
    void deveRetornarAcessoNegadoParaUsuarioSemPapelExigido() {
        Resposta resposta = filtro.processar(requisicaoDe(leitor, "GET", "/admin"));

        assertEquals(403, resposta.getStatus());
        assertEquals("Acesso negado: requer o papel ADMINISTRADOR", resposta.getMensagem());
    }

    @Test
    void deveRetornarNaoAutenticadoQuandoNaoHaUsuarioNaRequisicao() {
        Resposta resposta = filtro.processar(new Requisicao("GET", "/admin"));

        assertEquals(401, resposta.getStatus());
        assertEquals("Usuário não autenticado", resposta.getMensagem());
    }

    @Test
    void deveRetornarNaoAutenticadoMesmoParaRecursoSemRegra() {
        Resposta resposta = filtro.processar(new Requisicao("GET", "/publico"));

        assertEquals(401, resposta.getStatus());
    }

    @Test
    void deveLiberarRecursoSemRegraParaQualquerUsuarioAutenticado() {
        Resposta resposta = filtro.processar(requisicaoDe(leitor, "GET", "/artigos"));

        assertTrue(resposta.isSucesso());
    }

    @Test
    void deveAplicarRegraAosSubcaminhos() {
        Resposta resposta = filtro.processar(requisicaoDe(leitor, "GET", "/admin/usuarios/10"));

        assertEquals(403, resposta.getStatus());
    }

    @Test
    void naoDeveAplicarRegraACaminhoApenasParecidoComOPrefixo() {
        Resposta resposta = filtro.processar(requisicaoDe(leitor, "GET", "/administrador"));

        assertTrue(resposta.isSucesso());
    }

    @Test
    void deveAplicarRegraApenasAoMetodoInformado() {
        assertTrue(filtro.processar(requisicaoDe(leitor, "GET", "/artigos/1")).isSucesso());
        assertEquals(403, filtro.processar(requisicaoDe(leitor, "DELETE", "/artigos/1")).getStatus());
    }

    @Test
    void deveIgnorarMaiusculasNoMetodoDaRegraEDaRequisicao() {
        FiltroPermissao filtroMinusculo = new FiltroPermissao().exigir("delete", "/artigos", Papel.EDITOR);

        assertEquals(403, filtroMinusculo.processar(requisicaoDe(leitor, "Delete", "/artigos")).getStatus());
    }

    @Test
    void deveExigirTodasAsRegrasQueSeAplicamARequisicao() {
        FiltroPermissao filtroDuplo = new FiltroPermissao()
                .exigir("/artigos", Papel.LEITOR)
                .exigir("DELETE", "/artigos", Papel.EDITOR);
        Usuario editor = new Usuario("Maria", Papel.LEITOR, Papel.EDITOR);

        assertTrue(filtroDuplo.processar(requisicaoDe(editor, "DELETE", "/artigos/1")).isSucesso());
        assertEquals(403, filtroDuplo.processar(requisicaoDe(leitor, "DELETE", "/artigos/1")).getStatus());
    }

    @Test
    void naoDeveLancarExcecaoComCaminhoNulo() {
        Resposta resposta = filtro.processar(requisicaoDe(leitor, "GET", null));

        assertTrue(resposta.isSucesso());
    }
}
