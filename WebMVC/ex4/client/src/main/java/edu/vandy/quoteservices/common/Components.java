package edu.vandy.quoteservices.common;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

/**
 * This class contains a {@code Bean} annotation that can be injected
 * into classes using the Spring {@code @Autowired} annotation.
 */
@Component
public class Components {
    /**
     * This factory method returns a new {@link RestTemplate}, which
     * enables a client to perform HTTP requests synchronously.
     *
     * @return A new {@link RestTemplate}
     */
    @Bean
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate;

        var poolConnections = System.getenv("POOL_CONNECTIONS");

        // Check to see if connection pooling has been requested.
        if (poolConnections != null && poolConnections.equals("true")) {
            PoolingHttpClientConnectionManager connectionManager =
                PoolingHttpClientConnectionManagerBuilder
                    .create()
                    .setMaxConnTotal(100)
                    .setMaxConnPerRoute(20)
                    .setDefaultSocketConfig(
                        SocketConfig
                            .custom()
                            .setSoTimeout(Timeout.ofSeconds(5))
                            .build())
                    .build();

            RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(5)) // timeout
                // to get connection from pool
                .setConnectTimeout(Timeout.ofSeconds(5)) // standard
                // connection timeout
                .build();

            CloseableHttpClient httpClient = HttpClientBuilder
                .create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

            ClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

            restTemplate = new RestTemplate(requestFactory);
        } else {
            restTemplate = new RestTemplate();
        }

        restTemplate
            // Set the base URL for the RestTemplate.
            .setUriTemplateHandler(new DefaultUriBuilderFactory(Constants.GATEWAY_BASE_URL));

        // Return restTemplate.
        return restTemplate;
    }
}
