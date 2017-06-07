package br.edu.ifpe.monitoria.managedbeans;

import java.io.IOException;
import java.io.Serializable;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import br.edu.ifpe.monitoria.entidades.Usuario;
import br.edu.ifpe.monitoria.localBean.UsuarioLocalBean;

@ManagedBean (name="indexView")
public class IndexView implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	private UsuarioLocalBean usuarioBean;
	
	private Usuario usuario;

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	
	public IndexView() {
		usuario = new Usuario();
	}

	public void loginUsuario()
	{
		Usuario usuarioLogado = usuarioBean.consultaUsuarioPorEmailSenha(usuario.getEmail(), usuario.getSenha());
		
		if(usuarioLogado.getId() == null)
		{
			System.out.println("USUARIO N�O ACHADO, EMAIL OU SENHA INCORRETOS");
		}
		else
		{
			try {
				FacesContext.getCurrentInstance().getExternalContext().dispatch("/indexP.xhtml");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
