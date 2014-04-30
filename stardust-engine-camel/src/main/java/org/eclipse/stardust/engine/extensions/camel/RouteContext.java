package org.eclipse.stardust.engine.extensions.camel;

import org.eclipse.stardust.engine.api.model.IApplication;

public abstract class RouteContext
{
   protected IApplication application;
   protected String partitionId;
   protected String camelContextId;

   public abstract String getRouteId();

   public String getApplicationId()
   {
      return application.getId();
   }

   public String getCamelContextId()
   {
      return camelContextId;
   }

   public String getPartitionId()
   {
      return partitionId;
   }
}
