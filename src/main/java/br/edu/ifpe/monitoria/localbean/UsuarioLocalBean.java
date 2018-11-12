package br.edu.ifpe.monitoria.localbean;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import br.edu.ifpe.monitoria.entidades.Grupo;
import br.edu.ifpe.monitoria.entidades.Grupo.Grupos;
import br.edu.ifpe.monitoria.entidades.Usuario;
import br.edu.ifpe.monitoria.utils.DelecaoRequestResult;
import br.edu.ifpe.monitoria.utils.LongRequestResult;

/**
* Classe respons�vel pela execu��o de opera��es sobre a entidade Usu�rio,
* utilizando o {@code EntityManager} como interface para interagir com a base.
* <p> Contexto de persistencia: <strong>monitoria</strong></p>
*  
* @author Jo�o Vitor
* @author Felipe Ara�jo
* 
*/
@Stateless
@LocalBean
public class UsuarioLocalBean {
	@PersistenceContext(name = "monitoria", type = PersistenceContextType.TRANSACTION)
	private EntityManager em;
	
	/**
	 * <p>M�todo respons�vel por trazer todos os usu�rios cadastrados na base do sistema de monitoria
	 * </p>
	 * @return uma lista de objetos do tipo {@code Usu�rio} representando os v�rios usu�rios cadastrados na base de dados
	 */
	public List<Usuario> consultaUsuarios()
	{
		List<Usuario> usuarios = em.createNamedQuery("Usuario.findAll", Usuario.class).getResultList();
		
		return usuarios;
	}
	
	/**
	 * <p>M�todo que valida se um determinado usu�rio pertence ao grupo COMISSAO de permiss�es
	 * </p>
	 * @param usuario Objeto do tipo {@code Usu�rio} que representa o usu�rio � ser consultado
	 * @return {@code true} para o caso do usu�rio estar no grupo COMISSAO ou {@code false} caso o contr�rio
	 */
	public boolean checaComissao(Usuario usuario)
	{
		boolean isComissao = false;
		
		List<Grupo> grupos = em.createQuery("SELECT g FROM Grupo g WHERE g.usuario = :usuario", Grupo.class)
				.setParameter("usuario", usuario)
				.getResultList();
		
		for(int i = 0; i < grupos.size(); i++) {
			if (grupos.get(i).getGrupo().equals(Grupos.COMISSAO)) {
				isComissao = true;
			}
		}
		
		return isComissao;
	}
	
	/**
	 * <p>M�todo respons�vel por retirar um determinado usu�rio do grupo COMISSAO
	 * </p>
	 * @param usuario Objeto do tipo {@code Usu�rio} que representa o usu�rio � ser exclu�do do grupo COMISSAO
	 */
	public void revokeComissao(Usuario usuario)
	{
		List<Grupo> grupos = usuario.getGrupos();
		
		for(int i = 0; i < grupos.size(); i++) {
			Grupo grupo = grupos.get(i);
			
			if(grupo.getGrupo().equals(Grupos.COMISSAO)) {
				usuario.getGrupos().remove(grupo);
				
				if(!em.contains(grupo)) {
					grupo = em.merge(grupo); 
				}
				
				em.remove(grupo);
			}
		}
		
		em.merge(usuario);
		em.flush();
	}
	
	/**
	 * <p>M�todo que concede permiss�es de COMISSAO � um usu�rio, por meio da adi��o do mesmo ao grupo COMISSAO
	 * </p>
	 * @param usuario Objeto do tipo {@code Usu�rio} que representa o usu�rio � ser inclu�do na comiss�o
	 */
	public void grantComissao(Usuario usuario)
	{
		List<Grupo> grupos = usuario.getGrupos();
		boolean anyComissao = false;
		
		for(int i = 0; i < grupos.size(); i++) {
			Grupo grupo = grupos.get(i);
			
			if(grupo.getGrupo().equals(Grupos.COMISSAO)) {
				anyComissao = true;
			}
		}
		
		if(!anyComissao) {
			Grupo newComissao = new Grupo();
			newComissao.setEmail(usuario.getEmail());
			newComissao.setGrupo(Grupos.COMISSAO);
			newComissao.setUsuario(usuario);
			
			em.persist(newComissao);
			
			usuario.getGrupos().add(newComissao);
			
			em.merge(usuario);
		}
	}
	
