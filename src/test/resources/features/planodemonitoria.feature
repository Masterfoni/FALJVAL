# language: pt
Funcionalidade: Submeter plano de monitoria

 Contexto:
	Dado que o usuario esta logado como professor
	E esteja na pagina de submissao de plano de monitoria
	E tenta submeter um novo plano de monitoria

  Cenario: Submeter plano de monitoria com informacoes validas
	Quando preencher o formulario de plano de monitoria com informacoes validas
	Entao o sistema deve submeter o plano de monitoria

	