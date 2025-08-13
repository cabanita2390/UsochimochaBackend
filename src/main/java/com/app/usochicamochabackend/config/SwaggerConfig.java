package com.app.usochicamochabackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(info = @Info(
        title = "Usochicamocha API",
        description = "Our app provides a concise way for supervising machines",
        version = "1.0.0"
    ),
        servers = {
            @Server(
                    description = "DEV SERVER",
                    url = "http://localhost:8080"
            ),
            @Server(
                    description = "PROD SERVER",
                    url = "https://usochicamocha.com"
            )    
        }
)
public class SwaggerConfig {
}
