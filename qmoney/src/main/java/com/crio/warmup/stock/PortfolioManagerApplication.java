
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication{

  private static String token = "6107214edc33e6ccd9533ad6cfe48c9386f401eb";


  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
     
    return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {

    return candles.get(candles.size()-1).getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
  
      RestTemplate rt = new RestTemplate();
  
      TiingoCandle[] result = rt.getForObject(prepareUrl(trade, endDate, getToken()),TiingoCandle[].class);

      List<Candle> newresult = new ArrayList<>();

      for(TiingoCandle tc : result){
        newresult.add(tc);
      }
  
      return newresult;    
     //return Collections.emptyList();
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {

      File file = resolveFileFromResources(args[0]);
      ObjectMapper om = getObjectMapper();

      PortfolioTrade[] result = om.readValue(file, PortfolioTrade[].class);

      List<AnnualizedReturn> r = new ArrayList<>();

      LocalDate date = LocalDate.parse(args[1]);

      for(PortfolioTrade pt : result){
        List<Candle> z = fetchCandles(pt, date, getToken());
        Double closingPrice = getClosingPriceOnEndDate(z);
        Double openPrice = getOpeningPriceOnStartDate(z);
        AnnualizedReturn rr = calculateAnnualizedReturns(date, pt, openPrice, closingPrice);
        r.add(rr);
      }

      Collections.sort(r, new DescendingOrder());

      return r;

     //return Collections.emptyList();
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {

        //Double sell_value = getClosingPriceOnEndDate(fetchCandles(trade, endDate, getToken()));
        //Double buy_value = getOpeningPriceOnStartDate(fetchCandles(trade, endDate, getToken()));

        Double totalReturn = (sellPrice - buyPrice) / buyPrice; 

        //long total_num_years = ChronoUnit.DAYS.between(trade.getPurchaseDate(),endDate);

        LocalDate currentDate = trade.getPurchaseDate();

        double total_num_years = currentDate.until(endDate, ChronoUnit.DAYS)/365.24;

      //Double annualized_returns = Math.pow((1 + totalReturn),(1 / total_num_years))-1;
      Double annualized_returns= Math.pow((1 + totalReturn) , (1 / (double)total_num_years)) - 1;

      System.out.println(trade.getSymbol() +" "+ annualized_returns +" "+ (double)total_num_years);

      return new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalReturn);
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {

    File file = resolveFileFromResources(args[0]);
    ObjectMapper om = getObjectMapper();

    PortfolioTrade[] result = om.readValue(file, PortfolioTrade[].class);

    List<String> newresult = new ArrayList<>();
    for (PortfolioTrade trade : result) {
      newresult.add(trade.getSymbol());
    }

    return newresult;
  }

    public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {

      List<PortfolioTrade> symbols = readTradesFromJson(args[0]);
  
      LocalDate date = LocalDate.parse(args[1]);
  
      RestTemplate rt = new RestTemplate();
  
      List<String> result = new ArrayList<>();
  
      int cntSym = symbols.size();
  
      Double d[] = new Double[cntSym];
      String rst[] = new String[cntSym];
  
      int cnt = 0;
      for (PortfolioTrade pr : symbols) {
        // String name = pr.getSymbol();
        TiingoCandle[] tc = rt.getForObject(prepareUrl(pr, date, token), TiingoCandle[].class);
        d[cnt] = (tc[tc.length - 1].getClose());
        rst[cnt] = pr.getSymbol();
        cnt++;
      }
  
      for (int i = 0; i < d.length; i++) {
        for (int j = 0; j < d.length; j++) {
          if (d[i] < d[j]) {
            Double temp1 = d[j];
            d[j] = d[i];
            d[i] = temp1;
            String temp2 = rst[j];
            rst[j] = rst[i];
            rst[i] = temp2;
          }
        }
      }
      for (int i = 0; i < rst.length; i++) {
        result.add(rst[i]);
      }
      return result;
    }
  
    // TODO:
    // After refactor, make sure that the tests pass by using these two commands
    // ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
    // ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile

    public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
  
      File file = resolveFileFromResources(filename);
      ObjectMapper om = getObjectMapper();
  
      // PortfolioTrade[] result = om.readValue(file, PortfolioTrade[].class);
  
      List<PortfolioTrade> newresult = om.readValue(file, new TypeReference<List<PortfolioTrade>>() {
      });
  
      return newresult;
    }

    public static List<String> debugOutputs() {

      String valueOfArgument0 = "trades.json";
      String resultOfResolveFilePathArgs0 = "File@84 \"/home/crio-user/workspace/amanjeetsahay052-ME_QMONEY_V2/qmoney/bin/main/trades.json\"";
      String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@2f9f7dcf";
      String functionNameFromTestFileInStackTrace = "mainReadFile()";
      String lineNumberFromTestFileInStackTrace = "29";
  
      return Arrays.asList(new String[] { valueOfArgument0, resultOfResolveFilePathArgs0, toStringOfObjectMapper,
          functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace });
    }
  
    // TODO:
    // Build the Url using given parameters and use this function in your code to
    // cann the API.
    public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
      // return Collections.emptyList();
  
      String api = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate="
          + trade.getPurchaseDate() + "&endDate=" + endDate + "&token=" + token;
      return api;
    }

    public static String getToken() {
      return token;
    }

  //----------------------------------------------------------------------------------------------------------

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper();
       PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(new RestTemplate());
       return portfolioManager.calculateAnnualizedReturn(readTradesFromJson(file), endDate);
  }
/*public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper();
       return PortfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  } */

  private static String readFileAsString(String file) {
    return null;
  }

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}

