package jhon.silva.articleExtractor.Service;

import jhon.silva.articleExtractor.Model.ArticleRequest;
import jhon.silva.articleExtractor.Model.ArticleSummary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IArticleService {
    Mono<ArticleSummary> summarize(ArticleRequest request);
    Flux<ArticleSummary> getAll();
    Mono<ArticleSummary> getById(Long id);
    Flux<ArticleSummary> getByStatus(String status);
    Flux<ArticleSummary> getByLanguage(String lang);
    Mono<ArticleSummary> update(Long id, ArticleRequest request);
    Mono<Void> delete(Long id);
}