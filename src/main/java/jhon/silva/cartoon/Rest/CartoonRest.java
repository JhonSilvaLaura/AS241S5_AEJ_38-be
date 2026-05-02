package jhon.silva.cartoon.Rest;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jhon.silva.cartoon.Model.CartoonResult;
import jhon.silva.cartoon.Service.ICartoonService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/cartoon")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@Tag(name = "AI Cartoon Generator", description = "Convierte imágenes a estilo cartoon usando IA")
public class CartoonRest{


    private final ICartoonService service;

    public CartoonRest(ICartoonService service) {
        this.service = service;
    }

    @Operation(
            summary = "Generar imagen cartoon",
            description = "Sube una imagen (JPEG/PNG/JPG/BMP/WEBP) y un índice de estilo. Guarda resultado en MongoDB."
    )
    @PostMapping(value = "/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<CartoonResult> generate(
            @RequestPart("image") FilePart image,
            @RequestPart("index") String index) {   //  queda String, se convierte en el service
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

    @Operation(
            summary = "Actualizar imagen cartoon",
            description = "Actualiza un registro existente regenerando el cartoon con nueva imagen/índice usando la API de IA"
    )
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<CartoonResult> update(
            @Parameter(description = "ID del documento en MongoDB")
            @PathVariable String id,
            @RequestPart("image") FilePart image,
            @RequestPart("index") String index) {
        return service.updateCartoon(id, image, index);
    }

    @Operation(
            summary = "Eliminar registro (borrado lógico)",
            description = "Marca un registro como eliminado sin borrarlo físicamente de la base de datos"
    )
    @DeleteMapping("/{id}")
    public Mono<CartoonResult> delete(
            @Parameter(description = "ID del documento en MongoDB")
            @PathVariable String id) {
        return service.deleteCartoon(id);
    }

    @Operation(
            summary = "Descargar imagen cartoon",
            description = "Descarga la imagen cartoon generada a través del backend (proxy para evitar CORS)"
    )
    @GetMapping("/download")
    public Mono<org.springframework.http.ResponseEntity<byte[]>> downloadImage(
            @Parameter(description = "URL de la imagen a descargar")
            @RequestParam String imageUrl) {
        return service.downloadImage(imageUrl)
                .map(bytes -> {
                    org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                    headers.setContentType(org.springframework.http.MediaType.IMAGE_PNG);
                    headers.setContentDisposition(
                            org.springframework.http.ContentDisposition
                                    .attachment()
                                    .filename("cartoon_image.png")
                                    .build()
                    );
                    headers.setContentLength(bytes.length);
                    
                    return org.springframework.http.ResponseEntity
                            .ok()
                            .headers(headers)
                            .body(bytes);
                })
                .onErrorResume(e -> {
                    System.out.println(">>> ERROR en endpoint download: " + e.getMessage());
                    return Mono.just(
                            org.springframework.http.ResponseEntity
                                    .status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(null)
                    );
                });
    }
}