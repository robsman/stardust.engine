package org.eclipse.stardust.engine.extensions.camel.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.language.simple.SimpleLanguage;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.StringHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;
import org.eclipse.stardust.engine.extensions.camel.util.data.KeyValueList;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

/**
 * This class provides reusable methods to deal with Camel concepts, like Exchange
 * objects.
 * 
 * @author JanHendrik.Scheufen
 */
public final class CamelHelper
{
   public static String extractTokenFromExpression(String input)
   {
      return input.substring(2, input.length() - 1);
   }

   private static final transient Logger LOG = LogManager.getLogger(CamelHelper.class);

   /**
    * Creates a Map object from the specified data using the specified Exchange to replace
    * values declared with Camel's Simple language. Data keys specifying Structured Data
    * Types will be converted to Map types.<br/>
    * <br/>
    * Example:<br/>
    * Data:
    * "Person.id::12345::Long,Person.name::John Smith,Person.birthday::13.07.2004::date" <br/>
    * <br/>
    * will result in the returned Map containing a <strong>single</strong> entry with the
    * key "Person" which itself is a Map<String,Object> type containing three fields (id,
    * name, birthday) with values of type java.lang.Boolean, java.lang.String, and
    * java.util.Date.
    * 
    * @param data
    * @param exchange
    * @return Map object
    */
   public static Map<String, Object> createStructuredDataMap(String data, Exchange exchange)
   {
      return createTypedMap(splitDataField(data), exchange, true);
   }

   private static String[] splitDataField(String data)
   {
      String[] elts = data.split("(?<!\\\\),");
      String[] response = elts;

      for (int i = 0; i < elts.length; i++)
      {
         response[i] = elts[i].replaceAll("\\\\,", ",");
      }

      return response;
   }
   /**
    * @see #createStructuredDataMap(String, Exchange)
    * 
    * @param data
    * @param exchange
    * @return Map object
    */
   public static Map<String, Object> createStructuredDataMap(String[] data, Exchange exchange)
   {
      return createTypedMap(data, exchange, true);
   }

   /**
    * Creates a Map object from the specified data using the specified Exchange to replace
    * values declared with Camel's Simple language. Data keys specifying Structured Data
    * Types will be left unchanged.<br/>
    * <br/>
    * Example:<br/>
    * Data:
    * "Person.id::12345::Long,Person.name::John Smith,Person.birthday::13.07.2004::date" <br/>
    * <br/>
    * will result in the returned Map containing <strong>three</strong> entries with the
    * keys Person.id, Person.name, Person.birthday and according values of type
    * java.lang.Boolean, java.lang.String, and java.util.Date.
    * 
    * @param data
    * @param exchange
    * @return
    */
   public static Map<String, Object> createFlatDataMap(String dataFilters, Exchange exchange)
   {
      return createTypedMap(dataFilters.split(","), exchange, false);
   }

   /**
    * @see #createFlatMapFromExchange(String, Exchange)
    * 
    * @param data
    * @param exchange
    * @return flat data map
    */
   public static Map<String, Object> createFlatDataMap(String[] data, Exchange exchange)
   {
      return createTypedMap(data, exchange, false);
   }

   @SuppressWarnings("unchecked")
   public static void replaceExpressionValues(Map<String, Object> keyValueMap, Exchange exchange)
   {
      for (String key : keyValueMap.keySet())
      {
         Object value = keyValueMap.get(key);
         if (value instanceof String && StringHelper.hasStartToken((String) value, "simple"))
         {
            keyValueMap.put(key, SimpleLanguage.simple((String) value).evaluate(exchange, Object.class));
         }
         else if (value instanceof Map< ? , ? >)
         {
            try
            {
               replaceExpressionValues((Map<String, Object>) keyValueMap.get(key), exchange);
            }
            catch (ClassCastException e)
            {
               // can happen since exact generic type check was not possible
               LOG.warn("ClassCastException while replacing Expression values in Map.");
            }
         }
      }
   }

