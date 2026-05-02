package jhon.silva.cartoon.Service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jhon.silva.cartoon.Model.CartoonResult;
import jhon.silva.cartoon.Repository.CartoonRepository;
import jhon.silva.cartoon.Service.ICartoonService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class CartoonServiceImpl implements ICartoonService {

    private final WebClient webClient;
    private final CartoonRepository repository;

    @Value("${rapidapi.endpoint-generate}")
    private String endpointGenerate;

    @Value("${rapidapi.endpoint-result}")
    private String endpointResult;

    @Value("${rapidapi.key}")
    private String rapidApiKey;

    @Value("${rapidapi.host}")
    private String rapidApiHost;

    @Value("${rapidapi.base-url}")
    private String baseUrl;

    public CartoonServiceImpl(WebClient cartoonWebClient, CartoonRepository repository) {
        this.webClient = cartoonWebClient;
        this.repository = repository;
    }

    @Override
    public Mono<CartoonResult> generateCartoon(FilePart image, String index) {

        Mono<byte[]> imageBytes = DataBufferUtils
                .join(image.content())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                });

        return imageBytes.flatMap(bytes -> {

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("image", bytes)
                    .filename(image.filename())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM);
            builder.part("index", Integer.parseInt(index));
            builder.part("task_type", "async");

            System.out.println(">>> POST: " + endpointGenerate);
            System.out.println(">>> imagen: " + image.filename() + " | index: " + index);

            return webClient.post()
                    .uri(endpointGenerate)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> resp = (Map<String, Object>) response;
                        System.out.println(">>> Respuesta API: " + resp);

                        CartoonResult result = new CartoonResult();
                        result.setImageName(image.filename());
                        result.setCartoonIndex(Integer.parseInt(index));

                        if (resp.containsKey("request_id"))
                            result.setRequestId(resp.get("request_id").toString());
                        if (resp.containsKey("log_id"))
                            result.setLogId(resp.get("log_id").toString());
                        if (resp.containsKey("error_code"))
                            result.setErrorCode(((Number) resp.get("error_code")).intValue());
                        if (resp.containsKey("error_msg"))
                            result.setErrorMsg(resp.get("error_msg").toString());
                        if (resp.containsKey("task_type"))
                            result.setTaskType(resp.get("task_type").toString());
                        if (resp.containsKey("task_id"))
                            result.setTaskId(resp.get("task_id").toString());

                        Integer errorCode = result.getErrorCode();
                        result.setStatus(errorCode != null && errorCode == 0 ? "pending" : "failed");

                        return result;
                    })
                    .flatMap(repository::save)
                    .onErrorResume(e -> {
                        System.out.println(">>> ERROR generate: " + e.getMessage());
                        CartoonResult errorResult = new CartoonResult();
                        errorResult.setImageName(image.filename());
                        errorResult.setCartoonIndex(Integer.parseInt(index));
                        errorResult.setStatus("failed");
                        errorResult.setErrorMsg(e.getMessage());
                        return repository.save(errorResult);
                    });
        });
    }

    @Override
    public Mono<CartoonResult> checkTaskResult(String taskId) {

        System.out.println(">>> GET task_id: " + taskId);

        String fullUrl = baseUrl + endpointResult + "?task_id=" + taskId;
        System.out.println(">>> URL: " + fullUrl);

        return Mono.fromCallable(() -> {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(fullUrl))
                            .GET()
                            .header("x-rapidapi-key", rapidApiKey)
                            .header("x-rapidapi-host", rapidApiHost)
                            .header("Content-Type", "application/json")
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    System.out.println(">>> HTTP Status: " + response.statusCode());
                    System.out.println(">>> Body: " + response.body());
                    return response.body();
                })
                .flatMap(body -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        @SuppressWarnings("unchecked")
                        Map<String, Object> resp = mapper.readValue(body, Map.class);

                        return repository.findByTaskId(taskId)
                                .flatMap(existing -> {

                                    if (resp.containsKey("error_code"))
                                        existing.setErrorCode(((Number) resp.get("error_code")).intValue());

                                    if (resp.containsKey("task_status")) {
                                        int ts = ((Number) resp.get("task_status")).intValue();
                                        existing.setTaskStatus(ts);
                                        System.out.println(">>> task_status: " + ts);

                                        switch (ts) {
                                            case 2:
                                                existing.setStatus("completed");
                                                if (resp.containsKey("data")) {
                                                    @SuppressWarnings("unchecked")
                                                    Map<String, Object> data = (Map<String, Object>) resp.get("data");
                                                    if (data != null && data.containsKey("result_url"))
                                                        existing.setResultUrl(data.get("result_url").toString());
                                                }
                                                break;
                                            case 1:
                                                existing.setStatus("pending");
                                                break;
                                            case 0:
                                                existing.setStatus("pending");
                                                break;
                                            default:
                                                existing.setStatus("failed");
                                        }
                                    }

                                    Integer code = existing.getErrorCode();
                                    if (code != null && code != 0)
                                        existing.setStatus("failed");

                                    return repository.save(existing);
                                });
                    } catch (Exception e) {
                        System.out.println(">>> ERROR parseando: " + e.getMessage());
                        return repository.findByTaskId(taskId)
                                .flatMap(existing -> {
                                    existing.setStatus("failed");
                                    existing.setErrorMsg(e.getMessage());
                                    return repository.save(existing);
                                });
                    }
                })
                .onErrorResume(e -> {
                    System.out.println(">>> ERROR checkTask: " + e.getMessage());
                    return repository.findByTaskId(taskId)
                            .flatMap(existing -> {
                                existing.setStatus("failed");
                                existing.setErrorMsg(e.getMessage());
                                return repository.save(existing);
                            });
                });
    }

    @Override
    public Flux<CartoonResult> getAllResults() {
        return repository.findByDeletedFalse(); // Solo retorna los no eliminados
    }

    @Override
    public Flux<CartoonResult> getByStatus(String status) {
        return repository.findByStatusAndDeletedFalse(status); // Solo retorna los no eliminados
    }

    @Override
    public Mono<CartoonResult> getById(String id) {
        return repository.findById(id)
                .filter(result -> !result.getDeleted()); // Solo retorna si no está eliminado
    }

    @Override
    public Mono<CartoonResult> updateCartoon(String id, FilePart image, String index) {
        System.out.println(">>> UPDATE cartoon id: " + id);

        return repository.findById(id)
                .filter(existing -> !existing.getDeleted()) // Verificar que no esté eliminado
                .flatMap(existing -> {
                    // Generar nuevo cartoon con la API
                    Mono<byte[]> imageBytes = DataBufferUtils
                            .join(image.content())
                            .map(dataBuffer -> {
                                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(bytes);
                                DataBufferUtils.release(dataBuffer);
                                return bytes;
                            });

                    return imageBytes.flatMap(bytes -> {
                        MultipartBodyBuilder builder = new MultipartBodyBuilder();
                        builder.part("image", bytes)
                                .filename(image.filename())
                                .contentType(MediaType.APPLICATION_OCTET_STREAM);
                        builder.part("index", Integer.parseInt(index));
                        builder.part("task_type", "async");

                        System.out.println(">>> POST UPDATE: " + endpointGenerate);
                        System.out.println(">>> nueva imagen: " + image.filename() + " | index: " + index);

                        return webClient.post()
                                .uri(endpointGenerate)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .body(BodyInserters.fromMultipartData(builder.build()))
                                .retrieve()
                                .bodyToMono(Map.class)
                                .map(response -> {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> resp = (Map<String, Object>) response;
                                    System.out.println(">>> Respuesta API UPDATE: " + resp);

                                    // Actualizar el registro existente
                                    existing.setImageName(image.filename());
                                    existing.setCartoonIndex(Integer.parseInt(index));
                                    existing.setUpdatedAt(java.time.LocalDateTime.now());

                                    if (resp.containsKey("request_id"))
                                        existing.setRequestId(resp.get("request_id").toString());
                                    if (resp.containsKey("log_id"))
                                        existing.setLogId(resp.get("log_id").toString());
                                    if (resp.containsKey("error_code"))
                                        existing.setErrorCode(((Number) resp.get("error_code")).intValue());
                                    if (resp.containsKey("error_msg"))
                                        existing.setErrorMsg(resp.get("error_msg").toString());
                                    if (resp.containsKey("task_type"))
                                        existing.setTaskType(resp.get("task_type").toString());
                                    if (resp.containsKey("task_id"))
                                        existing.setTaskId(resp.get("task_id").toString());

                                    // Resetear el resultado anterior
                                    existing.setResultUrl(null);
                                    existing.setTaskStatus(null);

                                    Integer errorCode = existing.getErrorCode();
                                    existing.setStatus(errorCode != null && errorCode == 0 ? "pending" : "failed");

                                    return existing;
                                })
                                .flatMap(repository::save)
                                .onErrorResume(e -> {
                                    System.out.println(">>> ERROR update: " + e.getMessage());
                                    existing.setStatus("failed");
                                    existing.setErrorMsg(e.getMessage());
                                    existing.setUpdatedAt(java.time.LocalDateTime.now());
                                    return repository.save(existing);
                                });
                    });
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Registro no encontrado o ya eliminado")));
    }

    @Override
    public Mono<CartoonResult> deleteCartoon(String id) {
        System.out.println(">>> DELETE (lógico) cartoon id: " + id);

        return repository.findById(id)
                .flatMap(existing -> {
                    // Verificar que no esté ya eliminado
                    if (existing.getDeleted() != null && existing.getDeleted()) {
                        return Mono.error(new RuntimeException("Registro no encontrado o ya eliminado"));
                    }
                    
                    existing.setDeleted(true);
                    existing.setUpdatedAt(java.time.LocalDateTime.now());
                    System.out.println(">>> Marcando como eliminado: " + existing.getImageName());
                    return repository.save(existing);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Registro no encontrado o ya eliminado")));
    }

    @Override
    public Mono<byte[]> downloadImage(String imageUrl) {
        System.out.println(">>> Descargando imagen: " + imageUrl);
        
        return webClient.get()
                .uri(imageUrl)
                .retrieve()
                .bodyToMono(byte[].class)
                .doOnSuccess(bytes -> System.out.println(">>> Imagen descargada: " + bytes.length + " bytes"))
                .onErrorResume(e -> {
                    System.out.println(">>> ERROR descargando imagen: " + e.getMessage());
                    return Mono.error(new RuntimeException("Error al descargar la imagen"));
                });
    }
}
