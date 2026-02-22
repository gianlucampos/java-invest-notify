package com.github.gianlucampos.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.gianlucampos.exception.StockApiException;
import com.github.gianlucampos.models.Ticker;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;
import java.util.stream.StreamSupport;
import javax.naming.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BrApiRepositoryImpl implements BrApiRepository {

    private final HttpClient client;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private final String token;

    public BrApiRepositoryImpl(String baseUrl, String token) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.mapper = new ObjectMapper();
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    public List<Ticker> getTickersFromList(List<String> tickersForSearch) {
        try {
            String symbols = String.join(",", tickersForSearch);
            String tokenQuery = "?token=".concat(token);
            String formatedURL = baseUrl.concat(symbols).concat(tokenQuery);

            HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(formatedURL))
                .build();

            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("Error at calling BrApi: {}", response.body());
                throw new ServiceUnavailableException("Error at calling BrApi: " + response.body());
            }

            JsonNode root = mapper.readTree(response.body());
            JsonNode results = root.path("results");

            return StreamSupport.stream(results.spliterator(), false)
                .map(item -> Ticker.builder()
                    .symbol(item.get("symbol").asText())
                    .marketPrice(item.get("regularMarketPrice").decimalValue())
                    .build()
                ).toList();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted while retrieving stocks", ex);
            throw new StockApiException(ex);

        } catch (Exception ex) {
            log.error("Error at retrieving stocks", ex);
            throw new StockApiException(ex);
        }
    }
}
