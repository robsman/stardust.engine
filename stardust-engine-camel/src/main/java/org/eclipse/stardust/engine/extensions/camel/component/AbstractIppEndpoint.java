package org.eclipse.stardust.engine.extensions.camel.component;

import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.*;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.EXPECTED_RESULT_SIZE;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_ID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCE_OID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCE_PROPERTIES;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.impl.DefaultEndpoint;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

/**
 * Abstract parent class for all IPP endpoints to provide shared fields and functionality.
 * 
 * @author JanHendrik.Scheufen
 */
public abstract class AbstractIppEndpoint extends DefaultEndpoint
{
   private static final transient Logger LOG = LogManager.getLogger(ProcessEndpoint.class);

   protected String subCommand;

   // attributes shared by more than one IPP endpoint
   protected Expression processId;

   protected Expression processInstanceOid;

   protected String dataOutput;

   protected Expression dataOutputMap;

   protected String properties;

   protected Expression propertiesMap;

   protected String dataFilters;

   protected Expression dataFiltersMap;

   protected Long expectedResultSize;
   /**
    * @param uri
    * @param component
    */
   public AbstractIppEndpoint(String uri, IppComponent component)
   {
      super(uri, component);
   }

   /**
    * The default singleton behavior is true in order to use the endpoint cache. The
    * requirement is that every endpoint with the same URI must behave in exactly the same
    * way and does not change state after being used, so that it would effect its being
    * used again.
    * 
    * @return whether this endpoint can be reused for the same URI
    */
   public boolean isSingleton()
   {
      return true;
   }

   /**
    * Returns the subcommand with which this endpoint is initiated.
    * 
    * @return subCommand
    */
   public String getSubCommand()
   {
      return subCommand;
   }

   /**
    * Sets the subcommand for this endpoint. See extending classes for possible
    * subcommand.
    * 
    * @param subCommand
    */
   public void setSubCommand(String subCommand)
   {
      this.subCommand = subCommand;
   }

   /**
    * @param processInstanceOid
    *           the Oid of process Instance
    */
   public void setProcessInstanceOid(String processInstanceOid)
   {
      this.processInstanceOid =    parseSimpleExpression(processInstanceOid);
   }

   /**
    * @param processId
    *           the Id of process
    */
   public void setProcessId(String processId)
   {
      this.processId =    parseSimpleExpression(processId);
   }

   /**
    * @param dataOutput
    *           the data output
    */
   public void setDataOutput(String dataOutput)
   {
      this.dataOutput = dataOutput;
   }

   /**
    * @return properties
    */
   public String getProperties()
   {
      return properties;
   }

   /**
    * @param properties
    */
   public void setProperties(String properties)
   {
      this.properties = properties;
   }

   /**
    * @param dataFilters
    */
   public void setDataFilters(String dataFilters)
   {
      this.dataFilters = dataFilters;
   }

   /**
    * @param dataOutputMap
    */
   public void setDataOutputMap(String dataOutputMap)
   {
      this.dataOutputMap =    parseSimpleExpression(dataOutputMap);
   }

   /**
    * @param propertiesMap
    */
   public void setPropertiesMap(String propertiesMap)
   {
      this.propertiesMap =    parseSimpleExpression(propertiesMap);
   }

   /**
    * @param dataFiltersMap
    */
   public void setDataFiltersMap(String dataFiltersMap)
   {
      this.dataFiltersMap=    parseSimpleExpression(dataFiltersMap);
   }

   /**
    * Returns the Oid value of the process instance
    * 
    * @param exchange
    *           the camel exchange
    * @param strict
    *           (boolean)
    * @return the value of the ProcessInstanceOid attribute
    */
   public Long evaluateProcessInstanceOid(Exchange exchange, boolean strict)
   {
      if (null != this.processInstanceOid)
         return this.processInstanceOid.evaluate(exchange, Long.class);
      else
      {
         Long piOid = exchange.getIn().getHeader(PROCESS_INSTANCE_OID, Long.class);
         if (null == piOid && strict)
         {
            throw new IllegalStateException("Missing required process instance OID.");
         }
         return piOid;
      }
   }

