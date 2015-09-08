package org.eclipse.stardust.engine.extensions.camel.producer;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.DIRECT_ENDPOINT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.INVOCATION_PATTERN_EXT_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.INVOCATION_TYPE_EXT_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.PRODUCER_METHOD_NAME_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SEND_METHOD;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.InvocationPatterns.SEND;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.InvocationPatterns.SENDRECEIVE;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.InvocationTypes.ASYNCHRONOUS;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.InvocationTypes.SYNCHRONOUS;
import static org.eclipse.stardust.engine.extensions.camel.Util.getCamelContextId;
import static org.eclipse.stardust.engine.extensions.camel.Util.getEndpoint;

import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.spring.spi.ApplicationContextRegistry;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class ApplicationProducerFactory
{
   private static final String PRP_APPLICATION_CONTEXT = "org.eclipse.stardust.engine.api.spring.applicationContext";
   private static final Logger logger = LogManager.getLogger(ApplicationProducerFactory.class.getCanonicalName());
   
   private ApplicationProducerFactory()
   {
   }
   /**
    * 
    * @param application
    * @return
    */
   public static CamelProducer getProducer(Application application){
      String endpointUri = DIRECT_ENDPOINT + getEndpoint(application);
      String camelContextId = getCamelContextId(application);
      
      ApplicationContext springContext=(AbstractApplicationContext) Parameters.instance().get(PRP_APPLICATION_CONTEXT);
      ModelCamelContext camelContext = (DefaultCamelContext) springContext.getBean(camelContextId);
      if (camelContext != null && springContext != null)
       {
          ((DefaultCamelContext)camelContext).setRegistry(new ApplicationContextRegistry(springContext));
       }
       else
       {
          // TODO: What if null
       }

       if (logger.isDebugEnabled())
       {
          logger.debug("Processing request for application with ID " + application.getId() + ".");
          logger.debug("CamelContext: " + camelContextId);
       }
      
      String producerMethodName = (String) application.getAttribute(PRODUCER_METHOD_NAME_ATT);

      if (producerMethodName == null)
      {
         String invocationPattern = (String) application
               .getAttribute(INVOCATION_PATTERN_EXT_ATT);

         String invocationType = (String) application.getAttribute(INVOCATION_TYPE_EXT_ATT);
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
                  //this.producerMethodName = SEND_METHOD_WITH_HEADER;
                  return new InOnlyProducer(endpointUri, camelContext);
               }
            }
            else if (invocationPattern.equals(SENDRECEIVE))
            {
               if (invocationType.equals(SYNCHRONOUS))
               {
                 // this.producerMethodName = SEND_RECEIVE_METHOD_WITH_HEADER;
                  return new InOutProducer(endpointUri, camelContext);
               }
               else if (invocationType.equals(ASYNCHRONOUS))
               {
                 // this.producerMethodName = SEND_METHOD_WITH_HEADER;
                  return new InOnlyProducer(endpointUri, camelContext);
               }

            }
         }

         if (producerMethodName == null)
         {
            //this.producerMethodName = SEND_RECEIVE_METHOD_WITH_HEADER;
            return new InOutProducer(endpointUri, camelContext);
         }

      }
      else
      {
         if (producerMethodName.equalsIgnoreCase(SEND_METHOD))
           // this.producerMethodName = SEND_METHOD_WITH_HEADER;
            return new InOnlyProducer(endpointUri, camelContext);
      }
      
      return null;
   }
   /**
    * 
    * @param application
    * @return
    */
   public static CamelProducer getProducer(IApplication application, String uri){
      String endpointUri =null;
      if(StringUtils.isNotEmpty(uri))
         endpointUri=uri;

      String camelContextId = getCamelContextId(application);
      
      ApplicationContext springContext=(AbstractApplicationContext) Parameters.instance().get(PRP_APPLICATION_CONTEXT);
      ModelCamelContext camelContext = (DefaultCamelContext) springContext.getBean(camelContextId);
      if (camelContext != null && springContext != null)
       {
          ((DefaultCamelContext)camelContext).setRegistry(new ApplicationContextRegistry(springContext));
       }
       else
       {
          // TODO: What if null
       }

       if (logger.isDebugEnabled())
       {
          logger.debug("Processing request for application with ID " + application.getId() + ".");
          logger.debug("CamelContext: " + camelContextId);
       }
      
      String producerMethodName = (String) application.getAttribute(PRODUCER_METHOD_NAME_ATT);

      if (producerMethodName == null)
      {
         String invocationPattern = (String) application
               .getAttribute(INVOCATION_PATTERN_EXT_ATT);

         String invocationType = (String) application.getAttribute(INVOCATION_TYPE_EXT_ATT);
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
                  //this.producerMethodName = SEND_METHOD_WITH_HEADER;
                  return new InOnlyProducer(endpointUri, camelContext);
               }
            }
            else if (invocationPattern.equals(SENDRECEIVE))
            {
               if (invocationType.equals(SYNCHRONOUS))
               {
                 // this.producerMethodName = SEND_RECEIVE_METHOD_WITH_HEADER;
                  return new InOutProducer(endpointUri, camelContext);
               }
               else if (invocationType.equals(ASYNCHRONOUS))
               {
                 // this.producerMethodName = SEND_METHOD_WITH_HEADER;
                  return new InOnlyProducer(endpointUri, camelContext);
               }

            }
         }

         if (producerMethodName == null)
         {
            //this.producerMethodName = SEND_RECEIVE_METHOD_WITH_HEADER;
            return new InOutProducer(endpointUri, camelContext);
         }

      }
      else
      {
         if (producerMethodName.equalsIgnoreCase(SEND_METHOD))
           // this.producerMethodName = SEND_METHOD_WITH_HEADER;
            return new InOnlyProducer(endpointUri, camelContext);
      }
      
      return null;
   }
}
