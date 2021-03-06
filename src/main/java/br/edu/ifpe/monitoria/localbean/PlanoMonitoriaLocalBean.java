package br.edu.ifpe.monitoria.localbean;

import java.util.Calendar;
import java.util.Date;
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
import br.edu.ifpe.monitoria.entidades.EsquemaBolsa;
import br.edu.ifpe.monitoria.entidades.Monitoria;
import br.edu.ifpe.monitoria.entidades.PlanoMonitoria;
import br.edu.ifpe.monitoria.utils.AtualizacaoRequestResult;
import br.edu.ifpe.monitoria.utils.CriacaoRequestResult;
import br.edu.ifpe.monitoria.utils.DelecaoRequestResult;

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
	public CriacaoRequestResult persistePlanoMonitoria (@Valid @NotNull PlanoMonitoria plano)
	{
		CriacaoRequestResult resultado = new CriacaoRequestResult();
		
		List<PlanoMonitoria> planoSingleResult = em.createNamedQuery("PlanoMonitoria.findByEditalComponente", PlanoMonitoria.class)
				.setParameter("editalId", plano.getEdital().getId())
				.setParameter("ccId", plano.getCc().getId()).getResultList();

		Calendar fim = Calendar.getInstance();
		fim.setTime(plano.getEdital().getFimInsercaoPlano());
		fim.add(Calendar.DAY_OF_MONTH, 1);
		
		if(!planoSingleResult.isEmpty()) {
			resultado.errors.add("J� existe um plano cadastrado para este edital e componente!");
			resultado.result = false;
		} else if(fim.getTime().before(new Date())) {
			resultado.errors.add("J� se passou o per�odo de cadastro de planos de monitoria!");
		}
		
		
		if(!resultado.hasErrors()) {
			plano.setBolsas(0);
			plano.setHomologado(false);
			
			List<EsquemaBolsa> esquemaSingleResult = em.createNamedQuery("EsquemaBolsa.findByEditalCurso", EsquemaBolsa.class)
					.setParameter("idEdital", plano.getEdital().getId())
					.setParameter("idCurso", plano.getCc().getCurso().getId()).getResultList();
			
			if(esquemaSingleResult.size() > 0) {
				EsquemaBolsa esquemaAssociado = esquemaSingleResult.get(0);
				plano.setEsquemaAssociado(esquemaSingleResult.get(0));
				em.persist(plano);
				
				esquemaAssociado.addPlano(plano);
				em.merge(esquemaAssociado);
			} else {
				em.persist(plano);
			}
			
			resultado.result = true;
		}
		
		return resultado;
	}
	
	/**
	 * M�todo respons�vel por resgatar planos de monitoria com base no ID do servidor
	 *
	 * @param id Identificador �nico do servidor no banco de dados
	 * @return {@code List<PlanoMonitoria>} uma lista de planos de monitoria vinculados � um componente curricular do qual o servidor � professor
	 * ou que o servidor � coordenador do curso ao qual aquele componente pertence. 
	 */
	public List<PlanoMonitoria> consultaPlanosByServidor(Long id)
	{
		List<PlanoMonitoria> planos = em.createNamedQuery("PlanoMonitoria.findByServidor", PlanoMonitoria.class).setParameter("servidorId", id).getResultList();
		
		return planos;
	}
	
	public List<PlanoMonitoria> consultaPlanosByEditalServidor(Long servidor, Long edital)
	{
		List<PlanoMonitoria> planos = em.createNamedQuery("PlanoMonitoria.findByEditalServidor", PlanoMonitoria.class).
				setParameter("servidorId", servidor).
				setParameter("editalId", edital).
				getResultList();
		
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
	public AtualizacaoRequestResult atualizaPlanoMonitoria(PlanoMonitoria plano)
	{
		AtualizacaoRequestResult resultado = new AtualizacaoRequestResult();
		
		PlanoMonitoria planoOriginal = em.createNamedQuery("PlanoMonitoria.findById", PlanoMonitoria.class).setParameter("id", plano.getId()).getSingleResult();
		
		if(planoOriginal.getEdital().getId() != plano.getEdital().getId() || planoOriginal.getCc().getId() != plano.getCc().getId()) {
			List<PlanoMonitoria> planoSingleResult = em.createNamedQuery("PlanoMonitoria.findByEditalComponente", PlanoMonitoria.class)
					.setParameter("editalId", plano.getEdital().getId())
					.setParameter("ccId", plano.getCc().getId()).getResultList();
			
			if(!planoSingleResult.isEmpty()) {
				resultado.errors.add("J� existe um plano cadastrado para este edital e componente!");
				resultado.result = false;
			}
		}
		
		if(!resultado.hasErrors()) {
			em.merge(plano);
			resultado.result = true;
		}
		
		return resultado;
	}
	
	public DelecaoRequestResult deletaPlanoMonitoria(Long id)
	{
		DelecaoRequestResult delecao = new DelecaoRequestResult();
		
		PlanoMonitoria planoDeletado = em.createNamedQuery("PlanoMonitoria.findById", PlanoMonitoria.class).setParameter("id", id).getSingleResult();
		
		List<Monitoria> monitorias = em.createNamedQuery("Monitoria.findByPlano", Monitoria.class).setParameter("plano", planoDeletado).getResultList();
		
		if(monitorias != null && !monitorias.isEmpty()) {
			delecao.errors.add("Existem monitorias vinculados � este edital, n�o � possivel excluir este edital. Mas voc� pode dizer que n�o est� vigente na op��o alterar.");
		}
		
		if(!delecao.hasErrors()) 
		{
			try {
				em.remove(planoDeletado);
			} catch (Exception e) {
				delecao.errors.add("Problemas na remo��o da entidade no banco de dados, contate o suporte.");
			}
		}
				
		return delecao;
	}
	
	public List<PlanoMonitoria> consultaPlanosByEditaleCurso(Edital edital, Curso curso, boolean apenasHomologados) {
		String namedQuery = apenasHomologados ? "PlanoMonitoria.findHomologadosByEditalCurso" : "PlanoMonitoria.findByEditalCurso";
		
		List<PlanoMonitoria> planos = em.createNamedQuery(namedQuery, PlanoMonitoria.class).
				setParameter("edital", edital).
				setParameter("curso", curso.getId()).
				getResultList();
		
		return planos;
	}
	
	public List<PlanoMonitoria> consultaPlanosByEdital(Edital edital, boolean apenasHomologados) {
		String namedQuery = apenasHomologados ? "PlanoMonitoria.findHomologadosByEdital" : "PlanoMonitoria.findByEdital";
		
		List<PlanoMonitoria> planos = em.createNamedQuery(namedQuery, PlanoMonitoria.class).setParameter("edital", edital).getResultList();
		
		return planos;
	}
}
