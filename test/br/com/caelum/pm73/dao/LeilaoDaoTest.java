package br.com.caelum.pm73.dao;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.List;

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
	
	@Test
	public void deveRetornarCasoNaoHajaLeilaoNaoEncerrado() {
		Usuario murilo = new Usuario("Murilo", "murilo@cassio.com.br");
		
		Leilao encerrado = new Leilao("Geladeira", 1500.0, murilo, false);
		Leilao tambemEncerrado = new Leilao("PS4", 1500.0, murilo, false);
		
		encerrado.encerra();
		tambemEncerrado.encerra();
		
		usuarioDao.salvar(murilo);
		leilaoDao.salvar(tambemEncerrado);
		leilaoDao.salvar(encerrado);
		
		long total = leilaoDao.total();
		
		assertEquals(0L, total);
	}
	
	@Test
	public void deveRetornarLeiloesDeProdutosNovos() {
		Usuario murilo = new Usuario("Murilo", "murilo@cassio.com.br");
		
		Leilao produtoNovo = new Leilao("Geladeira", 1500.0, murilo, false);
		Leilao produtoUsado = new Leilao("PS4", 1500.0, murilo, true);
		
		usuarioDao.salvar(murilo);
		leilaoDao.salvar(produtoNovo);
		leilaoDao.salvar(produtoUsado);
		
		List<Leilao> novos = leilaoDao.novos();
		
		assertEquals(1, novos.size());
		assertEquals("Geladeira", novos.get(0).getNome());
	}
	
	@Test
	public void deveTrazerSomenteLeiloesAntigos() {
		Usuario murilo = new Usuario("Murilo", "murilo@cassio.com.br");
		
		Leilao recente = new Leilao("Geladeira", 1500.0, murilo, false);
		Leilao antigo = new Leilao("PS4", 1500.0, murilo, true);
		
		Calendar dataRecente = Calendar.getInstance();
		Calendar dataAntiga = Calendar.getInstance();
		dataAntiga.add(Calendar.DAY_OF_MONTH, -7);
		
		recente.setDataAbertura(dataRecente);
		antigo.setDataAbertura(dataAntiga);
		
		usuarioDao.salvar(murilo);
		leilaoDao.salvar(recente);
		leilaoDao.salvar(antigo);
		
		List<Leilao> antigos = leilaoDao.antigos();
		
		assertEquals(1, antigos.size());
		assertEquals("PS4", antigos.get(0).getNome());
	}
	
	@Test
	public void deveTrazerSomenteLeiloesAntigosHaMaisDe7Dias() {
		Usuario murilo = new Usuario("Murilo", "murilo@cassio.com.br");
		
		Leilao noLimite = new Leilao("PS4", 1500.0, murilo, false);
		
		Calendar dataAntiga = Calendar.getInstance();
		dataAntiga.add(Calendar.DAY_OF_MONTH, -7);
		
		noLimite.setDataAbertura(dataAntiga);
		
		usuarioDao.salvar(murilo);
		leilaoDao.salvar(noLimite);
		
		List<Leilao> antigos = leilaoDao.antigos();
		
		assertEquals(1, antigos.size());
	}

}
