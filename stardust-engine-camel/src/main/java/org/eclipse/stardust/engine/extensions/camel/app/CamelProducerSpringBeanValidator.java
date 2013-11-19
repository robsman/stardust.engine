package org.eclipse.stardust.engine.extensions.camel.app;

import static org.eclipse.stardust.engine.extensions.camel.Util.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidator;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidatorEx;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;

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

      // check if route has been specified
      String routeDefinition = getProvidedRouteConfiguration(application);

      String invocationPattern = getInvocationPattern(application);
      String invocationType = getInvocationType(application);

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

            if (getProvidedRouteConfiguration(application) == null)
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

      return inconsistencies;
   }
}