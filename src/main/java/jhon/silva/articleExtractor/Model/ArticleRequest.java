package jhon.silva.articleExtractor.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para solicitud de resumen de artículo")
public class ArticleRequest {

    @Schema(
            description = "URL del artículo a resumir",
            example = "https://elperuano.pe/noticia/292757-una-multitud-de-personas-se-concentra-en-bagdad",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String url;

    @Schema(description = "Idioma del resumen", example = "es", defaultValue = "es")
    private String lang = "es";

    @Schema(description = "Número de oraciones del resumen", example = "3", defaultValue = "3")
    private Integer length = 3;
}