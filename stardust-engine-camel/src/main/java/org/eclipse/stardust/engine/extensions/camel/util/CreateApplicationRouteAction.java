package org.eclipse.stardust.engine.extensions.camel.util;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ADDITIONAL_SPRING_BEANS_DEF_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CAMEL_CONTEXT_ID_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.DEFAULT_CAMEL_CONTEXT_ID;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.createSpringFileContent;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.loadBeanDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.runtime.LoginUtils;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.AbstractLoginInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.app.CamelApplicationRouteHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class CreateApplicationRouteAction implements Action<Object>
{
   public static final Logger logger = LogManager.getLogger(CreateApplicationRouteAction.class);
   private BpmRuntimeEnvironment bpmRt;
   private ApplicationContext springContext;
   private String partition;
   private IApplication application;

   public CreateApplicationRouteAction(String partition, ApplicationContext springContext)
   {

      this.partition = partition;
      this.springContext = springContext;
   }

   public CreateApplicationRouteAction(BpmRuntimeEnvironment bpmRt, String partition, ApplicationContext springContext)
   {

      this.bpmRt = bpmRt;
      this.partition = partition;
      this.springContext = springContext;
   }

   public CreateApplicationRouteAction(BpmRuntimeEnvironment bpmRt, String partition, ApplicationContext springContext,
         IApplication application)
   {

      this(bpmRt, partition, springContext);
      this.application = application;

   }

   @Override
   public Object execute()
   {

      if (this.bpmRt == null)
      {
         this.bpmRt = PropertyLayerProviderInterceptor.getCurrent();
      }

      Map<String, String> properties = new HashMap<String, String>();
      properties.put(SecurityProperties.PARTITION, partition);
      LoginUtils.mergeDefaultCredentials(Parameters.instance(), properties);
      AbstractLoginInterceptor.setCurrentPartitionAndDomain(Parameters.instance(), bpmRt, properties);

      if (this.application == null)
      {
         createApplicationRoute();
      }
      else
      {
         createApplicationRoute(this.application);
      }

      return null;

   }

   @SuppressWarnings("unchecked")
   private void createApplicationRoute()
   {

      List<IModel> models = ModelManagerFactory.getCurrent().getModels();

      for (int i = 0; i < models.size(); i++)
      {

         IModel m = models.get(i);

         if (ModelManagerFactory.getCurrent().isActive(m))
         {

            ModelElementList<IApplication> apps = m.getApplications();

            for (int ai = 0; ai < apps.size(); ai++)
            {

               IApplication app = apps.get(ai);

               if (app != null && app.getType() != null
                     && (app.getType().getId().equals(CamelConstants.CAMEL_CONSUMER_APPLICATION_TYPE) || app.getType()
                           .getId().equals(CamelConstants.CAMEL_PRODUCER_APPLICATION_TYPE)))
               {
                  logger.debug("Processing Routes defined in " + app.getId());
                  this.createApplicationRoute(app);
               }

            }
         }
      }
   }

   private void createApplicationRoute(IApplication application)
   {

      try
      {
         String contextId = (String) application.getAttribute(CAMEL_CONTEXT_ID_ATT);

         String springBeans = (String) application.getAttribute(ADDITIONAL_SPRING_BEANS_DEF_ATT);

         String invocationPattern = (String) application.getAttribute(CamelConstants.INVOCATION_PATTERN_EXT_ATT);

         if (StringUtils.isEmpty(contextId))
         {
            contextId = DEFAULT_CAMEL_CONTEXT_ID;
         }

         CamelContext camelContext = (CamelContext) springContext.getBean(contextId);

         if (!StringUtils.isEmpty(springBeans))
         {
            loadBeanDefinition(createSpringFileContent(springBeans, false, null),
                  (AbstractApplicationContext) springContext);
         }

         if (CamelConstants.CAMEL_CONSUMER_APPLICATION_TYPE.equals(application.getType().getId()))
         {

            if (StringUtils.isNotEmpty(invocationPattern)
                  && CamelConstants.InvocationPatterns.SENDRECEIVE.equals(invocationPattern))
            {
               CamelApplicationRouteHelper.createAndStartProducerRoute(application, camelContext, partition);
            }

            CamelApplicationRouteHelper.createAndStartConsumerRoute(application, camelContext, partition);

         }
         else if (CamelConstants.CAMEL_PRODUCER_APPLICATION_TYPE.equals(application.getType().getId()))
         {

            CamelApplicationRouteHelper.createAndStartProducerRoute(application, camelContext, partition);

         }
         else
         {

            // old behaviour
            CamelApplicationRouteHelper.createAndStartProducerRoute(application, camelContext, partition);

         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Exception creating route for application " + application.getId(), e);
      }
   }

}