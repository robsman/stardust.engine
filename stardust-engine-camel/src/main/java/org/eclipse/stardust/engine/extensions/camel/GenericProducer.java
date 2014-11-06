package org.eclipse.stardust.engine.extensions.camel;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.*;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.InvocationTypes.*;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.InvocationPatterns.*;
import static org.eclipse.stardust.engine.extensions.camel.Util.getEndpoint;
import java.util.Collections;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;

public class GenericProducer
{
   private static final Logger logger = LogManager.getLogger(GenericProducer.class.getCanonicalName());

   private ProducerTemplate template;

   private CamelContext camelContext;

   private String endpointName;

   private String producerMethodName;

   public GenericProducer(ActivityInstance activityInstance, CamelContext camelContext)
   {
      try
      {
         this.template = camelContext.createProducerTemplate();

         this.camelContext = camelContext;
         this.endpointName = DIRECT_ENDPOINT + getEndpoint(activityInstance);

         this.producerMethodName = (String) activityInstance.getActivity().getApplication()
               .getAttribute(PRODUCER_METHOD_NAME_ATT);

         if (this.producerMethodName == null)
         {
            String invocationPattern = (String) activityInstance.getActivity().getApplication()
                  .getAttribute(INVOCATION_PATTERN_EXT_ATT);

            String invocationType = (String) activityInstance.getActivity().getApplication()
                  .getAttribute(INVOCATION_TYPE_EXT_ATT);
            if (StringUtils.isEmpty(invocationPattern))
            {
               if (logger.isDebugEnabled())
                  logger.debug("Attribute " + INVOCATION_PATTERN_EXT_ATT + " is missing");
            }

            if (StringUtils.isEmpty(invocationType))
            {
               if (logger.isDebugEnabled())
                  logger.debug("Attribute " + INVOCATION_TYPE_EXT_ATT + " is missing");
            }

            if (StringUtils.isNotEmpty(invocationPattern) && StringUtils.isNotEmpty(invocationType))
            {
               if (invocationPattern.equals(SEND))
               {
                  if (invocationType.equals(SYNCHRONOUS))
                  {
                     this.producerMethodName = SEND_METHOD_WITH_HEADER;
                  }
               }
               else if (invocationPattern.equals(SENDRECEIVE))
               {
                  if (invocationType.equals(SYNCHRONOUS))
                  {
                     this.producerMethodName = SEND_RECEIVE_METHOD_WITH_HEADER;
                  }
                  else if (invocationType.equals(ASYNCHRONOUS))
                  {
                     this.producerMethodName = SEND_METHOD_WITH_HEADER;
                  }

               }
            }

            if (this.producerMethodName == null)
            {
               this.producerMethodName = SEND_RECEIVE_METHOD_WITH_HEADER;
            }

         }
         else
         {
            if (this.producerMethodName.equalsIgnoreCase(SEND_METHOD))
               this.producerMethodName = SEND_METHOD_WITH_HEADER;
         }

      }
      catch (Exception exception)
      {
         throw new PublicException(exception);
      }
   }

   public String getProducerMethodName()
   {
      return producerMethodName;
   }

   public void setProducerMethodName(String producerMethodName)
   {
      this.producerMethodName = producerMethodName;
   }

   public void setTemplate(ProducerTemplate template)
   {
      this.template = template;
   }

   public void setEndpointName(String endpointName)
   {
      this.endpointName = endpointName;
   }

   public String getEndpointName()
   {
      return endpointName;
   }

   public void setCamelContext(CamelContext camelContext)
   {
      this.camelContext = camelContext;
   }

   /**
    * send message to an endpointName
    *
    * @param message
    *           the payload
    * @throws Exception
    *            if the processing of the exchange failed
    *
    */
   public void executeMessage(Object message) throws Exception
   {
      template.sendBody(this.endpointName, message);
   }

   /**
    * send message to an endpointName
    *
    * @param message
    *           the payload
    * @throws Exception
    *            if the processing of the exchange failed
    *
    */
   public void executeMessage(Object message, Map<String, Object> headers) throws Exception
   {
      Exchange exchange = new DefaultExchange(camelContext);
      exchange.setPattern(ExchangePattern.InOut);
      CamelMessage inMessage=new CamelMessage();
      inMessage.setBody(message);
      inMessage.setHeaders(headers == null ? Collections.<String, Object> emptyMap() : headers);
      exchange.setIn(inMessage);
      template.send(endpointName, exchange);
   }

   /**
    * Sends the body to an endpoint with the specified headers values
    *
    * @param message
    *           the payload to send
    * @param headers
    *           headers
    * @return the result if {@link ExchangePattern} is OUT capable, otherwise
    *         <tt>null</tt>
    * @throws Exception
    *            if the processing of the exchange failed
    */
   public Object sendBodyInOut(Object message, Map<String, Object> headers) throws Exception
   {
      Exchange exchange = new DefaultExchange(camelContext);
      exchange.setPattern(ExchangePattern.InOut);
      CamelMessage inMessage=new CamelMessage();
      inMessage.setBody(message);
      inMessage.setHeaders(headers == null ? Collections.<String, Object> emptyMap() : headers);
      exchange.setIn(inMessage);

      return template.send(endpointName, exchange);
   }
}
