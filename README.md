# FitForge — Gateway e BFF

Spring Cloud Gateway e WebFlux. Entrada única do FitForge com proxy CRUD, autenticação HTTP Basic, CORS, ID de correlação e agregação. Na Azure, use HTTPS.

| Rota | Destino |
|---|---|
| `/api/treinos/**` | Serviço de Treinos |
| `/api/sessoes/**` | Serviço de Progresso |
| `/api/bff/dashboard` | Dashboard que agrega fichas, sessões e estatísticas |
| `/api/bff/treinos/{id}` | Ficha e histórico agregado |
| `/api/bff/historico` | Histórico com estado das fichas |
| `/api/bff/status` | Status das dependências |
| `/swagger-ui.html` | Swagger de rotas proxy e agregadas |

A fatia `bff` reúne o controlador, contratos, serviço e clientes HTTP do dashboard. Segurança e filtros são componentes transversais. O Gateway remove o Authorization de entrada e transmite a chave interna aos serviços quando configurada.

## Rodar e testar

JDK 21 e Maven suficientes:

```powershell
mvn -B clean verify
$env:FITFORGE_AUTH_REQUIRED = "true"
$env:FITFORGE_USERNAME = "fitforge"
$env:FITFORGE_PASSWORD = "senha-local-de-demonstracao"
mvn spring-boot:run
```

O Gateway roda em 8080 e conhece `localhost:8081` e `localhost:8082` por padrão. Na Azure, configure `TREINOS_SERVICE_URL`, `PROGRESSO_SERVICE_URL`, `CORS_ORIGINS`, a autenticação acima e `FITFORGE_INTERNAL_KEY`. Os testes verificam agregação, autenticação, CORS, Swagger e limites entre as camadas. O deploy usa `AZURE_WEBAPP_GATEWAY` e `AZUREAPPSERVICE_PUBLISHPROFILE` do GitHub Actions.

Integrantes em [GRUPO.md](GRUPO.md).
