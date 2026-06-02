package jhon.silva.cartoon.Repository;



import jhon.silva.cartoon.Model.CartoonResult;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartoonRepository extends ReactiveMongoRepository<CartoonResult, String> {
    
    Mono<CartoonResult> findByTaskId(String taskId);
    Mono<CartoonResult> findByRequestId(String requestId);
    
    // Query que busca documentos donde deleted sea false O no exista
    @Query("{ $or: [ { 'deleted': { $exists: false } }, { 'deleted': false } ] }")
    Flux<CartoonResult> findAllNotDeleted();
    
    // Query que busca por status Y que no esté eliminado
    @Query("{ 'status': ?0, $or: [ { 'deleted': { $exists: false } }, { 'deleted': false } ] }")
    Flux<CartoonResult> findByStatusNotDeleted(String status);
    
    // Métodos legacy (por si acaso)
    Flux<CartoonResult> findByStatus(String status);
}