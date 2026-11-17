package br.fitforge.gateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class GatewayOpenApi {
    private static final String SECURITY = "fitforgeBasic";

    @Bean
    OpenApiCustomizer fitForgeDocs(@Value("${app.auth-required}") boolean required) {
        return api -> {
            api.setInfo(new Info().title("FitForge — Gateway e BFF").version("1.0")
                    .description("Entrada única para fichas, sessões e indicadores agregados."));
            if (required) {
                if (api.getComponents() == null) api.setComponents(new Components());
                api.getComponents().addSecuritySchemes(SECURITY,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic"));
                api.addSecurityItem(new SecurityRequirement().addList(SECURITY));
            }
            addCrud(api, "/api/treinos", "Fichas de treino", Map.of(
                    "id", "ficha-demo-001", "name", "Treino de demonstração",
                    "days", List.of("Seg"), "exercises", List.of(Map.of(
                        "name", "Supino reto", "muscle", "Peito", "sets", 3, "reps", 12, "load", 20, "rest", 90))));
            addCrud(api, "/api/sessoes", "Sessões de treino", Map.of(
                    "id", "sessao-demo-001", "workoutId", "ficha-demo-001", "workoutName", "Treino de demonstração",
                    "date", LocalDate.now().toString(), "duration", 45, "effort", 7, "bodyWeight", 78.5, "notes", ""));
            api.path("/api/treinos/{id}/atividade", new PathItem().addParametersItem(id())
                    .get(operation("Fichas de treino", "Consultar atividade recebida pelo Service Bus", "200", "Atividade da ficha")));
        };
    }

    private static void addCrud(OpenAPI api, String path, String tag, Map<String, Object> example) {
        api.path(path, new PathItem()
                .get(operation(tag, "Listar registros", "200", "Lista"))
                .post(operation(tag, "Criar registro", "201", "Criado").requestBody(body(example))));
        api.path(path + "/{id}", new PathItem().addParametersItem(id())
                .get(operation(tag, "Consultar registro", "200", "Encontrado"))
                .put(operation(tag, "Editar registro", "200", "Atualizado").requestBody(body(example)))
                .delete(operation(tag, "Excluir registro", "204", "Excluído")));
    }

    private static Parameter id() {
        return new Parameter().in("path").name("id").required(true)
                .schema(new io.swagger.v3.oas.models.media.StringSchema());
    }

    private static RequestBody body(Map<String, Object> example) {
        return new RequestBody().required(true)
                .content(new Content().addMediaType("application/json", new MediaType().schema(new ObjectSchema()).example(example)));
    }

    private static Operation operation(String tag, String summary, String code, String description) {
        return new Operation().addTagsItem(tag).summary(summary).responses(new ApiResponses()
                .addApiResponse(code, new ApiResponse().description(description))
                .addApiResponse("400", new ApiResponse().description("Entrada inválida"))
                .addApiResponse("401", new ApiResponse().description("Autenticação exigida"))
                .addApiResponse("404", new ApiResponse().description("Registro inexistente"))
                .addApiResponse("409", new ApiResponse().description("Conflito")));
    }
}
