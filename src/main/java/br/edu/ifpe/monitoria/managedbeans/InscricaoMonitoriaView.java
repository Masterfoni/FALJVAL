package br.edu.ifpe.monitoria.managedbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import br.edu.ifpe.monitoria.entidades.Aluno;
import br.edu.ifpe.monitoria.entidades.Curso;
import br.edu.ifpe.monitoria.entidades.Edital;
import br.edu.ifpe.monitoria.entidades.Monitoria;
import br.edu.ifpe.monitoria.entidades.PlanoMonitoria;
import br.edu.ifpe.monitoria.localbean.AlunoLocalBean;
import br.edu.ifpe.monitoria.localbean.CursoLocalBean;
import br.edu.ifpe.monitoria.localbean.EditalLocalBean;
import br.edu.ifpe.monitoria.localbean.MonitoriaLocalBean;
import br.edu.ifpe.monitoria.localbean.PlanoMonitoriaLocalBean;

@ManagedBean (name="inscricaoMonitoriaView")
@ViewScoped
public class InscricaoMonitoriaView implements Serializable{
	
	private static final long serialVersionUID = -2822785320179539798L;
	
	@EJB
	private PlanoMonitoriaLocalBean planoBean;
	
	@EJB
	private MonitoriaLocalBean monitoriaBean;
	
	@EJB
	private AlunoLocalBean alunoBean;
	
	@EJB
	private CursoLocalBean cursoBean;
	
	@EJB
	private EditalLocalBean editalBean;
	
	private Monitoria monitoria;
	
	private List<PlanoMonitoria> planos;
	
	private List<Curso> cursos;
	
	private Curso curso;
	
	private Aluno aluno;
	
	private List<Edital> editais;
	
	private Edital edital;
	
	boolean bolsista;
	
	boolean voluntario;

	public InscricaoMonitoriaView() {
		planos = new ArrayList<PlanoMonitoria>();
		bolsista = false;
		voluntario = false;
	}
	
	@PostConstruct
	public void init() {
		editais = editalBean.consultaEditaisVigentes();
		if (editais != null && editais.size() > 0) {
			edital = editais.get(0);
		}
	}
	
	public void salvarMonitoria(PlanoMonitoria plano) {
		boolean alteracao = true;
		
		if(monitoria == null) {
			monitoria = new Monitoria();
			alteracao = false;
		}
		
		monitoria.setPlanoMonitoria(plano);
		monitoria.setAluno(aluno);
		monitoria.setEdital(getEdital());
		monitoria.setBolsista(bolsista);
		monitoria.setVoluntario(voluntario);
		
		if(alteracao) {
			monitoriaBean.atualizaMonitoria(monitoria);
		} else {
			monitoriaBean.persisteMonitoria(monitoria);
		}
	}
	
	public void alterarCurso() {	
		aluno.setCurso(curso);
		alunoBean.atualizaAluno(aluno);
	}
	
	public List<PlanoMonitoria> getPlanos() {
		planos = null;
		if(getEdital() != null) {
			planos = planoBean.consultaPlanosByEditaleCurso(edital, getAluno().getCurso(), true);
		}
		if(planos == null) {
			FacesContext context = FacesContext.getCurrentInstance();
			context.addMessage(null, new FacesMessage("N�o existe programa de monitoria disponivel para inscri��o."));
		} 
		return planos;
	}

	public void setPlanos(ArrayList<PlanoMonitoria> planos) {
		this.planos = planos;
	}

	public void setPlanos(List<PlanoMonitoria> planos) {
		this.planos = planos;
	}
	
	public Monitoria getMonitoria() {
		if(monitoria == null && edital != null) {
			monitoria = monitoriaBean.consultaMonitoriaByAlunoEdital(getAluno(), getEdital());
		}
		return monitoria;
	}

	public void setMonitoria(Monitoria monitoria) {
		this.monitoria = monitoria;
	}

	public Aluno getAluno() {
		if(aluno == null) {
			FacesContext fc = FacesContext.getCurrentInstance();
			ExternalContext ec = fc.getExternalContext(); 
			HttpSession session = (HttpSession) ec.getSession(false);
		
			Long id = (Long) session.getAttribute("id");
			aluno = alunoBean.consultaAlunoById(id);
		}
		return aluno;
	}

	public void setAluno(Aluno aluno) {
		this.aluno = aluno;
	}

	public List<Curso> getCursos() {
		if(cursos == null) {
			cursos = cursoBean.consultaCursos();
		}
		return cursos;
	}

	public void setCursos(List<Curso> cursos) {
		this.cursos = cursos;
	}

	public Curso getCurso() {
		curso = getAluno().getCurso();
		return curso;
	}

	public void setCurso(Curso curso) {
		this.curso = curso;
	}

	public List<Edital> getEditais() {
		if(editais == null) {
			List<Edital> editaisVigentes = editalBean.consultaEditaisVigentes();
			editais = new ArrayList<Edital>();
			Date dataAtual = new Date();
			for (Edital ed : editaisVigentes) {
				if(dataAtual.before(ed.getFimInscricaoEstudante()) &&
						dataAtual.after(ed.getInicioInscricaoEstudante())) {
					editais.add(ed);
				}
			}
		}
		return editais;
	}

	public void setEditais(List<Edital> editais) {
		this.editais = editais;
	}

	public Edital getEdital() {
		return edital;
	}

	public void setEdital(Edital edital) {
		this.edital = edital;
	}

	public boolean isBolsista() {
		return bolsista;
	}

	public void setBolsista(boolean bolsista) {
		this.bolsista = bolsista;
	}

	public boolean isVoluntario() {
		return voluntario;
	}

	public void setVoluntario(boolean voluntario) {
		this.voluntario = voluntario;
	}	
}
