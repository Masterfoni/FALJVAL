package br.edu.ifpe.monitoria.managedbeans;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import br.edu.ifpe.monitoria.entidades.Aluno;
import br.edu.ifpe.monitoria.entidades.Edital;
import br.edu.ifpe.monitoria.entidades.PerfilGoogle;
import br.edu.ifpe.monitoria.entidades.Servidor;
import br.edu.ifpe.monitoria.entidades.Usuario;
import br.edu.ifpe.monitoria.localbean.AlunoLocalBean;
import br.edu.ifpe.monitoria.localbean.EditalLocalBean;
import br.edu.ifpe.monitoria.localbean.MonitoriaLocalBean;
import br.edu.ifpe.monitoria.utils.SessionContext;

@ManagedBean (name="menuView")
@SessionScoped
public class MenuView implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	MonitoriaLocalBean monitoriaBean;
	
	@EJB
	AlunoLocalBean alunoBean;
	
	@EJB
	private EditalLocalBean editalBean;
	
	private Edital editalGlobal;
	
	boolean comissao;
	
	boolean aluno;
	
	boolean professor;
	
	boolean monitor;
	
	boolean isLoading;

	private Long usuario;
	
	private Usuario lastUsuario;
	
	private PerfilGoogle myPerfilGoogle;
	
	private String myEmail;

	@PostConstruct
	public void init() {
		SessionContext sessionContext = SessionContext.getInstance();
		
		comissao = FacesContext.getCurrentInstance().getExternalContext().isUserInRole("comissao");
		aluno = FacesContext.getCurrentInstance().getExternalContext().isUserInRole("aluno");
		professor = FacesContext.getCurrentInstance().getExternalContext().isUserInRole("professor");
		editalGlobal = editalBean.consultaEditaisVigentes().size() > 0 ?  editalBean.consultaEditaisVigentes().get(0) : null;
		
		if((Long) sessionContext.getAttribute("id") != null) {
			usuario = (Long)sessionContext.getAttribute("id");
		}

		checkMonitor();
	}
	
	public void checkMonitor() {
		if (aluno) {
			monitor = monitoriaBean.isCurrentMonitor(alunoBean.consultaAlunoById(usuario), editalGlobal);
		} else {
			monitor = false;
		}
	}
	
	public Edital getEditalGlobal() {
		return editalGlobal;
	}

	public void setEditalGlobal(Edital editalGlobal) {
		this.editalGlobal = editalGlobal;
	}

	public List<Edital> getEditais() {
		return editalBean.consultaEditaisVigentes();
	}

	public boolean isComissao() {
		return comissao;
	}

	public void setComissao(boolean comissao) {
		this.comissao = comissao;
	}

	public boolean isAluno() {
		return aluno;
	}

	public void setAluno(boolean aluno) {
		this.aluno = aluno;
	}

	public boolean isProfessor() {
		return professor;
	}

	public void setProfessor(boolean professor) {
		this.professor = professor;
	}

	public boolean isMonitor() {
		if (aluno) {
			monitor = monitoriaBean.isCurrentMonitor(alunoBean.consultaAlunoById(usuario), editalGlobal);
		} else {
			monitor = false;
		}
		return monitor;
	}

	public void setMonitor(boolean monitor) {
		this.monitor = monitor;
	}

	public boolean isLoading() {
		return isLoading;
	}

	public void setLoading(boolean isLoading) {
		this.isLoading = isLoading;
	}

	public Usuario getLastUsuario() {
		return lastUsuario;
	}

	public void setLastUsuario(Usuario lastUsuario) {
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
		
		if (lastUsuario instanceof Aluno) {
			setAluno(true);
		} else if (lastUsuario instanceof Servidor) {
			setProfessor(true);
		}
		
		session.setAttribute("usuario", lastUsuario);
		session.setAttribute("id", lastUsuario.getId());
		usuario = (Long)session.getAttribute("id");
		
		this.lastUsuario = lastUsuario;
	}

	public PerfilGoogle getMyPerfilGoogle() {
		return myPerfilGoogle;
	}

	public void setMyPerfilGoogle(PerfilGoogle myPerfilGoogle) {
		this.myPerfilGoogle = myPerfilGoogle;
	}

	public String getMyEmail() {
		return myEmail;
	}

	public void setMyEmail(String myEmail) {
		this.myEmail = myEmail;
	}
}