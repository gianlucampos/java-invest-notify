package com.github.gianlucampos.service;

//   Regra de ouro:
//    +100% de lucro (dobrou) → vende 50% das ações
//    Você recupera totalmente o capital investido.
//    O que sobra fica “só com lucro”.
//    +200% de lucro (triplicou o valor inicial) → vende o restante.

//valorInvestido = precoMedio * quantidade
//valorTotalCarteira = soma de todos valores investidos dos ativos (valorInvestido de todos)
//pesoStock = valorAtualStock / valorTotalCarteira
//Se for maior que 10% da carteira enviar email

import com.github.gianlucampos.models.Ticker;
import com.github.gianlucampos.models.TickerTypeEnum;
import com.github.gianlucampos.provider.HoldingsProvider;
import com.github.gianlucampos.repository.BrApiRepository;
import com.github.gianlucampos.repository.UsaApiRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class RuleService {

    private static final Integer WEIGHT_PERCENTAGE_TO_SELL = 10;
    private static final Integer INCOME_PLUS_VALUE_TO_SELL = 2;

    private final GmailService gmailService;
    private final HoldingsProvider holdingsProvider;
    private final UsaApiRepository usaApiRepository;
    private final BrApiRepository brApiRepository;

    public void verifyAll() {
        verifyTickersToSell(holdingsProvider.getTickerByGroup(TickerTypeEnum.STOCK), TickerTypeEnum.STOCK);
        verifyTickersToSell(holdingsProvider.getTickerByGroup(TickerTypeEnum.FII), TickerTypeEnum.FII);
        verifyTickersToSell(holdingsProvider.getTickerByGroup(TickerTypeEnum.REIT), TickerTypeEnum.REIT);
    }

    private void verifyTickersToSell(List<Ticker> tickers, TickerTypeEnum tickerGroup) {
        List<String> symbols = tickers.stream()
            .map(Ticker::getSymbol)
            .toList();

        var apiRepository = switch (tickerGroup) {
            case TickerTypeEnum.FII, TickerTypeEnum.STOCK -> brApiRepository;
            case TickerTypeEnum.REIT -> usaApiRepository;
        };

        List<Ticker> tickersWithMarketValue = apiRepository.getTickersFromList(symbols);
        arrangeTickerWithMarketValue(tickersWithMarketValue, tickers);

        BigDecimal walletPriceInvested = tickers.stream()
            .map(Ticker::getAveragePrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("GROUP: {}", tickerGroup.name());
        for (Ticker ticker : tickers) {
            double walletPercentage = getWalletPercentage(walletPriceInvested, ticker);
            double targetPrice = ticker.getBuyPrice().doubleValue() * INCOME_PLUS_VALUE_TO_SELL;

            boolean isIncomeOkToSell = ticker.getMarketPrice().doubleValue() >= targetPrice;
            boolean isWeightOkToSell = walletPercentage >= WEIGHT_PERCENTAGE_TO_SELL;

            if (isIncomeOkToSell && isWeightOkToSell) {
                gmailService.sendEmail(ticker.getSymbol(), ticker.getMarketPrice());
            }
            log.info("STOCK: {} | MARKET PRICE: {} | BUY PRICE: {} |  WALLET PERCENTAGE: {}%",
                ticker.getSymbol(), ticker.getMarketPrice(), ticker.getBuyPrice(), walletPercentage);
        }
    }

    private void arrangeTickerWithMarketValue(List<Ticker> tickersWithMarketValue, List<Ticker> reits) {
        Map<String, Ticker> marketPriceMap = tickersWithMarketValue.stream()
            .collect(Collectors.toMap(Ticker::getSymbol, Function.identity()));

        reits.forEach(ticker -> {
            Ticker withPrice = marketPriceMap.get(ticker.getSymbol());
            ticker.setMarketPrice(withPrice.getMarketPrice());
        });
    }

    private Double getWalletPercentage(BigDecimal walletPrice, Ticker ticker) {
        return ticker.getTotalPrice()
            .divide(walletPrice, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
    }
}
