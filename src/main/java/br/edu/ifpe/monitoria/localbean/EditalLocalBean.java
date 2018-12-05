package br.edu.ifpe.monitoria.localbean;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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
 * Respons�vel por salvar, listar, atualizar e remover editais no banco de dados.
 *
 * @author Felipe Araujo, Jo�o Vitor
 */
@Stateless
@LocalBean
public class EditalLocalBean 
{
	@PersistenceContext(name = "monitoria", type = PersistenceContextType.TRANSACTION)
	private EntityManager em;
	
	/** Lista todos os editais criados no sistema
     * @return List<Edital> - Lista de editais
     */
	public List<Edital> consultaEditais()
	{
		List<Edital> editais = em.createNamedQuery("Edital.findAll", Edital.class).getResultList();
		
		return editais;
	}
	
	/** Consulta editais vigente
     * @return List<Edital> - Lista de editais vigentes
     */
	public List<Edital> consultaEditaisVigentes() {
		List<Edital> editais = em.createNamedQuery("Edital.findVigentes", Edital.class).getResultList();
		
		return editais;
	}
	
	/** Consulta edital no sistema por n�mero
     * @param String numero - Numero do edital
     * @return Edital - edital que possui exatamente o n�mero passado
     */
	public Edital consultaEditalByNumero(String numero) {
		return em.createNamedQuery("Edital.findByNumero", Edital.class).setParameter("numero", numero).getSingleResult();
	}
	
	/** Consulta um edital no sistema pelo ID
     * @param id Long - ID a ser consultado
     * @exception NoResultException 
     * @return Edital - Edital encontrado com ID informado
     */
	public Edital consultaEditalById(Long id)
	{
		Edital editalPorId = null;
		try {
			editalPorId = em.createNamedQuery("Edital.findById", Edital.class)
										   .setParameter("id", id).getSingleResult();
		}
		catch (NoResultException e) {
			e.printStackTrace();
		}
		
		return editalPorId;
	}
	
	/** Salva um edital v�lido no sistema
     * @param edital Edital - Edital a ser salvo
     * @return long - ID do edital persistido
     */
	public CriacaoRequestResult persisteEdital(@NotNull @Valid Edital edital)
	{
		CriacaoRequestResult resultado = new CriacaoRequestResult();
		
		resultado.errors = validarDatas(edital);
		
		if(!resultado.hasErrors())
		{		
			List<Curso> cursos = em.createNamedQuery("Curso.findAll", Curso.class).getResultList();

			em.persist(edital);
			
			for(Curso curso : cursos) {
				EsquemaBolsa esquema = new EsquemaBolsa();
				esquema.setEdital(edital);
				esquema.setCurso(curso);
				esquema.setQuantidade(0);
				esquema.setDistribuido(false);
				
				em.persist(esquema);
			}
			
			resultado.result = true;
		}
		
		return resultado;
	}
	
	/** Atualiza informa��es de um edital especifico no sistema
     * @param edital Edital - Edital atualizado
     * @return boolean - Informa se houve sucesso na transa��o
     */
	public AtualizacaoRequestResult atualizaEdital(Edital edital)
	{
		AtualizacaoRequestResult resultado = new AtualizacaoRequestResult();
		
		resultado.errors = validarDatas(edital);

		List<EsquemaBolsa> esquemas = em.createNamedQuery("EsquemaBolsa.findByEdital", EsquemaBolsa.class).setParameter("idEdital", edital.getId()).getResultList();
		
		if(edital.isVigente()) {
			for(EsquemaBolsa esquema : esquemas) {
				if(!esquema.isDistribuido()) {
					resultado.errors.add("Voc� deve explicitar o n�mero de bolsas para cada curso!");
					break;
				}
			}
		}
		
		
		if(!resultado.hasErrors())
		{
			em.merge(edital);
			resultado.result = true;
		}
		
		return resultado;
	}
	
	/** Deleta um edital no sistema pelo ID
     * @param id Long - ID do edital a ser exclu�do
     * @return boolean - Informa se houve sucesso na transa��o
     */
	public DelecaoRequestResult deletaEdital(Long id)
	{
		DelecaoRequestResult delecao = new DelecaoRequestResult();
		boolean podeExcluir = true;
		
		Edital editalDeletado = em.createNamedQuery("Edital.findById", Edital.class)
				.setParameter("id", id).getSingleResult();
		
		List<Monitoria> monitorias = em.createNamedQuery("Monitoria.findByEdital", Monitoria.class).setParameter("edital", editalDeletado).getResultList();
		List<PlanoMonitoria> planos = em.createNamedQuery("PlanoMonitoria.findByEdital", PlanoMonitoria.class).setParameter("edital", editalDeletado).getResultList();
		
		if(monitorias != null && !monitorias.isEmpty()) {
			delecao.errors.add("Existem monitorias vinculados � este edital, n�o � possivel excluir este edital. "
					+ "Mas voc� pode dizer que n�o est� vigente na op��o alterar.");
			podeExcluir = false;
		}
		if(planos != null && !planos.isEmpty()) {
			delecao.errors.add("Existem planos de monitorias vinculados � este edital, n�o � possivel excluir este edital. "
					+ "Remova-os primeiro!");
			podeExcluir = false;
		}
		
		if(podeExcluir) {
			try {
				em.remove(editalDeletado);
			} catch (Exception e) {
				delecao.errors.add("Problemas na remo��o da entidade no banco de dados, contate o suporte.");
			}
		}
				
		return delecao;
	}
	
	private List<String> validarDatas(Edital edital) {
		List<String> validationFailures = new ArrayList<String>();
		
		if (edital.getInicioInscricaoComponenteCurricular().after(edital.getFimInscricaoComponenteCurricular())) 
		{
			validationFailures.add("A data para o fim de Inser��o do Componente Curricular deve ser depois da data de in�cio.");
		}
		if (edital.getInicioInscricaoEstudante().after(edital.getFimInscricaoEstudante())) 
		{
			validationFailures.add("A data para o fim de Inscri��o dos Alunos deve ser depois da data de in�cio.");
		}
		if (edital.getInicioInsercaoNota().after(edital.getFimInsercaoNota())) 
		{
			validationFailures.add("A data para o fim de Inser��o das Notas deve ser depois da data de in�cio.");
		}
		if (edital.getInicioInsercaoPlano().after(edital.getFimInsercaoPlano())) 
		{
			validationFailures.add("A data para o fim de Inser��o dos Planos de Monitoria deve ser depois da data de in�cio.");
		}
		if (edital.getInicioMonitoria().after(edital.getFimMonitoria())) 
		{
			validationFailures.add("A data para o fim da Monitoria deve ser depois da data de in�cio.");
		}
		
		return validationFailures;
	}
}
