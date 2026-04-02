package jhon.silva.cartoon.Rest;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jhon.silva.cartoon.Model.CartoonResult;
import jhon.silva.cartoon.Service.CartoonService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/cartoon")
@Tag(name = "AI Cartoon Generator", description = "Convierte imágenes a estilo cartoon usando IA")
public class CartoonRest{

    private final CartoonService service;

    public CartoonRest(CartoonService service) {
        this.service = service;
    }

    @Operation(
            summary = "Generar imagen cartoon",
            description = "Sube una imagen (JPEG/PNG/JPG/BMP/WEBP) y un índice de estilo. Guarda resultado en MongoDB."
    )
    @PostMapping(value = "/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<CartoonResult> generate(
            @RequestPart("image") FilePart image,
            @RequestPart("index") String index) {   // ✅ queda String, se convierte en el service
        return service.generateCartoon(image, index);
    }

    @Operation(summary = "Consultar resultado de tarea asíncrona")
    @GetMapping("/task/{taskId}")
    public Mono<CartoonResult> checkTask(
            @Parameter(description = "task_id retornado al generar la imagen")
            @PathVariable String taskId) {
        return service.checkTaskResult(taskId);
    }

    @Operation(summary = "Listar todos los resultados")
    @GetMapping("/all")
    public Flux<CartoonResult> getAll() {
        return service.getAllResults();
    }

    @Operation(summary = "Buscar por ID de MongoDB")
    @GetMapping("/{id}")
    public Mono<CartoonResult> getById(
            @Parameter(description = "ID del documento en MongoDB")
            @PathVariable String id) {
        return service.getById(id);
    }

    @Operation(summary = "Filtrar por estado", description = "Estados: pending, completed, failed")
    @GetMapping("/status/{status}")
    public Flux<CartoonResult> getByStatus(
            @Parameter(description = "Estado: pending | completed | failed")
            @PathVariable String status) {
        return service.getByStatus(status);
    }
}