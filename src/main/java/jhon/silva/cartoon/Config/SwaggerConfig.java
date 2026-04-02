package jhon.silva.cartoon.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Cartoon Generator API")
                        .version("1.0.0")
                        .description("API para convertir imágenes a estilo cartoon usando RapidAPI + MongoDB")
                        .contact(new Contact()
                                .name("Tu Nombre")
                                .email("tu@email.com"))
                        .license(new License()
                                .name("MIT")));
    }
}