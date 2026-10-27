import org.example.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CadeiaFiltrosTest {

    FiltroAutenticacao autenticacao;
    FiltroPermissao permissao;
    FiltroValidacao validacao;

    @BeforeEach
    void setUp() {
        Map<String, Usuario> tokens = new HashMap<>();
        tokens.put("token-admin", new Usuario("Ana", Papel.ADMINISTRADOR, Papel.EDITOR, Papel.LEITOR));
        tokens.put("token-editor", new Usuario("Maria", Papel.EDITOR, Papel.LEITOR));
        tokens.put("token-leitor", new Usuario("João", Papel.LEITOR));

        autenticacao = new FiltroAutenticacao(tokens);
        permissao = new FiltroPermissao()
                .exigir("/admin", Papel.ADMINISTRADOR)
                .exigir("POST", "/artigos", Papel.EDITOR);
        validacao = new FiltroValidacao("titulo", "conteudo");

        autenticacao.setProximo(permissao);
        permissao.setProximo(validacao);
    }

    private Requisicao requisicao(String token, String metodo, String caminho) {
        Requisicao requisicao = new Requisicao(metodo, caminho);
        if (token != null) {
            requisicao.comCabecalho("Authorization", "Bearer " + token);
        }
        return requisicao;
    }

    // ---------- cenários de ponta a ponta ----------

    @Test
    void deveProcessarRequisicaoQuePassaPorTodosOsFiltros() {
        Requisicao requisicao = requisicao("token-editor", "POST", "/artigos")
                .comParametro("titulo", "Padrões de projeto")
                .comParametro("conteudo", "Chain of Responsibility");

        Resposta resposta = autenticacao.processar(requisicao);

        assertEquals(200, resposta.getStatus());
        assertEquals("Requisição processada com sucesso", resposta.getMensagem());
        assertEquals("Maria", requisicao.getUsuario().getNome());
    }

    @Test
    void deveProcessarLeituraPorUsuarioSemPermissaoDeEscrita() {
        Resposta resposta = autenticacao.processar(requisicao("token-leitor", "GET", "/artigos"));

        assertEquals(200, resposta.getStatus());
    }

    @Test
    void deveRetornarNaoAutenticadoSemToken() {
        Resposta resposta = autenticacao.processar(requisicao(null, "GET", "/artigos"));

        assertEquals(401, resposta.getStatus());
    }

    @Test
    void deveRetornarNaoAutenticadoParaTokenInvalido() {
        Resposta resposta = autenticacao.processar(requisicao("token-falso", "GET", "/artigos"));

        assertEquals(401, resposta.getStatus());
        assertEquals("Token inválido ou expirado", resposta.getMensagem());
    }

    @Test
    void deveRetornarAcessoNegadoParaLeitorEmAreaAdministrativa() {
        Resposta resposta = autenticacao.processar(requisicao("token-leitor", "GET", "/admin/painel"));

        assertEquals(403, resposta.getStatus());
    }

    @Test
    void devePermitirAdministradorEmAreaAdministrativa() {
        Resposta resposta = autenticacao.processar(requisicao("token-admin", "GET", "/admin/painel"));

        assertEquals(200, resposta.getStatus());
    }

    @Test
    void deveRetornarRequisicaoInvalidaQuandoFaltamCamposObrigatorios() {
        Requisicao requisicao = requisicao("token-editor", "POST", "/artigos")
                .comParametro("titulo", "Padrões de projeto");

        Resposta resposta = autenticacao.processar(requisicao);

        assertEquals(400, resposta.getStatus());
        assertEquals("Campos obrigatórios ausentes: conteudo", resposta.getMensagem());
    }

    // ---------- ordem de avaliação ----------

    @Test
    void autenticacaoDeveTerPrecedenciaSobrePermissaoEValidacao() {
        // sem token, em área restrita e com corpo vazio: só a autenticação deve responder
        Resposta resposta = autenticacao.processar(requisicao(null, "POST", "/admin/artigos"));

        assertEquals(401, resposta.getStatus());
    }

    @Test
    void permissaoDeveTerPrecedenciaSobreValidacao() {
        // leitor não pode criar artigos e o corpo está vazio: deve ser negado antes de validar
        Resposta resposta = autenticacao.processar(requisicao("token-leitor", "POST", "/artigos"));

        assertEquals(403, resposta.getStatus());
    }

    // ---------- comportamento da cadeia ----------

    @Test
    void deveEncadearFiltrosDeFormaFluente() {
        FiltroAutenticacao primeiro = new FiltroAutenticacao(new HashMap<>());
        FiltroPermissao segundo = new FiltroPermissao();
        FiltroValidacao terceiro = new FiltroValidacao();

        Filtro ultimo = primeiro.encadear(segundo).encadear(terceiro);

        assertSame(segundo, primeiro.getProximo());
        assertSame(terceiro, segundo.getProximo());
        assertSame(terceiro, ultimo);
        assertNull(terceiro.getProximo());
    }

    @Test
    void deveExecutarFiltrosNaOrdemEncadeada() {
        List<String> registro = new ArrayList<>();
        FiltroSimulado a = new FiltroSimulado("A", registro, null);
        FiltroSimulado b = new FiltroSimulado("B", registro, null);
        FiltroSimulado c = new FiltroSimulado("C", registro, null);
        a.encadear(b).encadear(c);

        Resposta resposta = a.processar(new Requisicao("GET", "/"));

        assertTrue(resposta.isSucesso());
        assertEquals(Arrays.asList("A", "B", "C"), registro);
    }

    @Test
    void deveInterromperACadeiaNoFiltroQueRejeita() {
        List<String> registro = new ArrayList<>();
        FiltroSimulado a = new FiltroSimulado("A", registro, null);
        FiltroSimulado b = new FiltroSimulado("B", registro, Resposta.acessoNegado("bloqueado por B"));
        FiltroSimulado c = new FiltroSimulado("C", registro, null);
        a.encadear(b).encadear(c);

        Resposta resposta = a.processar(new Requisicao("GET", "/"));

        assertEquals(403, resposta.getStatus());
        assertEquals("bloqueado por B", resposta.getMensagem());
        assertEquals(Arrays.asList("A", "B"), registro);
    }

    @Test
    void naoDeveExecutarFiltroSeguinteQuandoAutenticacaoRejeita() {
        List<String> registro = new ArrayList<>();
        permissao.encadear(validacao).encadear(new FiltroSimulado("final", registro, null));

        autenticacao.processar(requisicao(null, "GET", "/artigos"));
        assertTrue(registro.isEmpty());

        autenticacao.processar(requisicao("token-leitor", "GET", "/artigos"));
        assertEquals(Arrays.asList("final"), registro);
    }

    @Test
    void deveRetornarSucessoQuandoCadeiaTemApenasUmFiltro() {
        FiltroSimulado unico = new FiltroSimulado("unico", new ArrayList<>(), null);

        assertEquals(200, unico.processar(new Requisicao("GET", "/")).getStatus());
    }

    @Test
    void devePermitirIncluirNovoFiltroNaCadeiaSemAlterarOsExistentes() {
        // novo filtro (ex.: bloqueio de IP) inserido entre autenticação e permissão
        FiltroSimulado bloqueioIp = new FiltroSimulado("ip", new ArrayList<>(), Resposta.acessoNegado("IP bloqueado"));
        autenticacao.setProximo(bloqueioIp);
        bloqueioIp.setProximo(permissao);

        Resposta resposta = autenticacao.processar(requisicao("token-admin", "GET", "/admin"));

        assertEquals(403, resposta.getStatus());
        assertEquals("IP bloqueado", resposta.getMensagem());
    }

    /**
     * Filtro de teste: registra a sua execução e, se configurado, rejeita a requisição.
     */
    static class FiltroSimulado extends Filtro {

        private final String nome;
        private final List<String> registro;
        private final Resposta rejeicao;

        FiltroSimulado(String nome, List<String> registro, Resposta rejeicao) {
            this.nome = nome;
            this.registro = registro;
            this.rejeicao = rejeicao;
        }

        @Override
        protected Optional<Resposta> aplicar(Requisicao requisicao) {
            registro.add(nome);
            return Optional.ofNullable(rejeicao);
        }
    }
}
