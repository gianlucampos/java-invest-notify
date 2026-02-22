package com.github.gianlucampos.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.gianlucampos.exception.HoldingsProviderException;
import com.github.gianlucampos.models.Ticker;
import com.github.gianlucampos.models.TickerTypeEnum;
import java.util.Arrays;
import java.util.List;

public class HoldingsProvider {

    private final List<Ticker> tickers;

    public HoldingsProvider(String jsonData) {
        if (jsonData == null || jsonData.isBlank()) {
            throw new HoldingsProviderException("Datasource is empty");
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            tickers = Arrays.asList(mapper.readValue(jsonData, Ticker[].class));
        } catch (JsonProcessingException e) {
            throw new HoldingsProviderException("Error at gathering data from Datasource", e);
        }
    }

    public List<Ticker> getTickerByGroup(TickerTypeEnum tickerGroup) {
        return tickers.stream()
            .filter(t -> t.getTickerType() == tickerGroup)
            .toList();
    }
}
