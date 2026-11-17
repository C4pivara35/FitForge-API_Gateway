# FitForge — Gateway e BFF

Entrada HTTPS da solução, com Spring Cloud Gateway, WebFlux, autenticação HTTP Basic, CORS, proxy CRUD, correlação e agregação em uma resposta. Java 21 e Maven.

**Integrantes:** Andryus Zolet, Nicolas Medeiros, Valentin Rojas e Leonardo Henrique.

## Acesso e repositórios

- [Gateway Azure](https://fitforge-gateway-hvaqcubhf8d0a2hc.chilecentral-01.azurewebsites.net).
- [Swagger Gateway](https://fitforge-gateway-hvaqcubhf8d0a2hc.chilecentral-01.azurewebsites.net/swagger-ui.html).
- [Frontend Azure](https://c4pivara35.z21.web.core.windows.net/).
- [Repositório frontend](https://github.com/C4pivara35/FitForge).
- [Repositório backend](https://github.com/C4pivara35/FitForge-Microservicos).

## Rotas

| Rota | Responsabilidade |
|---|---|
| `/api/treinos/**` | Proxy CRUD para Treinos |
| `/api/sessoes/**` | Proxy CRUD para Progresso |
| `/api/bff/dashboard` | Fichas, sessões, estatísticas e estado dos serviços |
| `/api/bff/treinos/{id}` | Ficha e histórico agregado |
| `/api/bff/historico` | Histórico enriquecido com situação da ficha |
| `/api/bff/status` | Dependências |
| `/api/auth/check` | Verificação de credencial |
| `/api/auth/register` | Cadastro via Progresso, quando configurado |
| `/swagger-ui.html`, `/v3/api-docs` | Documentação OpenAPI |

## Execução e testes locais

Com JDK 21/Maven e serviços locais em 8081/8082, rode na raiz:

```powershell
mvn -B clean verify
$env:FITFORGE_AUTH_REQUIRED = "true"
$env:FITFORGE_USERNAME = "fitforge"
$env:FITFORGE_PASSWORD = "senha-local-de-demonstracao"
mvn spring-boot:run
```

A porta local é 8080. A senha acima é apenas exemplo para o modo local. Para testar cadastro ou segurança interna local, defina o mesmo `FITFORGE_INTERNAL_KEY` no Gateway e nos dois serviços. Os testes incluem unidade de agregação, segurança, integração HTTP e ArchUnit.

## Configuração Azure

Ative `SPRING_PROFILES_ACTIVE=azure` no App Service após preencher `TREINOS_SERVICE_URL`, `PROGRESSO_SERVICE_URL`, `CORS_ORIGINS`, `FITFORGE_USERNAME`, `FITFORGE_PASSWORD` e `FITFORGE_INTERNAL_KEY`. A chave é o mesmo segredo configurado nos dois serviços. Os valores reais ficam no portal, não no Git.

O perfil Azure exige autenticação e não usa URLs localhost como fallback. O Gateway não recebe credenciais de Mongo, SQL ou broker. Remova a possibilidade de acesso direto aos dados ativando a chave interna também nos serviços.

GitHub Actions usa a variável `AZURE_WEBAPP_GATEWAY` e o segredo `AZUREAPPSERVICE_PUBLISHPROFILE`, testa e publica `target/app.jar`. Mudanças somente em README, GRUPO e `docs/` não disparam deploy pelo workflow atualizado. O filtro de deduplicação preserva uma única origem CORS.

## Documentação e entrega

- [Arc42 completa: 12 seções](docs/ARC42.md).
- [Checklist de validação e evidências](docs/VALIDACAO-ENTREGA.md).
- [Guia detalhado de Azure SQL, Service Bus e publicação](https://github.com/C4pivara35/FitForge-Microservicos/blob/main/docs/DEPLOY-AZURE.md).
- [Integrantes](GRUPO.md).

A arquitetura adotada é **Vertical Slice Architecture**, com separação interna de responsabilidades e dependências verificadas por testes. A Arc42 detalha a aplicação de SOLID e os limites do desenho. A conclusão de SQL/mensageria depende da configuração e das evidências na Azure; código e CI aprovados não comprovam essa ativação.

**Vídeo:** pendente de gravação/publicação não listada no YouTube. Inserir aqui o link após validar o fluxo em nuvem, com todos os integrantes falando e com câmera aberta.