	/**
	 * <p>M�todo que atualiza informa��es de um usu�rio, especificamente: Email, nome e senha.
	 * </p>
	 * @param usuario Objeto do tipo {@code Usu�rio} que representa o usu�rio com as informa��es atualizadas
	 * @return {@code true} para o caso da opera��o bem sucedida ou {@code false} caso o contr�rio
	 */
	public boolean atualizaUsuario(Usuario usuario)
	{
		try {
			Usuario usuarioAtualizar = em.createNamedQuery("Usuario.findById", Usuario.class).setParameter("id", usuario.getId()).getSingleResult();
			
			usuarioAtualizar.setEmail(usuario.getEmail());
			usuarioAtualizar.setNome(usuario.getNome());
			usuarioAtualizar.setSenha(usuario.getSenha());
			
			em.merge(usuarioAtualizar);			
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * <p>M�todo que busca um determinado usu�rio atrav�s do seu email e sua senha
	 * </p>
	 * @param email Objeto do tipo {@code String} representando o e-mail
	 * @param senha Objeto do tipo {@code String} representando a senha
	 * @return {@code Usuario} o usu�rio encontrado
	 * @exception NoResultException quando n�o existir um usu�rio cadastrado com esta combina��o de e-mail e senha
	 */
	public Usuario consultaUsuarioPorEmailSenha(String email, String senha) throws NoResultException
	{
		Usuario userResult = new Usuario();
		
		try {
			userResult = em.createNamedQuery("Usuario.findByEmailSenha", Usuario.class).setParameter("email", email)
																					   .setParameter("senha", senha)
																					   .getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
			throw e;
		}
		
		return userResult;
	}
	
	/**
	 * <p>M�todo que busca um determinado usu�rio atrav�s apenas do seu e-mail
	 * </p>
	 * @param email Objeto do tipo {@code String} representando o e-mail
	 * @return {@code Usuario} o usu�rio encontrado
	 * @exception NoResultException quando n�o existir um usu�rio cadastrado com este e-mail
	 */
	public Usuario consultaUsuarioPorEmail(String email) throws NoResultException
	{
		Usuario userResult = null;
		
		try {
			userResult = em.createNamedQuery("Usuario.findByEmail", Usuario.class).setParameter("email", email).getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
			throw e;
		}
		
		return userResult;
	}
	
	/**
	 * <p>M�todo que busca um determinado usu�rio atrav�s apenas do seu RG
	 * </p>
	 * @param rg Objeto do tipo {@code String} representando o rg
	 * @return {@code Usuario} o usu�rio encontrado
	 * @exception NoResultException quando n�o existir um usu�rio cadastrado com este rg
	 */
	public Usuario consultaUsuarioPorRg(String rg) throws NoResultException
	{
		Usuario userResult = null;
		
		try {
			userResult = em.createNamedQuery("Usuario.findByRg", Usuario.class).setParameter("rg", rg).getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
			throw e;
		}
		
		return userResult;
	}
	
	/**
	 * <p>M�todo que busca um determinado usu�rio atrav�s apenas do cpf
	 * </p>
	 * @param cpf Objeto do tipo {@code String} representando o cpf
	 * @return {@code Usuario} o usu�rio encontrado
	 * @exception NoResultException quando n�o existir um usu�rio cadastrado com este cpf
	 */
	public Usuario consultaUsuarioPorCpf(String cpf) throws NoResultException
	{
		Usuario userResult = null;
		
		try {
			userResult = em.createNamedQuery("Usuario.findByCpf", Usuario.class).setParameter("cpf", cpf).getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
			throw e;
		}
		
		return userResult;
	}
	
	/**
	 * <p>M�todo que busca um determinado usu�rio atrav�s do ID
	 * </p>
	 * @param id Objeto do tipo {@code Long} representando o cpf
	 * @return {@code Usuario} o usu�rio encontrado
	 * @exception NoResultException quando n�o existir um usu�rio cadastrado com este id
	 */
	public Usuario consultaUsuarioById(Long id) throws NoResultException
	{
		Usuario usuarioPorId = null;

		try {
			usuarioPorId = em.createNamedQuery("Usuario.findById", Usuario.class).setParameter("id", id).getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
			throw e;
		}
		
		return usuarioPorId;
	}
	
	/**
	 * <p>M�todo que busca usu�rios por um determinado nome, no caso desses metodos a palavra inteira tem de ser igual, ignorando apenas uppercase.
	 * </p>
	 * @param nome Objeto do tipo {@code String} representando o nome
	 * @return {@code List<Usuario>} lista de usu�rios encontrados com aquele nome
	 */
	public List<Usuario> consultaUsuarioByName(String nome)
	{
		List<Usuario> usuarios = em.createNamedQuery("Usuario.findByNome", Usuario.class).setParameter("nome", nome).getResultList();
		
		return usuarios;
	}
	
	/**
	 * <p>M�todo que busca o ID de um usu�rio pelo e-mail
	 * </p>
	 * @param email Objeto do tipo {@code String} representando o email
	 * @return {@code LongRequestResult} objeto contendo o ID do usu�rio no caso de sucesso ou contendo uma lista de mensagens de erros.
	 */
	public LongRequestResult consultarIdByEmail(String email)
	{
		LongRequestResult result = new LongRequestResult();
		
		try {
			result.data = em.createNamedQuery("Usuario.findIdByEmail", Long.class).setParameter("email", email).getSingleResult();
		} catch (NoResultException e) {
			result.errors.add("E-mail inexistente!");
		}

		return result;
	}
	
	/**
	 * <p>M�todo que deleta o usu�rio da base por meio do seu identificador �nico
	 * </p>
	 * @param id Objeto do tipo {@code Long} representando o identificador �nico do usu�rio
	 * @return {@code DelecaoRequestResult} objeto contendo {@code true} na vari�vel {@code data} no caso de sucesso ou
	 * contendo {@code false} na vari�vel {@code data} aliado � uma lista de mensagens de erro.
	 */
	public DelecaoRequestResult deletaUsuario(Long id)
	{
		DelecaoRequestResult resultado = new DelecaoRequestResult();
		
		Usuario usuarioDeletado = em.createNamedQuery("Usuario.findById", Usuario.class).setParameter("id", id).getSingleResult();
		
		try 
		{
			em.remove(usuarioDeletado);
			resultado.result = true;
		} catch(Exception e) {
			resultado.errors.add("Problemas na dele��o, contate o suporte.");
			resultado.result = false;
		}
		
		return resultado;
	}
	
	/**
	 * <p>M�todo que persiste um determinado usu�rio na base
	 * </p>
	 * @param usuario Objeto do tipo {@code Usuario} � ser persistido
	 * @return {@code true} ao persistir com sucesso
	 */
	public boolean persisteUsuario(@NotNull @Valid Usuario usuario)
	{
		em.persist(usuario);
		
		return true;
	}
}
