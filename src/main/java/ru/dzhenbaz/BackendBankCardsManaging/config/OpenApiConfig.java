package ru.dzhenbaz.BackendBankCardsManaging.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI (Swagger) для документирования REST API приложения.
 * Добавляет описание API и настройку авторизации через Bearer токен (JWT).
 */
@Configuration
public class OpenApiConfig {

    /**
     * Настраивает основную информацию об API и параметры безопасности для Swagger UI.
     *
     * @return объект {@link OpenAPI} с настройками документации
     */
    @Bean
    public OpenAPI apiInfo() {
        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Система управления банковскими картами")
                        .description("REST API для управления банковскими картами")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Dzhenbaz Arthur")
                                .email("artur.dzhenbaz@gmail.com")
                                .url("https://github.com/TusyaSonne")))
                // Настройка JWT-аутентификации
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components().addSecuritySchemes(securitySchemeName,
                        new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                ));
    }
}
