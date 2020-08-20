# buzz-project
Projeto para aplicação de estágio de back-end em Java para a Buzz Monitor. Projeto foi feito utilizando o Spring boot para requisições HTTP e o ElasticSearch.

## Instalação
É preciso que tenha instalado o JDK em sua máquina, que tenha baixado o ElasticSearch e alguma IDE que julgue ideal para executar o projeto.
Depois de baixado o ElasticSearch, deixe-o executando na porta 9200, que é a que está configurada nesse projeto.

## Execução 
Importe esse projeto, e execute na IDE em que julgar apropriada. É necessário que a porta 8080 esteja livre.
Feito isso, agora pode fazer as requisições disponíveis nesse projeto, que são: 

```
GET api/interactions/
GET (BY ID) api/interactions/{id}
GET (BY USER AND SOCIAL MEDIA) api/interactions/{socialmedia}/{name}
GET POSTS  api/interactions/{socialmedia}/{name}/posts
GET REPLIES  api/interactions/{socialmedia}/{name}/replies
POST api/interactions/
PUT api/intaractions/{id}
DELETE api/interactions/{id}
```