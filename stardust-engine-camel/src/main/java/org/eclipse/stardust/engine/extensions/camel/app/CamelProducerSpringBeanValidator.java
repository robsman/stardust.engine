package org.eclipse.stardust.engine.extensions.camel.app;

import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.removeRouteDefinitionWithoutRunningRoute;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.stopAndRemoveRunningRoute;
import static org.eclipse.stardust.engine.extensions.camel.Util.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.Route;
import org.apache.camel.model.ModelCamelContext;
import org.springframework.context.support.AbstractApplicationContext;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidator;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidatorEx;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.util.CreateApplicationRouteAction;

public class CamelProducerSpringBeanValidator implements ApplicationValidator, ApplicationValidatorEx
{

   private static final transient Logger logger = LogManager.getLogger(CamelProducerSpringBeanValidator.class);
   private ModelCamelContext camelContext;
   private String routeId;
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
   @SuppressWarnings({"unchecked", "rawtypes"})
   public List validate(IApplication application)
   {

      BpmRuntimeEnvironment bpmRt = PropertyLayerProviderInterceptor.getCurrent();

      if (logger.isDebugEnabled())
      {
         logger.debug("Start validation of " + application);
      }

      List inconsistencies = CollectionUtils.newList();

      String camelContextId = getCamelContextId(application);

      // check for empty camel context ID.
      if (StringUtils.isEmpty(camelContextId))
      {
         inconsistencies.add(new Inconsistency("No camel context ID specified for application: " + application.getId(),
               application, Inconsistency.ERROR));
      }


      String invocationPattern = getInvocationPattern(application);
      String invocationType = getInvocationType(application);

      if (invocationPattern == null && invocationType == null)
      {
         // backward compatiblity
         if (StringUtils.isEmpty(getProducerRouteConfiguration(application)))
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
            if (StringUtils.isEmpty(getProducerRouteConfiguration(application)))
               inconsistencies.add(new Inconsistency("No Producer route definition specified for application: "
                     + application.getId(), application, Inconsistency.ERROR));
         }

         if (invocationPattern.equals(CamelConstants.InvocationPatterns.RECEIVE))
         {

            if (getConsumerRouteConfiguration(application) == null)
            {
               inconsistencies.add(new Inconsistency("No route definition specified for application: "
                     + application.getId(), application, Inconsistency.ERROR));

            }
         }

         if (application.getAllOutAccessPoints().hasNext()
               && invocationPattern.equals(CamelConstants.InvocationPatterns.SEND))
         {

            inconsistencies.add(new Inconsistency("Application " + application.getName()
                  + " contains Out AccessPoint while the Endpoint Pattern is set to " + invocationPattern, application,
                  Inconsistency.ERROR));

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

            if (applicationContext != null && bpmRt != null && bpmRt.getModelManager() != null)
            {

	            IModel model = (IModel) application.getModel();

	            IModel activeModel = bpmRt.getModelManager().findActiveModel(model.getId());

	            // only start the contained routes if this model (the one being validated) is
	            // intended to be active (aka it has the same model OID as the currently
	            // active model with the same ID or is the first version to be deployed (model
	            // OID is 0))
	            if (model.getModelOID() == 0 || model.getModelOID() == activeModel.getModelOID())
	            {
	               String partitionId = SecurityProperties.getPartition().getId();
	               routeId=getRouteId(partitionId, application.getModel().getId(), null, application.getId(), isProducerApplication(application));
	                camelContext = (ModelCamelContext) applicationContext.getBean(camelContextId);

	               if (logger.isDebugEnabled())
	               {
	                  logger.debug("Camel Context " + camelContextId + " used.");
	               }

	               List<Route> routesToBeStopped = new ArrayList<Route>();

	               // select routes that are running in the current partition
	               for (Route runningRoute : camelContext.getRoutes())
	               {
	                  if (runningRoute.getId().equalsIgnoreCase(routeId))
	                  {
	                     routesToBeStopped.add(runningRoute);
	                  }
	               }

	               // stop running routes to sync up with the deployed model
	               for (Route runningRoute : routesToBeStopped)
	               {
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
         }
         catch (Exception e)
         {//using e.getCause() since e is RTE thrown by the Action class
            try{
            removeRouteDefinitionWithoutRunningRoute(camelContext,routeId );
            }
            catch (Exception e1)
            {
               //throw new RuntimeException(e);
               inconsistencies.add(new Inconsistency(buildExceptionMessage(e), application, Inconsistency.ERROR));
            }
            
            inconsistencies.add(new Inconsistency(buildExceptionMessage(e), application, Inconsistency.ERROR));
         }
      }

      return inconsistencies;
   }
}