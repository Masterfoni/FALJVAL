package br.edu.ifpe.monitoria.localbean;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import br.edu.ifpe.monitoria.entidades.Aluno;
import br.edu.ifpe.monitoria.entidades.Coordenacao;
import br.edu.ifpe.monitoria.entidades.Usuario;

@Stateless
@LocalBean
public class AlunoLocalBean 
{
	@PersistenceContext(name = "monitoria", type = PersistenceContextType.TRANSACTION)
	private EntityManager em;
	
	public boolean persisteAluno (@NotNull @Valid Aluno aluno)
	{
		em.persist(aluno);
		
		return true;
	}
	
	public Aluno consultaAlunoById(Long id)
	{
		Usuario usuario = em.createNamedQuery("Usuario.findById", Usuario.class).setParameter("id", id).getSingleResult();
		Aluno alunoPorId = em.createNamedQuery("Aluno.findById", Aluno.class).setParameter("id", usuario.getId()).getSingleResult();
		
		return alunoPorId;
	}
}
