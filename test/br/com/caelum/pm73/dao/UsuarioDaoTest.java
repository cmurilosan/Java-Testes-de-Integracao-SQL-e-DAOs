package br.com.caelum.pm73.dao;

import static org.junit.Assert.assertEquals;

import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.Test;
import org.mockito.Mockito;

import br.com.caelum.pm73.dominio.Usuario;

public class UsuarioDaoTest {
	
	@Test
	public void deveEncontrarPeloNomeEEmailMokado() {
		
		Session session = Mockito.mock(Session.class);
		Query query = Mockito.mock(Query.class);
		UsuarioDao usuarioDao = new UsuarioDao(session);
		
		Usuario usuario = new Usuario("Fulano de Tal", "fulado@detal.com.br");
		
		String sql = "from Usuario u where u.nome = :nome and x.email = :email";
		Mockito.when(session.createQuery(sql)).thenReturn(query);
		Mockito.when(query.uniqueResult()).thenReturn(usuario);
		Mockito.when(query.setParameter("nome", "Fulano de Tal")).thenReturn(query);
		Mockito.when(query.setParameter("email", "fulado@detal.com.br")).thenReturn(query);
		
		Usuario usuarioDoBanco = usuarioDao.porNomeEEmail("Fulano de Tal", "fulado@detal.com.br");
		
		
		assertEquals(usuario.getNome(), usuarioDoBanco.getNome());
		assertEquals(usuario.getEmail(), usuarioDoBanco.getEmail());
	}
	
	/*Excelente. 
	 * Se rodarmos o teste, ele passa! 
	 * Isso quer dizer que conseguimos então simular o banco de dados e facilitar a escrita do teste, certo? 
	 * 
	 * Errado!
	 * 
	 * Olhe a consulta SQL com mais atenção: 
	 * from Usuario u where u.nome = :nome and x.email = :email. 
	 * Veja que o "x.email" está errado! 
	 * Deveria ser "u.email". 
	 * Isso seria facilmente descoberto se não estivéssemos simulando o banco de dados, mas sim usando um banco de dados real! 
	 * A SQL seria imediatamente recusada!
	 */

	

}
