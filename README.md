# buzz-project
Projeto para aplicação de estágio de back-end em Java para a Buzz Monitor. Projeto foi feito utilizando o Spring boot para requisições HTTP e o ElasticSearch.

## Instalação
É preciso que tenha instalado o JDK em sua máquina, que tenha baixado o ElasticSearch e alguma IDE que julgue ideal para executar o projeto.
Além disso, baixe o ElasticSearch.

## Execução 
Execute o ElasticSearch.
Em seguida, importe esse projeto na IDE em que julgar apropriada. É necessário que a porta 8080 esteja livre.
Feito isso, agora vá em: Project > Properties > Java Build Path > Add External JARs e adicione o item java-json.jar que se encontra na pasta do projeto.
Agora clique em executar o projeto. Depois disso, o sistema irá ler o json que está nesse projeto, que é "buzz-project.json".
Pronto, já pode fazer as requisições disponíveis nesse projeto, que são: 

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
