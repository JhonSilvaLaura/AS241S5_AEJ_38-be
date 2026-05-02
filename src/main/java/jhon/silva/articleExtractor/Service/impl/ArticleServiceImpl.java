package jhon.silva.articleExtractor.Service.impl;

import jhon.silva.articleExtractor.Model.ArticleRequest;
import jhon.silva.articleExtractor.Model.ArticleSummary;
import jhon.silva.articleExtractor.Repository.ArticleRepository;
import jhon.silva.articleExtractor.Service.IArticleService;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class ArticleServiceImpl implements IArticleService {

    private final WebClient webClient;
    private final ArticleRepository repository;

    public ArticleServiceImpl(WebClient webClient, ArticleRepository repository) {
        this.webClient = webClient;
        this.repository = repository;
    }

    @Override
    public Mono<ArticleSummary> summarize(ArticleRequest request) {

        ArticleSummary record = new ArticleSummary(
                request.getUrl(),
                null,
                request.getLang() != null ? request.getLang() : "es",
                request.getLength() != null ? request.getLength() : 3,
                "pending",
                null,
                LocalDateTime.now()
        );

        String uri = UriComponentsBuilder.fromPath("/summarize")
                .queryParam("url", request.getUrl())
                .queryParam("length", request.getLength() != null ? request.getLength() : 3)
                .queryParam("lang", request.getLang() != null ? request.getLang() : "es")
                .queryParam("engine", 1)
                .build()
                .toUriString();

        System.out.println(">>> GET: " + uri);

        return webClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(err -> {
                                    System.err.println(">>> 4xx: " + err);
                                    return Mono.error(new RuntimeException("Error 4xx: " + err));
                                })
                )
                .onStatus(status -> status.is5xxServerError(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(err -> {
                                    System.err.println(">>> 5xx: " + err);
                                    return Mono.error(new RuntimeException("Error 5xx: " + err));
                                })
                )
                .bodyToMono(java.util.Map.class)
                .map(response -> {
                    System.out.println(">>> Respuesta: " + response);
                    if (response.containsKey("summary")) {
                        record.setSummary(response.get("summary").toString());
                        record.setStatus("generated");
                        System.out.println("Resumen generado");
                    } else {
                        record.setStatus("failed");
                        record.setErrorMessage("Respuesta inesperada: " + response);
                    }
                    return record;
                })
                .flatMap(repository::save)
                .onErrorResume(e -> {
                    System.err.println(">>> ERROR: " + e.getMessage());
                    record.setStatus("failed");
                    record.setErrorMessage(e.getMessage());
                    return repository.save(record);
                });
    }

    @Override
    public Flux<ArticleSummary> getAll() { return repository.findAll(); }

    @Override
    public Mono<ArticleSummary> getById(Long id) { return repository.findById(id); }

    @Override
    public Flux<ArticleSummary> getByStatus(String status) { return repository.findByStatus(status); }

    @Override
    public Flux<ArticleSummary> getByLanguage(String lang) { return repository.findByLanguage(lang); }

    @Override
    public Mono<ArticleSummary> update(Long id, ArticleRequest request) {
        return repository.findById(id)
                .flatMap(existing -> {
                    // Actualizar campos básicos
                    existing.setUrl(request.getUrl());
                    existing.setLanguage(request.getLang() != null ? request.getLang() : "es");
                    existing.setLength(request.getLength() != null ? request.getLength() : 3);
                    existing.setStatus("pending");
                    existing.setErrorMessage(null);

                    // Construir URI para RapidAPI
                    String uri = UriComponentsBuilder.fromPath("/summarize")
                            .queryParam("url", request.getUrl())
                            .queryParam("length", existing.getLength())
                            .queryParam("lang", existing.getLanguage())
                            .queryParam("engine", 1)
                            .build()
                            .toUriString();

                    System.out.println(">>> UPDATE - GET: " + uri);

                    // Llamar a RapidAPI para regenerar el resumen
                    return webClient.get()
                            .uri(uri)
                            .retrieve()
                            .onStatus(status -> status.is4xxClientError(), response ->
                                    response.bodyToMono(String.class)
                                            .flatMap(err -> {
                                                System.err.println(">>> 4xx: " + err);
                                                return Mono.error(new RuntimeException("Error 4xx: " + err));
                                            })
                            )
                            .onStatus(status -> status.is5xxServerError(), response ->
                                    response.bodyToMono(String.class)
                                            .flatMap(err -> {
                                                System.err.println(">>> 5xx: " + err);
                                                return Mono.error(new RuntimeException("Error 5xx: " + err));
                                            })
                            )
                            .bodyToMono(java.util.Map.class)
                            .map(response -> {
                                System.out.println(">>> Respuesta UPDATE: " + response);
                                if (response.containsKey("summary")) {
                                    existing.setSummary(response.get("summary").toString());
                                    existing.setStatus("generated");
                                    System.out.println("Resumen actualizado");
                                } else {
                                    existing.setStatus("failed");
                                    existing.setErrorMessage("Respuesta inesperada: " + response);
                                }
                                return existing;
                            })
                            .flatMap(repository::save)
                            .onErrorResume(e -> {
                                System.err.println(">>> ERROR UPDATE: " + e.getMessage());
                                existing.setStatus("failed");
                                existing.setErrorMessage(e.getMessage());
                                return repository.save(existing);
                            });
                });
    }

    @Override
    public Mono<Void> delete(Long id) {
        return repository.deleteById(id);
    }
}