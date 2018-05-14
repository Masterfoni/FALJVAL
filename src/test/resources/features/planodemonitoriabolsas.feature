# language: pt
Funcionalidade: Distribuir bolsas para cursos e planos de monitoria

 Contexto:
	Dado que o usuario esta logado como comissao
	E esteja na pagina de edital
	E gerencie bolsas

  Cenario: Criar um lan�amento quando ainda n�o existir outro criado
	Quando escolher lan�ar bolsas para um determinado curso
	E nao existir lan�amento criado para aquele curso
	Entao o sistema deve criar um novo lan�amento de bolsas
	
  Cenario: Criar um lan�amento quando j� houver outro criado
  	Quando escolher lan�ar bolsas para um determinado curso
  	E ja existir um lan�amento criado para aquele curso
  	Entao o sistema informa que ja existe um esquema criado
	
  Cenario: Distribuir bolsas para cursos num determinado edital
  	Quando houver um lan�amento criado para algum curso
  	E o usuario informar uma quantidade de bolsas valida
  	Entao o sistema deve disponibilizar aquela quantidade de bolsas para o curso

	