package org.eclipse.stardust.engine.extensions.camel.core;

import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;

public abstract class ApplicationRouteContext extends RouteContext
{
   protected IApplication application;
   
   public String getApplicationId()
   {
      return application.getId();
   }
   /**
    * mark route as transacted by default
    *
    * @return
    */
   public boolean markTransacted()
   {
      Object value = application.getAttribute(CamelConstants.TRANSACTED_ROUTE_EXT_ATT);

      if (value != null && value instanceof Boolean)
         return (Boolean) value;

      if (value != null)
         return Boolean.parseBoolean((String) value);
      return true;

   }
   public String getModelId(){
      return application.getModel().getId();
   }

   public String getId(){
      return application.getId();
   }

}
