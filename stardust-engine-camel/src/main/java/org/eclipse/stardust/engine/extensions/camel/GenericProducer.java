package org.eclipse.stardust.engine.extensions.camel;

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
   public static final Logger logger = LogManager.getLogger(GenericProducer.class
         .getCanonicalName());
   public static final String SEND_METHOD = "executeMessage(java.lang.Object)"; //$NON-NLS-1$
   public static final String SEND_METHOD_WITH_HEADER = "executeMessage(java.lang.Object,java.util.Map<java.lang.String,java.lang.Object>)"; //$NON-NLS-1$
   public static final String SEND_RECEIVE_METHOD_WITH_HEADER = "sendBodyInOut(java.lang.Object,java.util.Map<java.lang.String,java.lang.Object>)"; //$NON-NLS-1$

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
         this.endpointName = "direct://" + activityInstance.getActivity().getApplication().getId();

         this.producerMethodName = (String) activityInstance.getActivity().getApplication()
            .getAttribute(CamelConstants.PRODUCER_METHOD_NAME_ATT);
         
         if (this.producerMethodName == null)
         {
        	 String invocationPattern = (String) activityInstance.getActivity().getApplication()
             	.getAttribute(CamelConstants.INVOCATION_PATTERN_EXT_ATT);
        	 
        	 String invocationType = (String) activityInstance.getActivity().getApplication()
          		.getAttribute(CamelConstants.INVOCATION_TYPE_EXT_ATT);
        	if (StringUtils.isEmpty(invocationPattern) )
         {
        	   logger.debug("Attribute "+CamelConstants.INVOCATION_PATTERN_EXT_ATT+" is missing");
         }
        	
         if (StringUtils.isEmpty(invocationType) )
         {
            logger.debug("Attribute "+CamelConstants.INVOCATION_TYPE_EXT_ATT+" is missing");
         }
        	 
        	 
        	 if (StringUtils.isNotEmpty(invocationPattern) && StringUtils.isNotEmpty(invocationType))
        	 {
        		 if (invocationPattern.equals(CamelConstants.InvocationPatterns.SEND))
        		 {
        			 if (invocationType.equals(CamelConstants.InvocationTypes.SYNCHRONOUS))
        			 {
        				 this.producerMethodName = SEND_METHOD_WITH_HEADER;
        			 }
        		 }
        		 else if (invocationPattern.equals(CamelConstants.InvocationPatterns.SENDRECEIVE))
        		 {
        			 if (invocationType.equals(CamelConstants.InvocationTypes.SYNCHRONOUS))
        			 {
        				 this.producerMethodName = SEND_RECEIVE_METHOD_WITH_HEADER;
        			 }
        			 else if (invocationType.equals(CamelConstants.InvocationTypes.ASYNCHRONOUS))
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
      template.sendBodyAndHeaders(this.endpointName, message, headers == null
            ? Collections.<String, Object> emptyMap()
            : headers);
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
      exchange.getIn().setBody(message);
      exchange.getIn().setHeaders(headers == null ? Collections.<String, Object> emptyMap() : headers);

      return template.send(endpointName, exchange);
   }
}
