package org.eclipse.stardust.engine.extensions.camel;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ADDITIONAL_SPRING_BEANS_DEF_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CAMEL_CONTEXT_ID_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.DEFAULT_CAMEL_CONTEXT_ID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.INVOCATION_TYPE_EXT_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ROUTE_EXT_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.PRODUCER_ROUTE_ATT;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class Util
{
   public static String getCurrentPartition(String partition)
   {
      if (!StringUtils.isEmpty(partition))
      {
         return partition;
      }
      return Parameters.instance().getString(SecurityProperties.DEFAULT_PARTITION, "default");
   }

   public static String getUserName(ITrigger trigger)
   {
      return (String) trigger.getAllAttributes().get("carnot:engine:camel::username");
   }

   public static String getPassword(ITrigger trigger)
   {
      return (String) trigger.getAllAttributes().get("carnot:engine:camel::password");
   }

   public static String getProcessId(ITrigger trigger)
   {
      return (String) ((IProcessDefinition) trigger.getParent()).getId();
   }

   public static String getModelId(ITrigger trigger)
   {
      return (String) trigger.getModel().getId();
   }

   public static String getProvidedRouteConfiguration(ITrigger trigger)
   {
      return (String) (String) trigger.getAttribute(ROUTE_EXT_ATT);
   }

   public static String getProvidedRouteConfiguration(IApplication application)
   {
      if (CamelConstants.CAMEL_CONSUMER_APPLICATION_TYPE.equals(application.getType().getId()))
         return (String) application.getAttribute(CamelConstants.CONSUMER_ROUTE_ATT);
     // return (String) (String) application.getAttribute(ROUTE_EXT_ATT);
      return (String) (String) application.getAttribute(PRODUCER_ROUTE_ATT);
      
   }

   public static String extractBodyMainType(IData data)
   {
      return (String) data.getAttribute("carnot:engine:className");
   }

   public static String getAdditionalBeansDefinition(IApplication application)
   {
      return (String) application.getAttribute(ADDITIONAL_SPRING_BEANS_DEF_ATT);
   }

   public static String getCamelContextId(IApplication application)
   {

      return checkNotNull((String) application.getAttribute(CAMEL_CONTEXT_ID_ATT), DEFAULT_CAMEL_CONTEXT_ID);

   }

   private static String checkNotNull(String input, String defaultValue)
   {
      if (StringUtils.isEmpty(input))
      {
         input = defaultValue;
      }
      return input;
   }

   public static String getCamelContextId(Application application)
   {

      return checkNotNull((String) application.getAttribute(CamelConstants.CAMEL_CONTEXT_ID_ATT),
            DEFAULT_CAMEL_CONTEXT_ID);
   }

   public static String getInvocationPattern(IApplication application)
   {
      return (String) application.getAttribute(CamelConstants.INVOCATION_PATTERN_EXT_ATT);
   }

   public static String getInvocationPattern(Application application)
   {
      return (String) application.getAttribute(CamelConstants.INVOCATION_PATTERN_EXT_ATT);
   }

   public static String getInvocationType(IApplication application)
   {
      return (String) application.getAttribute(INVOCATION_TYPE_EXT_ATT);
   }

   public static Object getBodyOutAccessPoint(Application application)
   {
      return application.getAttribute(CamelConstants.CAT_BODY_OUT_ACCESS_POINT);
   }

   public static Object getBodyInAccessPoint(Application application)
   {
      return application.getAttribute(CamelConstants.CAT_BODY_IN_ACCESS_POINT);
   }

   public static Object getSupportMultipleAccessPointAttribute(Application application)
   {
      return application.getAttribute(CamelConstants.SUPPORT_MULTIPLE_ACCESS_POINTS);
   }

   public static ApplicationContext getActivityInstanceApplicationContext(ActivityInstance ai)
   {
      return ai.getActivity().getApplicationContext("application");
   }

   public static ApplicationContext getActivityInstanceDefaultContext(ActivityInstance ai)
   {
      return ai.getActivity().getApplicationContext("default");
   }

}
