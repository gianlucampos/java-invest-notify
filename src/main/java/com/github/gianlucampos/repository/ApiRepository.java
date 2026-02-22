package com.github.gianlucampos.repository;

import com.github.gianlucampos.models.Ticker;
import java.util.List;

public interface ApiRepository {

    List<Ticker> getTickersFromList(List<String> tickersForSearch);

}