   /**
    * Looks up and returns a ServiceFactory based on the availability of individual
    * credentials in the URI. When no credentials are found, the
    * ClientEnvironment.getCurrentServiceFactory() is returned.
    * 
    * NOTE: not fully implemented yet. Only supports ClientEnvironment lookup!
    * 
    * @param endpoint
    * @param exchange
    * @return a service factory
    * @throws PublicException if no ServiceFactory could be found.
    */
   public static ServiceFactory getServiceFactory(AbstractIppEndpoint endpoint, Exchange exchange)
         throws PublicException
   {
      // TODO move credential attributes of AuthenticationEndpoint to AbstractIppEndpoint
      // to implement SF retrievel based on the individual exchange
      try
      {
         return ClientEnvironment.getCurrentServiceFactory();
      }
      catch (Exception e)
      {
         LOG.error("Problems retrieving a ServiceFactory", e);
         throw new PublicException("No ServiceFactory found.", e);
      }
   }

/**
 * Retrieves data and put them into a Map
 * 
 * @param data
 * @param exchange
 * @param structured
 * @return a Map object
 */
private static Map<String, Object> createTypedMap(String[] data, Exchange exchange, boolean structured)
   {
      Map<String, Object> result = new HashMap<String, Object>();
      List<String> keyValuePairs = new ArrayList<String>();
      String nameKey;
      String remainder = null;

      for (String entry : data)
      {
         // check if the entry has a named key, i.e. at least one PARTS_SEPARATOR
         int idx = entry.indexOf(KeyValueList.PARTS_SEPARATOR);
         if (idx != -1)
         {
            nameKey = entry.substring(0, idx);
            remainder = entry.substring(idx + KeyValueList.PARTS_SEPARATOR.length());
            if (StringUtils.isEmpty(remainder))
            {
               throw new IllegalArgumentException("The entry's format is illegal. It must not end with "
                     + "a reserved separator '" + KeyValueList.PARTS_SEPARATOR + "': " + entry);
            }
            
            // an expression that is not a SDT and does not have conversion instructions can be added directly
            if( StringHelper.hasStartToken(remainder, "simple") &&
                nameKey.indexOf(KeyValueList.STRUCT_PATH_DELIMITER) == -1 &&
                remainder.indexOf(KeyValueList.PARTS_SEPARATOR) == -1)
            {
               evaluateAndAddToResult(remainder, exchange, result, nameKey);
            }
            // otherwise it goes through the KeyValueList first and any expressions are evaluated later
            else
            {
               if( StringHelper.hasStartToken(remainder, "simple") && remainder.indexOf(KeyValueList.PARTS_SEPARATOR) != -1 )
               {
                  LOG.warn("A Camel expression was detected with an additional conversion instruction: "+remainder+
                        ". It is recommended to use Camel conversions instead! E.g. ${headerAs(headerKey,java.lang.Integer}");
                  Object key = SimpleLanguage.simple(nameKey).evaluate(exchange, Object.class);
                  Object value = SimpleLanguage.simple(remainder).evaluate(exchange, Object.class);
                  keyValuePairs.add(key+KeyValueList.PARTS_SEPARATOR+value);
               }
               else
               {
            	   if( StringHelper.hasStartToken(nameKey, "simple"))
            	   {
            		   Object key = SimpleLanguage.simple(nameKey).evaluate(exchange, Object.class);
            		   Object value = SimpleLanguage.simple(remainder).evaluate(exchange, Object.class);
            		   keyValuePairs.add(key+KeyValueList.PARTS_SEPARATOR+value);
            	   }
            	   else
            		   keyValuePairs.add(entry);
               }
            }
         }
         // if there is no named key, the entry might contain an expression
         // from which a nameKey can be derived
         else if (StringHelper.hasStartToken(entry, "simple"))
         {
            // Try to determine the name key from the simple expression
            // which should correspond to the header key, for example
            String simple = ObjectHelper.between(entry, "${", "}");
            if (null == simple)
               simple = ObjectHelper.between(entry, "$simple{", "}");
            if (null != simple)
            {
               idx = simple.lastIndexOf(".");
               if (idx >= 0)
                  nameKey = simple.substring(idx + 1);
               else
                  nameKey = simple;
               
               evaluateAndAddToResult(entry, exchange, result, nameKey);
            }
            else
            {
               throw new IllegalArgumentException("Unable to derive a named key from the expression '"+entry+"'! The entry will be ignored!");
            }
         }
         else
         {
            throw new IllegalArgumentException("Unable to parse entry '"+entry+"'! The entry will be ignored!");
         }
      }

      // merge expressions and key-value-pairs
      if (keyValuePairs.size() > 0)
      {
         Map<String, Object> keyValueMap;
         if (structured)
         {
            // convert to KeyValueList typed map for structured types
            keyValueMap = new KeyValueList(keyValuePairs).getTypedMap();
         }
         else
         {
            // use flat nameKey and process values individually for flat map
            keyValueMap = new HashMap<String, Object>(keyValuePairs.size());
            
            // TODO switch to pattern in KeyValueList
            final Pattern separatorPattern = Pattern.compile(KeyValueList.PARTS_SEPARATOR); 
            String[] values;
            for (String entry : keyValuePairs)
            {
               values = separatorPattern.split(entry);
               // we know at this point that there is at least one parts separator,
               // otherwise the entry would not have been in the list.
               if (values.length > 2)
                  keyValueMap.put(values[0], KeyValueList.createTypedValue(values[2], values[1]));
               else
                  keyValueMap.put(values[0], KeyValueList.createTypedValue(null, values[1]));
            }
         }

         replaceExpressionValues(keyValueMap, exchange);
         // TODO check if anything would be overwritten before putAll call
         result.putAll(keyValueMap);
      }

      return result;
   }

   /**
    * Evaluates the given entry using the Exchange and adds the result to the Map under the specified nameKey.
    * 
    * @param entry
    * @param exchange
    * @param result
    * @param nameKey
    */
   private static void evaluateAndAddToResult(String entry, Exchange exchange, Map<String, Object> resultMap, String nameKey)
   {
      addToResult(resultMap, nameKey, SimpleLanguage.simple(entry).evaluate(exchange, Object.class));
   }

   /**
    * Addes the object to the Map under the specified nameKey. In case the key exists already, a warning will be issues.
    * 
    * @param resultMap
    * @param nameKey
    * @param value
    */
   private static void addToResult(Map<String, Object> resultMap, String nameKey, Object value)
   {
      if( resultMap.containsKey(nameKey) ) {
         LOG.warn("Key: "+nameKey+" already exists in result Map! Current value '"+resultMap.get(nameKey)+
                  "' will be replaced with new value '"+value+"'.");
      }
      resultMap.put(nameKey, value);
   }
}
