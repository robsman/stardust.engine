package org.eclipse.stardust.engine.api.query;

/**
 * 
 * @author Thomas.Wolfram
 *
 */
public class HistoricalDataPolicy implements EvaluationPolicy
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   private boolean includeHistoricalData = false;
   
   /**
    * Includes historical data values in process instance details.
    */
   public static final HistoricalDataPolicy INCLUDE_HISTORICAL_DATA = new HistoricalDataPolicy(true);

   /**
    * Does not include historical data values with process instance details.
    */
   public static final HistoricalDataPolicy NO_HISTORICAL_DATA = new HistoricalDataPolicy(false);
   
   public HistoricalDataPolicy(boolean includeHistoricalData)
   {
      this.includeHistoricalData = includeHistoricalData;
   }

   public boolean isIncludeHistoricalData()
   {
      return includeHistoricalData;
   }

   
}
