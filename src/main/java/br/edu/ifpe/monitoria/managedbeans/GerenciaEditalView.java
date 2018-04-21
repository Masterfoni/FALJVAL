package br.edu.ifpe.monitoria.managedbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import br.edu.ifpe.monitoria.entidades.Edital;
import br.edu.ifpe.monitoria.localbean.EditalLocalBean;

@ManagedBean (name="gerenciaEditalView")
@ViewScoped
public class GerenciaEditalView implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	private EditalLocalBean editalbean;

	public List<Edital> editais;
	
	public Edital editalAtualizado;
	
	public Edital editalPersistido;
	
	public String numeroBusca;
	
	public List<Edital> getEditais() {
		editais = editalbean.consultaEditais();
		return editais;
	}

	public void setEditais(List<Edital> editais) {
		this.editais = editais;
	}

	public Edital getEditalAtualizado() {
		return editalAtualizado;
	}

	public void setEditalAtualizado(Edital editalAtualizado) {
		this.editalAtualizado = editalAtualizado;
	}

	public Edital getEditalPersistido() {
		return editalPersistido;
	}

	public void setEditalPersistido(Edital editalPersistido) {
		this.editalPersistido = editalPersistido;
	}

	public String getnumeroBusca() {
		return numeroBusca;
	}

	public void setnumeroBusca(String numeroBusca) {
		this.numeroBusca = numeroBusca;
	}

	public GerenciaEditalView() {
		numeroBusca = "";
		editais = new ArrayList<Edital>();
		editalAtualizado = new Edital();
		editalPersistido = new Edital();
	}
	
	public void cadastrarEdital()
	{
		if(validarDatas(editalPersistido)) {		
			editalPersistido.setNumeroEdital(editalPersistido.getNumero() + "/" + editalPersistido.getAno());
			if(editalbean.persisteEdital(editalPersistido))
			{
				FacesContext context = FacesContext.getCurrentInstance();
				context.addMessage(null, new FacesMessage("Cadastro realizado com sucesso!"));
				editalPersistido = new Edital();
			}
		}
	}
	
	private boolean validarDatas(Edital edital) {
		FacesContext context = FacesContext.getCurrentInstance();
		boolean flag = true;
		if (edital.getInicioInscricaoComponenteCurricular().after(edital.getFimInscricaoComponenteCurricular())) {
			context.addMessage(null, new FacesMessage("A data para o fim de Inser��o do Componente Curricular deve ser depois da data de in�cio."));
			flag = false;
		}  if (edital.getInicioInscricaoEstudante().after(edital.getFimInscricaoEstudante())) {
			context.addMessage(null, new FacesMessage("A data para o fim de Inscri��o dos Alunos deve ser depois da data de in�cio."));
			flag = false;
		}  if (edital.getInicioInsercaoNota().after(edital.getFimInsercaoNota())) {
			context.addMessage(null, new FacesMessage("A data para o fim de Inser��o das Notas deve ser depois da data de in�cio."));
			flag = false;
		}  if (edital.getInicioInsercaoPlano().after(edital.getFimInsercaoPlano())) {
			context.addMessage(null, new FacesMessage("A data para o fim de Inser��o dos Planos de Monitoria deve ser depois da data de in�cio."));
			flag = false;
		}  if (edital.getInicioMonitoria().after(edital.getFimMonitoria())) {
			context.addMessage(null, new FacesMessage("A data para o fim da Monitoria deve ser depois da data de in�cio."));
			flag = false;
		}
		return flag;
	}

	public String deletaEdital(Edital edital) {
		editais.remove(edital);
		editalbean.deletaEdital(edital.getId());
		return "";
	}
	
	public void alteraEdital(Edital edital) {
		editalAtualizado = edital;
		System.out.println(editalAtualizado);
	}
	
	public void persisteAlteracao() {
		if(validarDatas(editalAtualizado)) {	
			editalAtualizado.setNumeroEdital(editalAtualizado.getNumero() + "/" + editalAtualizado.getAno());
			editalbean.atualizaEdital(editalAtualizado);
		}
	}
}
