package com.github.gianlucampos.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.gianlucampos.exception.StockApiException;
import com.github.gianlucampos.models.Ticker;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;
import javax.naming.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UsaApiRepositoryImpl implements UsaApiRepository {

    private final HttpClient client;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private final String token;


    public UsaApiRepositoryImpl(String baseUrl, String token) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.mapper = new ObjectMapper();
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    public List<Ticker> getTickersFromList(List<String> tickersForSearch) {
        return tickersForSearch.parallelStream()
            .map(symbol -> {
                try {
                    String symbolQuery = "?symbol=".concat(symbol);
                    String tokenQuery = "&token=".concat(token);
                    String formatedURL = baseUrl.concat(symbolQuery).concat(tokenQuery);

                    var request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(formatedURL))
                        .build();

                    var response = client.send(request, BodyHandlers.ofString());
                    if (response.statusCode() != 200) {
                        log.error("Error at calling UsaApi: {}", response.body());
                        throw new ServiceUnavailableException("Error at calling UsaApi: " + response.body());
                    }
                    var root = mapper.readTree(response.body());

                    return Ticker.builder()
                        .symbol(symbol)
                        .marketPrice(BigDecimal.valueOf(root.get("c").asDouble()))
                        .build();

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    log.error("Thread interrupted while retrieving stocks", ex);
                    throw new StockApiException(ex);

                } catch (Exception ex) {
                    log.error("Error retrieving stocks: {}", symbol, ex);
                    throw new StockApiException(ex);
                }
            })
            .toList();
    }
}
