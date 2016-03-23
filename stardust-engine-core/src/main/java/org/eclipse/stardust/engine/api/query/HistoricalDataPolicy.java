package org.eclipse.stardust.engine.api.query;

public class HistoricalDataPolicy implements EvaluationPolicy
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   private boolean includeHistoricalData;
   
   /**
    * Includes historical data values in process instance details.
    */
   public static final HistoricalDataPolicy INCLUDE_HISTORICAL_DATA = new HistoricalDataPolicy(true);

   /**
    * Does not include historical dat values with process instance details.
    */
   public static final HistoricalDataPolicy NO_HISTORICAL_DATA = new HistoricalDataPolicy(false);
   
   public HistoricalDataPolicy(boolean includeHistoricalData)
   {
      this.includeHistoricalData = includeHistoricalData;
   }

   
}
