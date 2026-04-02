package jhon.silva.cartoon.Repository;



import jhon.silva.cartoon.Model.CartoonResult;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartoonRepository extends ReactiveMongoRepository<CartoonResult, String> {
    Flux<CartoonResult> findByStatus(String status);
    Mono<CartoonResult> findByTaskId(String taskId); // ✅ nuevo
    Mono<CartoonResult> findByRequestId(String requestId); // ✅ nuevo
}