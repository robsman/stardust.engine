package org.eclipse.stardust.engine.extensions.camel.core;

public abstract class RouteContext
{

   protected String partitionId;
   protected String camelContextId;

   public abstract String getRouteId();

   public String getCamelContextId()
   {
      return camelContextId;
   }

   public String getPartitionId()
   {
      return partitionId;
   }
}
