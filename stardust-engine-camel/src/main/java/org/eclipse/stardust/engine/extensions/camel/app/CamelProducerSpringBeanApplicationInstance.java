package org.eclipse.stardust.engine.extensions.camel.app;

import static org.eclipse.stardust.engine.extensions.camel.Util.*;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.InvocationPatterns.RECEIVE;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spring.spi.ApplicationContextRegistry;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AsynchronousApplicationInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SynchronousApplicationInstance;
import org.eclipse.stardust.engine.extensions.camel.GenericProducer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class CamelProducerSpringBeanApplicationInstance
      implements SynchronousApplicationInstance, AsynchronousApplicationInstance
{ 

   public static final Logger logger = LogManager.getLogger(CamelProducerSpringBeanApplicationInstance.class
         .getCanonicalName());

   private static final String PRP_APPLICATION_CONTEXT = "org.eclipse.stardust.engine.api.spring.applicationContext";

   private ActivityInstance activityInstance;

   private ApplicationContext springContext;

   private DefaultCamelContext camelContext;

   private String camelContextId;

   private List<Pair<String, Object>> accessPointValues;

   private Application application;

   public void bootstrap(ActivityInstance activityInstance)
   {

      this.accessPointValues = new ArrayList<Pair<String, Object>>();

      this.activityInstance = activityInstance;
      this.application = activityInstance.getActivity().getApplication();
      this.camelContextId = getCamelContextId(this.application);
      this.springContext = (AbstractApplicationContext) Parameters.instance().get(PRP_APPLICATION_CONTEXT);
      this.camelContext = (DefaultCamelContext) this.springContext.getBean(this.camelContextId);

      if (this.camelContext != null && this.springContext != null)
      {
         this.camelContext.setRegistry(new ApplicationContextRegistry(springContext));
      }
      else
      {
         // TODO: What if null
      }

      if (logger.isDebugEnabled())
      {
         logger.debug("Processing request for application with ID " + this.application.getId() + ".");
         logger.debug("CamelContext: " + this.camelContextId);
      }
   }

   /**
    * Add IN access points
    * 
    * @see org.eclipse.stardust.engine.core.spi.extensions.runtime.ApplicationInstance#setInAccessPointValue(java.lang.String,
    *      java.lang.Object)
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   public void setInAccessPointValue(String s, Object obj)
   {

      Object pair = CamelMessageHelper.findAccessPointValue(s, this.accessPointValues);

      if (pair == null)
      {
         pair = new Pair(s, obj);
         accessPointValues.add((Pair<String, Object>) pair);
      }
      else
      {
         List<Pair> values = new ArrayList();
         values.add(new Pair(s, ((Pair) pair).getSecond()));
         values.add(new Pair(s, obj));
         accessPointValues.remove(s);
         accessPointValues.add((Pair<String, Object>) values);
      }
   }

   public Object getOutAccessPointValue(String outAccessPointId)
   {
      return null;
   }

   public void cleanup()
   {}

   @SuppressWarnings({"rawtypes"})
   public Map invoke(Set aSet) throws InvocationTargetException
   {

      try
      {

         Map<String, Object> outDataMappings = null;

         GenericProducer producer = new GenericProducer(this.activityInstance, this.camelContext);
         String producerMethodName = producer.getProducerMethodName();
         Method method = Reflect.decodeMethod(producer.getClass(), producerMethodName);

         Object[] inDataMappings = CamelMessageHelper.setInDataAccessPoints(method, this.application,
               this.accessPointValues);

         Exchange exchange = (Exchange) method.invoke(producer, inDataMappings);

         if (exchange != null)
         {
            if (exchange.getException() != null
                  || exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class) != null)
            {
               if (exchange.getException() instanceof org.apache.camel.RuntimeCamelException)
               {
                  throw new InvocationTargetException(exchange.getException().getCause());
               }

               Throwable caused = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);

               if (caused != null)
               {
                  throw new InvocationTargetException(caused);
               }

               throw new InvocationTargetException(exchange.getException());
            }
         }

         if (exchange != null)
         {
            if (exchange.getPattern().equals(ExchangePattern.InOnly))
            {
               outDataMappings = CamelMessageHelper.getOutDataAccessPoints(exchange.getIn(), this.activityInstance);
            }
            else
            {
               outDataMappings = CamelMessageHelper.getOutDataAccessPoints(exchange.getOut(), this.activityInstance);
            }
         }

         return outDataMappings;
      }
      catch (InvocationTargetException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new InvocationTargetException(e.getCause());
      }
   }

   public boolean isReceiving()
   {
      return true;
   }

   public boolean isSending()
   {
      return true;
   }

   @SuppressWarnings("rawtypes")
   public Map receive(Map arg0, Iterator arg1)
   {
      return null;
   }

   public void send() throws InvocationTargetException
   {

      try
      {
         String invocationPattern = getInvocationPattern(application);

         if (!RECEIVE.equals(invocationPattern))
         {
            GenericProducer producer = new GenericProducer(this.activityInstance, this.camelContext);
            String producerMethodName = producer.getProducerMethodName();
            Method method = Reflect.decodeMethod(producer.getClass(), producerMethodName);

            Object[] inDataMappings = CamelMessageHelper.setInDataAccessPoints(method, this.application,
                  this.accessPointValues);

            Exchange exchange = (Exchange) method.invoke(producer, inDataMappings);

            if (exchange != null && exchange.getException() != null)
            {
               throw exchange.getException();
            }
         }
         else
         {
            if (logger.isDebugEnabled())
            {
               logger.debug("Receive / Asnychronous - no message sent.");
            }
         }
      }
      catch (Exception e) 
      {
    	 Throwable t = e.getCause();
    	 
    	 if (t != null && t instanceof CamelExecutionException)
    	 {
    		 Exchange exchange = ((CamelExecutionException) t).getExchange();
    		 
    		 if (exchange != null && exchange.getException() != null)
    		 {
    			 throw new InvocationTargetException(exchange.getException());
    		 }
    		 else
    		 {
    			 throw new InvocationTargetException(t.getCause());
    		 }
    	 }
    	 else
    	 {
    		 throw new InvocationTargetException(e);
    	 }
      }
   }
}