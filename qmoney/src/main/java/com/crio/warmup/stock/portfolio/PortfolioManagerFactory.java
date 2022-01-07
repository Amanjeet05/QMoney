
package com.crio.warmup.stock.portfolio;

import java.time.LocalDate;
import java.util.List;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.web.client.RestTemplate;

public class PortfolioManagerFactory {

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Implement the method to return new instance of PortfolioManager.
  //  Remember, pass along the RestTemplate argument that is provided to the new instance.

  public static PortfolioManager getPortfolioManager(RestTemplate restTemplate) {

      return new PortfolioManagerImpl(restTemplate);
      
  }

}
