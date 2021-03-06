package br.edu.ifpe.monitoria.entidades;

import java.io.Serializable;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.Valid;

@Entity
@SequenceGenerator (name = "SEQUENCIA_FREQUENCIA",
	sequenceName = "SQ_FREQUENCIA",
	initialValue = 1,
	allocationSize = 1)
@Table(name = "TB_FREQUENCIA")
@Access(AccessType.FIELD)
@NamedQueries({
	@NamedQuery(name = "Frequencia.findById", query = "SELECT f FROM Frequencia f WHERE f.id = :id"),
	@NamedQuery(name = "Frequencia.findByMonitoria", query = "SELECT f FROM Frequencia f WHERE f.monitoria = :monitoria"),
	@NamedQuery(name = "Frequencia.findByAluno", query = "SELECT f FROM Frequencia f WHERE f.monitoria.aluno = :aluno AND f.monitoria.edital.vigente = TRUE ORDER BY f.id"),
	@NamedQuery(name = "Frequencia.findByMonitoriaMes", query = "SELECT f FROM Frequencia f WHERE f.monitoria = :monitoria AND f.mes = :mes")
})
public class Frequencia implements Serializable {

	private static final long serialVersionUID = 3671440051695351979L;

	@Id
	@GeneratedValue (strategy = GenerationType.SEQUENCE, generator="SEQUENCIA_FREQUENCIA")
	private Long id;
	
	@Valid
	@ManyToOne (fetch = FetchType.LAZY, optional = false)
	@JoinColumn (name = "ID_MONITORIA", referencedColumnName = "ID")
	private Monitoria monitoria;
	
	@Valid
	@OneToMany(fetch = FetchType.LAZY, mappedBy="frequencia", cascade=CascadeType.ALL)
	private List<Atividade> atividades;
	
	@Column (name="INT_MES")
	private Integer mes;
	
	@Column (name="BOOL_APROVADO")
	private boolean aprovado;
	
	@Column (name="BOOL_RECEBIDO")
	private boolean recebido;
	
	@ManyToOne
	@JoinColumn(name="ID_RELATORIO_ASSOCIADO")
	private RelatorioFinal relatorioAssociado;

	public Monitoria getMonitoria() {
		return monitoria;
	}

	public void setMonitoria(Monitoria monitoria) {
		this.monitoria = monitoria;
	}

	public List<Atividade> getAtividades() {
		return atividades;
	}

	public void setAtividades(List<Atividade> atividades) {
		this.atividades = atividades;
	}

	public Integer getMes() {
		return mes;
	}

	public void setMes(Integer mes) {
		this.mes = mes;
	}

	public boolean isAprovado() {
		return aprovado;
	}

	public void setAprovado(boolean aprovado) {
		this.aprovado = aprovado;
	}

	public boolean isRecebido() {
		return recebido;
	}

	public void setRecebido(boolean recebido) {
		this.recebido = recebido;
	}	

	public RelatorioFinal getRelatorioAssociado() {
		return relatorioAssociado;
	}

	public void setRelatorioAssociado(RelatorioFinal relatorioAssociado) {
		this.relatorioAssociado = relatorioAssociado;
	}

	public Long getId() {
		return id;
	}

	public void addAtividade(Atividade atividade) {
		this.atividades.add(atividade);
	}

	public void removeAtividade(Atividade atividade) {
		this.atividades.remove(atividade);
	}
	
	public String getNomeMes() {
		List<GregorianCalendar> mesesMonitoria = monitoria.getEdital().getMesesMonitoria();
		GregorianCalendar mesEmQuestao = null;
		
		for (GregorianCalendar mesMonitoria : mesesMonitoria) {
			if(mesMonitoria.get(GregorianCalendar.MONTH) == mes) {
				mesEmQuestao = mesMonitoria;
				break;
			}
		}
		
		Locale brazil = new Locale("pt", "BR");
		
		if(mes == -1) return "Selecione um m�s";
		
		return mesEmQuestao != null ? mesEmQuestao.getDisplayName(GregorianCalendar.MONTH, GregorianCalendar.LONG, brazil) + "/" + 
			   mesEmQuestao.get(GregorianCalendar.YEAR) : "M�S N�O IDENTIFICADO";
	}
	
	@Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        return (object instanceof Frequencia) && (id != null) 
             ? id.equals(((Frequencia) object).getId()) 
             : (object == this);
    }
    
    @Override
    public String toString() {
        return "br.edu.ifpe.monitoria.entidades.Frequencia[ id=" + id  + " ]";
    }
}
