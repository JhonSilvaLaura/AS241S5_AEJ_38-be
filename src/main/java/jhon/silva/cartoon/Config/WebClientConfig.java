package jhon.silva.cartoon.Config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Value("${rapidapi.base-url}")
    private String baseUrl;

    @Value("${rapidapi.key}")
    private String apiKey;

    @Value("${rapidapi.host}")
    private String apiHost;

    @Bean
    public WebClient cartoonWebClient() {
        // Configurar estrategias de intercambio con límite de buffer aumentado (10MB)
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();

        // Configurar HttpClient con timeouts aumentados
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(java.time.Duration.ofSeconds(30));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-rapidapi-key", apiKey)
                .defaultHeader("x-rapidapi-host", apiHost)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }
}