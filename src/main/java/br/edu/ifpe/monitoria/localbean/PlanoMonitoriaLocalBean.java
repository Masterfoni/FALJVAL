package br.edu.ifpe.monitoria.localbean;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import br.edu.ifpe.monitoria.entidades.Curso;
import br.edu.ifpe.monitoria.entidades.Edital;
import br.edu.ifpe.monitoria.entidades.PlanoMonitoria;

/**
 * Respons�vel por salvar, listar, atualizar e remover planos de monitoria no banco de dados.
 *
 * @author Felipe Araujo, Jo�o Vitor
 */
@Stateless
@LocalBean
public class PlanoMonitoriaLocalBean 
{
	@PersistenceContext(name = "monitoria", type = PersistenceContextType.TRANSACTION)
	private EntityManager em;
	
	/**
	 * M�todo respons�vel por inserir no banco um novo plano de monitoria
	 *
	 * @param plano uma inst�ncia de {@code PlanoMonitoria} que representa o plano � ser persistido
	 * @return true no caso de sucesso 
	 */
	public boolean persistePlanoMonitoria (@Valid @NotNull PlanoMonitoria plano)
	{
		em.persist(plano);
		
		return true;
	}
	
	/**
	 * M�todo respons�vel por resgatar planos de monitoria com base no ID do servidor
	 *
	 * @param id Identificador �nico do servidor no banco de dados
	 * @return {@code List<PlanoMonitoria>} uma lista de planos de monitoria vinculados � um componente curricular do qual o servidor � professor. 
	 */
	public List<PlanoMonitoria> consultaPlanosByServidor(Long id)
	{
		List<PlanoMonitoria> planos = em.createNamedQuery("PlanoMonitoria.findByProfessor", PlanoMonitoria.class).setParameter("id", id).getResultList();
		
		return planos;
	}
	
	public List<PlanoMonitoria> consultaPlanosByCoordenador(Long id)
	{
		List<PlanoMonitoria> planos = em.createNamedQuery("PlanoMonitoria.findByCoordenador", PlanoMonitoria.class).setParameter("id", id).getResultList();
		
		return planos;
	}
	
	/**
	 * M�todo respons�vel por listar todos os planos cadastrados no sistema
	 *
	 * @return {@code List<PlanoMonitoria>} Uma lista de planos de monitoria, que pode estar vazia ou n�o. 
	 */
	public List<PlanoMonitoria> consultaPlanos()
	{
		List<PlanoMonitoria> planos = em.createNamedQuery("PlanoMonitoria.findAll", PlanoMonitoria.class).getResultList();
		
		return planos;
	}
	
	/**
	 * M�todo respons�vel por resgatar um plano de monitoria espec�fico do banco de dados
	 *
	 * @param id Identificador �nico do plano de monitoria no banco de dados.
	 * @return {@code PlanoMonitoria} Plano de monitoria do banco de dados 
	 */
	public PlanoMonitoria consultaPlanosById(Long id)
	{
		PlanoMonitoria planoPorId = em.createNamedQuery("PlanoMonitoria.findById", PlanoMonitoria.class).setParameter("id", id).getSingleResult();
		
		return planoPorId;
	}
	
	/**
	 * M�todo respons�vel por persistir as atualiza��es de um plano de monitoria j� cadastrado.
	 *
	 * @return {@code true} ou {@code false} dependendo do sucesso, ou n�o da opera��o.
	 */
	public boolean atualizaPlanoMonitoria(PlanoMonitoria plano)
	{
		em.merge(plano);

		return true;
	}
	
	public List<PlanoMonitoria> consultaPlanosByEditaleCurso(Edital edital, Curso curso) {
		List<PlanoMonitoria> planos = em.createNamedQuery("PlanoMonitoria.findByEditaleCurso", PlanoMonitoria.class).
				setParameter("edital", edital).
				setParameter("curso", curso.getId()).
				getResultList();
		
		return planos;
	}
}
