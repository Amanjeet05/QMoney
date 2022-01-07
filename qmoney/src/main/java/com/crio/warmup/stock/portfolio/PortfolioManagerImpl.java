
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {

  private RestTemplate restTemplate;

  private String APIKEY = "6107214edc33e6ccd9533ad6cfe48c9386f401eb";

// Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {

        RestTemplate rt = new RestTemplate();
        TiingoCandle[] tc = rt.getForObject(buildUri(symbol, from, to), TiingoCandle[].class);

        List<Candle> result = new ArrayList<>();
        
        for (int i = 0; i < tc.length; i++) {
          result.add(tc[i]);
        }
      return result;
  }

    protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
       String uriTemplate = "https:api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
            + "startDate=$STARTDATE&endDate=$ENDDATE&token="+APIKEY;
      return uriTemplate;
  }

  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
     
    return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {

    return candles.get(candles.size()-1).getClose();
  }

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades, LocalDate endDate)
      throws JsonProcessingException {
    // TODO Auto-generated method stub

    List<AnnualizedReturn> results = new ArrayList<>();
    for(PortfolioTrade trade : portfolioTrades){

      List<Candle> result = getStockQuote(trade.getSymbol(), trade.getPurchaseDate(), endDate);

      Double sellPrice = getClosingPriceOnEndDate(result);

      Double buyPrice = getOpeningPriceOnStartDate(result);

      Double totalReturn = (sellPrice - buyPrice) / buyPrice; 

        //long total_num_years = ChronoUnit.DAYS.between(trade.getPurchaseDate(),endDate);

        LocalDate currentDate = trade.getPurchaseDate();

        double total_num_years = currentDate.until(endDate, ChronoUnit.DAYS)/365.24;

      //Double annualized_returns = Math.pow((1 + totalReturn),(1 / total_num_years))-1;
      Double annualized_returns= Math.pow((1 + totalReturn) , (1 / (double)total_num_years)) - 1;

      System.out.println(trade.getSymbol() +" "+ annualized_returns +" "+ (double)total_num_years);

      results.add(new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalReturn));

    }
    Collections.sort(results, getComparator());
    return results;
  }
}
