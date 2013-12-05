package org.eclipse.stardust.engine.extensions.camel.util;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.stardust.engine.api.query.DataFilter;
import org.eclipse.stardust.engine.api.query.Query;

public class QueryUtils
{

   /**
    * Adds the data filters in the map to the query.
    * 
    * @param data
    * @param query
    */
   public static void addDataFilters(Map<String, Serializable> data, Query query)
   {
      for (String key : data.keySet())
      {
         int index;
         if (-1 != (index = key.indexOf("#")))
         {
            String struct = key.substring(0, index);
            String path = key.substring(index + 1);
            query.where(DataFilter.isEqual(struct, path, data.get(key)));
         }
         else
         {
            query.where(DataFilter.isEqual(key, data.get(key)));
         }
      }
   }

}