   /**
    * Returns the Id value of the process instance
    * 
    * @param exchange
    *           the camel exchange
    * @param strict
    *           (boolean)
    * @return the value of the ProcessId attribute
    */
   public String evaluateProcessId(Exchange exchange, boolean strict)
   {
      if (null != this.processId)
         return this.processId.evaluate(exchange, String.class);
      else
      {
         String id = exchange.getIn().getHeader(PROCESS_ID, String.class);
         if (StringUtils.isEmpty(id) && strict)
         {
            throw new IllegalStateException("Missing required process ID.");
         }
         return id;
      }
   }

   /**
    * Returns a Map of DataOutputs
    * 
    * @param exchange
    * @return a Map of DataOutput
    */
   @SuppressWarnings("unchecked")
   public Map<String, ? > evaluateDataOutput(Exchange exchange)
   {
      if (null != this.dataOutputMap)
      {
         return this.dataOutputMap.evaluate(exchange, Map.class);
      }
      if (StringUtils.isNotEmpty(this.dataOutput))
      {
         return CamelHelper.createStructuredDataMap(this.dataOutput, exchange);
      }
      return null;
   }

   /**
    * Returns the value of the properties on the given exchange
    * 
    * @param exchange
    *           the message exchange on which to evaluate the properties
    * @param strict
    *           if true, throws exception if properties not found
    * @return the value of process instance properties as a map
    */
   @SuppressWarnings("unchecked")
   protected Map<String, ? > evaluateProperties(Exchange exchange, boolean strict)
   {
      Map<String, ? > props = null;
      if (null != this.propertiesMap)
      {
         props = this.propertiesMap.evaluate(exchange, Map.class);
      }
      else if (StringUtils.isNotEmpty(this.properties))
      {
         props = CamelHelper.createStructuredDataMap(this.properties, exchange);
      }
      else
      {
         props = exchange.getIn().getHeader(PROCESS_INSTANCE_PROPERTIES, Map.class);
      }
      if (null == props && strict)
      {
         throw new IllegalStateException("Missing required process instance properties.");
      }
      return props;
   }

   /**
    * Returns the value of the filters on the given exchange
    * 
    * @param exchange
    *           the message exchange on which to evaluate the filters
    * @param strict
    *           if true, throws exception if the filters are not found
    * @return the value of process instance filters as a map
    */
   @SuppressWarnings("unchecked")
   public Map<String, Serializable> evaluateDataFilters(Exchange exchange, boolean strict)
   {
      Map<String, Serializable> filters = null;
      if (null != this.dataFiltersMap)
      {
         filters = this.dataFiltersMap.evaluate(exchange, Map.class);
      }
      else if (StringUtils.isNotEmpty(this.dataFilters))
      {
         Map<String, Object> typedMap = CamelHelper.createFlatDataMap(dataFilters, exchange);
         if (typedMap.size() > 0)
         {
            filters = new HashMap<String, Serializable>();
            Object value = null;
            for (String key : typedMap.keySet())
            {
               value = typedMap.get(key);
               if (value instanceof Serializable)
                  filters.put(key, (Serializable) value);
               else
                  LOG.warn("Detected value in data filter that does not implement java.io.Serializable and will be ignored! Data: "
                        + key + ", Value: " + value);
            }
         }
      }

      if (null == filters && strict)
      {
         throw new IllegalStateException("Missing required process instance properties.");
      }
      return filters;
   }
   
   /**
    * Returns the value of ExpectedResultSize on the given exchange
    * 
    * @param exchange
    * @param strict
    *           flag
    * @return ExpectedResultSize
    */
   public Long evaluateExpectedResultSize(Exchange exchange, boolean strict)
   {
      if (null != this.expectedResultSize)
      {
         return this.expectedResultSize;
      }
      else
      {
         Long expectedResultSize = exchange.getIn().getHeader(EXPECTED_RESULT_SIZE, Long.class);
         return expectedResultSize;
      }
   }

   public Long getExpectedResultSize()
   {
      return expectedResultSize;
   }

   public void setExpectedResultSize(Long expectedResultSize)
   {
      this.expectedResultSize = expectedResultSize;
   }
}
