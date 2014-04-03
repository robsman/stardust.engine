package org.eclipse.stardust.engine.extensions.camel.app;

import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.spring.SpringUtils;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerLoader;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.extensions.camel.util.CreateApplicationRouteAction;
import org.eclipse.stardust.engine.extensions.camel.util.LoadPartitionsAction;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

public class CamelApplicationLoader implements ApplicationContextAware
{

   public static final Logger logger = LogManager.getLogger(CamelApplicationLoader.class);

   private ForkingService forkingService;

   private ApplicationContext springContext;

   private String partitionId;

   public ForkingService getForkingService()
   {
      return forkingService;
   }

   public void setForkingService(ForkingService forkingService)
   {
      this.forkingService = forkingService;
   }

   public void setPartitionId(String partitionId)
   {
      this.partitionId = partitionId;
   }

   @Override
   public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
   {
      this.springContext = applicationContext;

      SpringUtils.setApplicationContext((ConfigurableApplicationContext) applicationContext);

      this.bootstrap();
   }
   

   @SuppressWarnings({"unchecked", "rawtypes"})
   private void bootstrap()
   {
      final BpmRuntimeEnvironment bpmRt = PropertyLayerProviderInterceptor.getCurrent();

      if (bpmRt != null && this.partitionId != null)
      {

         Action action = new CreateApplicationRouteAction(bpmRt, this.partitionId, this.springContext);

         action.execute();

      }
      else
      {

         List<String> partitions = null;

         if (this.partitionId == null)
         {
            partitions = (List<String>) this.forkingService.isolate(new LoadPartitionsAction());
         }
         else
         {
            partitions = CollectionUtils.newList();
            partitions.add(this.partitionId);
         }

         for (Iterator<String> i = partitions.iterator(); i.hasNext();)
         {
            String partition = i.next();

            this.forkingService.isolate(new CreateApplicationRouteAction(partition, this.springContext));
         }
      }
   }
}
