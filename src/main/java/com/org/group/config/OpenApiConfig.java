package com.org.group.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port}")
    private String serverPort;

    @Bean
    public OpenAPI myOpenAPI() {
        Contact contact = new Contact()
                .name("Org Group API Support")
                .email("info@orggroup.com")
                .url("https://www.orggroup.com");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Organization Group Project API")
                .version("1.0")
                .contact(contact)
                .description("This API exposes endpoints for the Organization Group Project.")
                .license(mitLicense);

        // Define the JWT security scheme
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // Add the JWT security requirement
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme));
    }

    @Bean
    public OpenApiCustomizer sortSchemasAlphabetically() {
        return openApi -> openApi.getComponents().getSchemas().keySet().stream()
                .sorted()
                .forEach(schemaName -> openApi.getComponents().addSchemas(schemaName, openApi.getComponents().getSchemas().remove(schemaName)));
    }
}