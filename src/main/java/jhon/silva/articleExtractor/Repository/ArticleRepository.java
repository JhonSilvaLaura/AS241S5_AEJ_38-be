package jhon.silva.articleExtractor.Repository;

import jhon.silva.articleExtractor.Model.ArticleSummary;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ArticleRepository extends ReactiveCrudRepository<ArticleSummary, Long> {
    Flux<ArticleSummary> findByStatus(String status);
    Flux<ArticleSummary> findByLanguage(String language);
}