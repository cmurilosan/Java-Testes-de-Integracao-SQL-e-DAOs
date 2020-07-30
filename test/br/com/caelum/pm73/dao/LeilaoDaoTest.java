package br.com.caelum.pm73.dao;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.pm73.dominio.Leilao;
import br.com.caelum.pm73.dominio.LeilaoBuilder;
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
	public void deveRetornarZeroSeNaoHaLeiloesNovos() {
		Usuario murilo = new Usuario("Murilo", "murilo@cassio.com.br");

		Leilao encerrado = new LeilaoBuilder().comDono(murilo).encerrado().constroi();
		Leilao tambemEncerrado = new LeilaoBuilder().comDono(murilo).encerrado().constroi();

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

	@Test
	public void deveTrazerLeiloesNaoEncerradosNoPeriodo() {

		// Criando as datas
		Calendar comecoDoIntervalo = Calendar.getInstance();
		comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, -10);
		Calendar fimDoIntervalo = Calendar.getInstance();
		Calendar dataDoLeilao1 = Calendar.getInstance();
		dataDoLeilao1.add(Calendar.DAY_OF_MONTH, -2);
		Calendar dataDoLeilao2 = Calendar.getInstance();
		dataDoLeilao2.add(Calendar.DAY_OF_MONTH, -20);

		Usuario murilo = new Usuario("Murilo", "murilo@cassio.com.br");

		// Criando os leilões | Cada um com uma data
		Leilao leilao1 = new Leilao("PS4", 1300.0, murilo, false);
		leilao1.setDataAbertura(dataDoLeilao1);
		Leilao leilao2 = new Leilao("Geladeira", 1700.0, murilo, false);
		leilao2.setDataAbertura(dataDoLeilao2);

		// Persistindo os objetos no banco
		usuarioDao.salvar(murilo);
		leilaoDao.salvar(leilao1);
		leilaoDao.salvar(leilao2);

		// Invocando o método para testar
		List<Leilao> leiloes = leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);

		// Garantindo que a query funcionou
		assertEquals(1, leiloes.size());
		assertEquals("PS4", leiloes.get(0).getNome());

	}

	@Test
	public void naoDeveTrazerLeiloesEncerradosNoPeriodo() {

		// Criando as datas
		Calendar comecoDoIntervalo = Calendar.getInstance();
		comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, -10);
		Calendar fimDoIntervalo = Calendar.getInstance();
		Calendar dataDoLeilao1 = Calendar.getInstance();
		dataDoLeilao1.add(Calendar.DAY_OF_MONTH, -2);

		Usuario murilo = new Usuario("Murilo", "murilo@cassio.com.br");

		// Criando os leilões | Cada um com uma data
		Leilao leilao1 = new Leilao("PS4", 1300.0, murilo, false);
		leilao1.setDataAbertura(dataDoLeilao1);
		leilao1.encerra();

		// Persistindo os objetos do banco
		usuarioDao.salvar(murilo);
		leilaoDao.salvar(leilao1);

		// Invocando o metodo para testar
		List<Leilao> leiloes = leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);

		// Garantindo que a query funcionou
		assertEquals(0, leiloes.size());
	}

}
