package org.eclipse.stardust.engine.extensions.camel.core;

import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.Util;

public abstract class TriggerRouteContext extends RouteContext
{
   protected ITrigger trigger;

   public String getModelId()
   {

      return Util.getModelId(this.trigger);
   }

   public String getProcessId()
   {
      return Util.getProcessId(this.trigger);
   }

   public String getId()
   {
      return this.trigger.getId();
   }
   public String getDescription(){
      return Util.getDescription(getCurrentPartition(), getModelId(), getId());
   }
   public String getUserName()
   {
      return Util.getUserName(this.trigger);
   }

   public String getPassword()
   {
      return Util.getPassword(this.trigger);
   }

   public String getCurrentPartition()
   {
      return Util.getCurrentPartition(this.partitionId);
   }

   public String getProvidedRouteConfiguration()
   {
      return Util.getProvidedRouteConfiguration(this.trigger);
   }

   public boolean includeConversionStrategy()
   {
      return Util.includeConversionStrategy(this.trigger);
   }

   public String getConversionStrategy()
   {
      return Util.getConversionStrategy(this.trigger);
   }

   public String getEventImplementation()
   {
      return Util.getEventImplementation(this.trigger);
   }
   /**
    * mark route as transacted by default
    *
    * @return
    */
   public boolean markTransacted()
   {
      Object value = trigger.getAttribute(CamelConstants.TRANSACTED_ROUTE_EXT_ATT);

      if (value != null && value instanceof Boolean)
         return (Boolean) value;

      if (value != null)
         return Boolean.parseBoolean((String) value);
      return true;

   }

}
