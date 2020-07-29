package br.com.caelum.pm73.dao;

import static org.junit.Assert.assertEquals;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.pm73.dominio.Leilao;
import br.com.caelum.pm73.dominio.Usuario;

public class LeilaoDaoTest {
	
	private Session session;
	private LeilaoDao leilaoDao;
	private UsuarioDao usuarioDao;

	@Before
	public void antes() {
		// criamos a sessao e a passamos para o dao
		session = new CriadorDeSessao().getSession();
		leilaoDao = new LeilaoDao(session);
		usuarioDao = new UsuarioDao(session);
		
		// Inicia transação
		session.beginTransaction();
	}

	@After
	public void depois() {
		// Faz o rollback
		session.getTransaction().rollback();
		
		// fechamos a sessao
		session.close();
	}
	
	@Test
	public void deveContarLeiloesNaoEncerrados() {
		// Criamos um usuário
		Usuario murilo = new Usuario("Murilo", "murilo@cassio.com.br");
		
		// Criamos os dois leiloes
		Leilao ativo = new Leilao("Geladeira", 1500.0, murilo, false);
		Leilao encerrado = new Leilao("PS4", 1500.0, murilo, false);
		
		encerrado.encerra();
		
		// Persitimos todos no banco
		usuarioDao.salvar(murilo);
		leilaoDao.salvar(ativo);
		leilaoDao.salvar(encerrado);
		
		// Invocamos a ação que queremos testar
		// Pedimos o total para o DAO
		long total = leilaoDao.total();
		
		assertEquals(1L, total);
	}

}
