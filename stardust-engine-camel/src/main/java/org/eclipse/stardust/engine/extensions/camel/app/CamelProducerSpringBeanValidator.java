package org.eclipse.stardust.engine.extensions.camel.app;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CAMEL_CONTEXT_ID_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CONSUMER_ROUTE_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.INVOCATION_PATTERN_EXT_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.INVOCATION_TYPE_EXT_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.PRODUCER_ROUTE_ATT;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.getRouteId;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.stopAndRemoveRunningRoute;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidator;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidatorEx;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.util.CreateApplicationRouteAction;
import org.springframework.context.support.AbstractApplicationContext;

public class CamelProducerSpringBeanValidator implements ApplicationValidator, ApplicationValidatorEx
{

   private static final transient Logger logger = LogManager.getLogger(CamelProducerSpringBeanValidator.class);

   /**
    * Checks if the application has valid attributes (routes entries and camelContextId).
    * 
    * @param attributes
    *           The application context attributes.
    * @param typeAttributes
    *           The application type attributes.
    * @param accessPoints
    * @return A list with all found
    *         {@link org.eclipse.stardust.engine.api.model.Inconsistency} instances.
    */

   @SuppressWarnings("rawtypes")
   public List validate(Map attributes, Map typeAttributes, Iterator accessPoints)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   // @Override
   public List validate(IApplication application)
   {

      BpmRuntimeEnvironment bpmRt = PropertyLayerProviderInterceptor.getCurrent();

      if (logger.isDebugEnabled())
      {
         logger.debug("Start validation of " + application);
      }

      List inconsistencies = CollectionUtils.newList();

      String camelContextId = (String) application.getAttribute(CAMEL_CONTEXT_ID_ATT);

      // check for empty camel context ID.
      if (StringUtils.isEmpty(camelContextId))
      {
         inconsistencies.add(new Inconsistency("No camel context ID specified for application: " + application.getId(),
               application, Inconsistency.ERROR));
      }

      // check if route has been specified
      String routeDefinition = (String) application.getAttribute(PRODUCER_ROUTE_ATT);

      String invocationPattern = (String) application.getAttribute(INVOCATION_PATTERN_EXT_ATT);
      String invocationType = (String) application.getAttribute(INVOCATION_TYPE_EXT_ATT);

      if (invocationPattern == null && invocationType == null)
      {
         // backward compatiblity
         if (StringUtils.isEmpty(routeDefinition))
         {
            inconsistencies.add(new Inconsistency("No Producer route definition specified for application: "
                  + application.getId(), application, Inconsistency.ERROR));
         }
      }
      else
      {
         if (invocationPattern.equals(CamelConstants.InvocationPatterns.SEND)
               || invocationPattern.equals(CamelConstants.InvocationPatterns.SENDRECEIVE))
         {
            if (StringUtils.isEmpty(routeDefinition))
               inconsistencies.add(new Inconsistency("No Producer route definition specified for application: "
                     + application.getId(), application, Inconsistency.ERROR));
         }

         if (invocationPattern.equals(CamelConstants.InvocationPatterns.RECEIVE))
         {

            if ((String) application.getAttribute(CONSUMER_ROUTE_ATT) == null)
            {
               inconsistencies.add(new Inconsistency("No route definition specified for application: "
                     + application.getId(), application, Inconsistency.ERROR));

            }
         }
         
         if(application.getAllOutAccessPoints().hasNext() && invocationPattern.equals(CamelConstants.InvocationPatterns.SEND)){
            
            inconsistencies.add(new Inconsistency("Application "+application.getName()+" contains Out AccessPoint while the Endpoint Pattern is set to "+
                   invocationPattern, application, Inconsistency.ERROR));
          
         }
      }

      // TODO : consumer route validation

      if (inconsistencies.isEmpty())
      {

         if (logger.isDebugEnabled())
         {
            logger.debug("No inconsistencies found for application: " + application);
         }

         try
         {

            AbstractApplicationContext applicationContext = (AbstractApplicationContext) Parameters.instance().get(
                  CamelConstants.PRP_APPLICATION_CONTEXT);

            if (applicationContext != null)
            {

               String partitionId = SecurityProperties.getPartition().getId();

               CamelContext camelContext = (CamelContext) applicationContext.getBean(camelContextId);

               if (logger.isDebugEnabled())
               {
                  logger.debug("Camel Context " + camelContextId + " used.");
               }

               List<Route> routesToBeStopped = new ArrayList<Route>();

               // select routes that are running in the current partition
               for (Route runningRoute : camelContext.getRoutes())
               {
                  if (runningRoute.getId().equalsIgnoreCase(
                        getRouteId(partitionId, application.getModel().getId(), null, application.getId(), application
                              .getType().getId().equalsIgnoreCase("camelSpringProducerApplication"))))
                  {
                     routesToBeStopped.add(runningRoute);
                  }
               }

               // stop running routes to sync up with the deployed model
               for (Route runningRoute : routesToBeStopped)
               {

                  // camelContext.removeRoute(runningRoute.getId());
                  stopAndRemoveRunningRoute(camelContext, runningRoute.getId());
                  if (logger.isDebugEnabled())
                  {
                     logger.debug("Route " + runningRoute.getId() + " is removed from context " + camelContext + ".");
                  }
               }

               Action< ? > action = new CreateApplicationRouteAction(bpmRt, partitionId, applicationContext,
                     application);

               action.execute();

            }
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }

      return inconsistencies;
   }
}