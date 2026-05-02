package jhon.silva.cartoon.Service;

import jhon.silva.cartoon.Model.CartoonResult;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICartoonService {

    Mono<CartoonResult> generateCartoon(FilePart image, String index);

    Mono<CartoonResult> checkTaskResult(String taskId);

    Flux<CartoonResult> getAllResults();

    Flux<CartoonResult> getByStatus(String status);

    Mono<CartoonResult> getById(String id);
    
    // Nuevos métodos para CRUD completo
    Mono<CartoonResult> updateCartoon(String id, FilePart image, String index);
    
    Mono<CartoonResult> deleteCartoon(String id);
    
    // Método para descargar imagen
    Mono<byte[]> downloadImage(String imageUrl);
}