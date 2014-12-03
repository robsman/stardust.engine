package org.eclipse.stardust.engine.extensions.camel.trigger;

import java.util.Iterator;
import java.util.List;

import org.apache.camel.CamelContext;
import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.spring.SpringUtils;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.extensions.camel.util.CreateTriggerRouteAction;
import org.eclipse.stardust.engine.extensions.camel.util.LoadPartitionsAction;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

public class CamelTriggerLoader implements ApplicationContextAware
{

   private ForkingService forkingService;
   private List<CamelContext> camelContexts;
   //private List<DataConverter> dataConverters;
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

   public List<CamelContext> getCamelContexts()
   {
      return camelContexts;
   }

   public void setCamelContexts(List<CamelContext> camelContexts)
   {
      this.camelContexts = camelContexts;
   }

   public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
   {

      this.springContext = applicationContext;

      SpringUtils.setApplicationContext((ConfigurableApplicationContext) applicationContext);

      this.bootstrap();

   }

   public void setPartitionId(String partitionId)
   {
      this.partitionId = partitionId;
   }

   /**
    * used to generate Camel routes from the audit trial at startup
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   private void bootstrap()
   {

      final BpmRuntimeEnvironment bpmRt = PropertyLayerProviderInterceptor.getCurrent();

      if (bpmRt != null && this.partitionId != null)
      {

         Action action = new CreateTriggerRouteAction(bpmRt, this.partitionId, this.springContext);

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
            this.forkingService
                  .isolate(new CreateTriggerRouteAction(partition, this.springContext));
         }
      }
   }
}
