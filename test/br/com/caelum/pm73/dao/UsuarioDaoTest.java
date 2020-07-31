package br.com.caelum.pm73.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import br.com.caelum.pm73.dominio.Usuario;

public class UsuarioDaoTest {

	private Session session;
	private UsuarioDao usuarioDao;

	@Before
	public void antes() {
		// criamos a sessao e a passamos para o dao
		session = new CriadorDeSessao().getSession();
		usuarioDao = new UsuarioDao(session);
	}

	@After
	public void depois() {
		// fechamos a sessao
		session.close();
	}

	@Test
	public void deveEncontrarPeloNomeEEmail() {
		Usuario novoUsuario = new Usuario("João da Silva", "joao@dasilva.com.br");
		usuarioDao.salvar(novoUsuario);

		Usuario usuarioDoBanco = usuarioDao.porNomeEEmail("João da Silva", "joao@dasilva.com.br");

		assertEquals("João da Silva", usuarioDoBanco.getNome());
		assertEquals("joao@dasilva.com.br", usuarioDoBanco.getEmail());
	}

	@Test
	public void deveRetornarNuloSeNaoEncontrarUsuario() {
		Usuario usuarioDoBanco = usuarioDao.porNomeEEmail("João Joaquim", "joao@joaquim.com.br");

		assertNull(usuarioDoBanco);
	}

	@Test
	public void deveDeletarUmUsuario() {
		Usuario usuario = new Usuario("Murilo", "murilo@email.com");

		usuarioDao.salvar(usuario);
		usuarioDao.deletar(usuario);

		// Envia tudo para o banco de dados
		session.flush();
		session.clear();

		Usuario usuarioNoBanco = usuarioDao.porNomeEEmail("Murilo", "murilo@email.com");

		assertNull(usuarioNoBanco);
	}

	@Test
	public void fazendoAlteração() {
		Usuario usuario = new Usuario("Murilo", "murilo@email.com");

		usuarioDao.salvar(usuario);

		usuario.setNome("Cássio");
		usuario.setEmail("cassio@email.com");

		usuarioDao.atualizar(usuario);

		session.flush();

		Usuario novoUsuario = usuarioDao.porNomeEEmail("Cássio", "cassio@email.com");
		assertNotNull(novoUsuario);
		System.out.println(novoUsuario);

		Usuario usuarioInexistente = usuarioDao.porNomeEEmail("Murilo", "murilo@email.com");
		assertNull(usuarioInexistente);

	}

}
