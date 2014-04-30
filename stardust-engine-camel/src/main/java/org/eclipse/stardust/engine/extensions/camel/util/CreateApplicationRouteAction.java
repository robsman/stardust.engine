package org.eclipse.stardust.engine.extensions.camel.util;

import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.*;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.runtime.LoginUtils;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.AbstractLoginInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.springframework.context.ApplicationContext;

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
         createRouteForAllApplications(this.springContext, this.partition);
      }
      else
      {
         createRouteForApplication(this.application, this.springContext, this.partition);
      }
      return null;
   }
}