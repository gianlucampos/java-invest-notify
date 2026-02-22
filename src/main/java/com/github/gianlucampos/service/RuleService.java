package com.github.gianlucampos.service;

//    Pegar ação da base de dados (planilha ou json)
//    Buscar preço da ação na api (brapi ou stockapi)
//    Agrupar ação que está com valor acima do preço medio

//   Regra de ouro:
//    +100% de lucro (dobrou) → vende 50% das ações
//    Você recupera totalmente o capital investido.
//    O que sobra fica “só com lucro”.
//    +200% de lucro (triplicou o valor inicial) → vende o restante.

//valorInvestido = precoMedio * quantidade
//valorTotalCarteira = soma de todos valores investidos dos ativos (valorInvestido de todos)
//pesoEQIX = valorAtualEQIX / valorTotalCarteira
//Se for maior que 10% da carteira enviar email

import com.github.gianlucampos.models.Ticker;
import com.github.gianlucampos.provider.HoldingsProvider;
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

    public void verifyTickersToSell() {
        List<Ticker> reits = holdingsProvider.reits();
        List<String> symbols = reits.stream()
            .map(Ticker::getSymbol)
            .toList();

        List<Ticker> tickersWithMarketValue = usaApiRepository.getTickersFromList(symbols);
        arrangeTickerWithMakertValue(tickersWithMarketValue, reits);

        BigDecimal walletPriceInvested = reits.stream()
            .map(Ticker::getAveragePrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (Ticker ticker : reits) {
            double walletPercentage = getWalletPercentage(walletPriceInvested, ticker);
            double targetPrice = ticker.getBuyPrice().doubleValue() * INCOME_PLUS_VALUE_TO_SELL;

            boolean isIncomeOkToSell = ticker.getMarketPrice().doubleValue() >= targetPrice;
            boolean isWeightOkToSell = walletPercentage >= WEIGHT_PERCENTAGE_TO_SELL;

            if (isIncomeOkToSell && isWeightOkToSell) {
                gmailService.sendEmail(ticker.getSymbol(), ticker.getMarketPrice());
            }
            log.info("\nSTOCK: {} | MARKET PRICE: {} | BUY PRICE: {} |  WALLET PERCENTAGE: {}%\n",
                ticker.getSymbol(), ticker.getMarketPrice(), ticker.getBuyPrice(), walletPercentage);
        }
    }

    private void arrangeTickerWithMakertValue(List<Ticker> tickersWithMarketValue, List<Ticker> reits) {
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
